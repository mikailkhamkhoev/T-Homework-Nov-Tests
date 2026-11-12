package academy.backend.market_pulse.client.impl.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Один поток, один {@link Selector}, сколько угодно одновременных запросов (см. «План
 * семинара.md», семинар 7, этапы 6–7). Это и есть главная идея: вместо потока на запрос —
 * один поток следит сразу за всеми сокетами и реагирует только на те, что готовы.
 *
 * <p>Новый запрос не ждёт своей очереди на текущий {@link Selector#select()} — {@link #submit}
 * кладёт канал в очередь регистрации и будит цикл через {@link Selector#wakeup()}, вместо того
 * чтобы регистрировать канал напрямую из вызывающего потока (регистрация и {@code select()} не
 * должны происходить из разных потоков одновременно без синхронизации).
 */
public final class Reactor implements AutoCloseable {

    private final Selector selector;
    private final Queue<PendingExchange> pendingRegistrations = new ConcurrentLinkedQueue<>();
    private final Thread loopThread;
    private volatile boolean running = true;

    public Reactor() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось открыть Selector", e);
        }
        this.loopThread = new Thread(this::runLoop, "reactor-event-loop");
        this.loopThread.setDaemon(true);
    }

    public void start() {
        loopThread.start();
    }

    /**
     * Отправляет запрос и возвращает {@link CompletableFuture}, который завершится сырым телом
     * ответа — не блокирует вызывающий поток ни на подключение, ни на ожидание ответа.
     */
    public CompletableFuture<byte[]> submit(InetSocketAddress address, byte[] requestBytes) {
        SocketChannel channel;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(address);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        PendingExchange exchange = new PendingExchange(channel, requestBytes);
        pendingRegistrations.add(exchange);
        selector.wakeup(); // прерывает текущий select(), чтобы цикл сразу подобрал новый канал
        return exchange.future;
    }

    private void runLoop() {
        try {
            while (running) {
                registerPending();

                try {
                    selector.select();
                } catch (IOException e) {
                    if (running) {
                        throw new IllegalStateException("Ошибка в цикле Selector.select()", e);
                    }
                    return;
                }

                for (SelectionKey key : selector.selectedKeys()) {
                    try {
                        dispatch(key);
                    } catch (IOException | CancelledKeyException e) {
                        failAndClose(key, e);
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (ClosedSelectorException e) {
            // close() закрывает Selector под работающим циклом — это штатное завершение, не сбой.
            if (running) {
                throw e;
            }
        }
    }

    private void registerPending() {
        PendingExchange exchange;
        while ((exchange = pendingRegistrations.poll()) != null) {
            try {
                exchange.channel.register(selector, SelectionKey.OP_CONNECT, exchange);
            } catch (ClosedChannelException e) {
                exchange.future.completeExceptionally(e);
            }
        }
    }

    private void dispatch(SelectionKey key) throws IOException {
        PendingExchange exchange = (PendingExchange) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        if (key.isConnectable()) {
            channel.finishConnect();
            key.interestOps(SelectionKey.OP_WRITE); // сокет готов — переключаемся на запись запроса
        } else if (key.isWritable()) {
            channel.write(exchange.requestBuffer);
            if (!exchange.requestBuffer.hasRemaining()) {
                key.interestOps(SelectionKey.OP_READ); // запрос дописан — ждём ответ
            }
        } else if (key.isReadable()) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int read = channel.read(buffer);
            if (read == -1) {
                // Connection: close — сервер закрыл соединение, значит ответ получен целиком.
                key.cancel();
                channel.close();
                exchange.future.complete(exchange.responseBuffer.toByteArray());
            } else if (read > 0) {
                exchange.responseBuffer.write(buffer.array(), 0, read);
            }
        }
    }

    private void failAndClose(SelectionKey key, Exception cause) {
        PendingExchange exchange = (PendingExchange) key.attachment();
        key.cancel();
        try {
            key.channel().close();
        } catch (IOException ignored) {
            // канал и так не рабочий — закрыть по возможности, вторичная ошибка здесь не интересна
        }
        if (exchange != null) {
            exchange.future.completeExceptionally(cause);
        }
    }

    @Override
    public void close() {
        running = false;
        selector.wakeup();
        try {
            selector.close();
        } catch (IOException ignored) {
            // завершаем работу в любом случае
        }
    }
}
