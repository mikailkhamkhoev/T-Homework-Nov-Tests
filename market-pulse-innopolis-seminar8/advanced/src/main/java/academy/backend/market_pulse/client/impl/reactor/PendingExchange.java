package academy.backend.market_pulse.client.impl.reactor;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

/**
 * Состояние одного запроса в процессе обработки: канал, недописанный буфер запроса, накопленный
 * ответ и {@link CompletableFuture}, который завершится результатом (см. «План семинара.md»,
 * семинар 7, этапы 6–7). Один объект на один запрос — {@link Reactor} держит их через
 * {@code SelectionKey.attach(...)}, а не в отдельной карте по каналу.
 */
final class PendingExchange {

    final SocketChannel channel;
    final ByteBuffer requestBuffer;
    final ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
    final CompletableFuture<byte[]> future = new CompletableFuture<>();

    PendingExchange(SocketChannel channel, byte[] requestBytes) {
        this.channel = channel;
        this.requestBuffer = ByteBuffer.wrap(requestBytes);
    }
}
