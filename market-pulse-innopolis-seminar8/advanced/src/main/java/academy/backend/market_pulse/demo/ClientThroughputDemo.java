package academy.backend.market_pulse.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.impl.HttpUrlConnectionTInvestApiClient;
import academy.backend.market_pulse.client.impl.reactor.ReactorTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Живая демонстрация этапа 9 (см. «План семинара.md», семинар 7): четыре способа сходить за
 * котировками {@code N} раз к моковому серверу и измерить пропускную способность — без JMH, просто
 * по часам ({@link System#nanoTime()}), чтобы цифры менялись на глазах у группы:
 *
 * <ol>
 *   <li><b>Один поток, N запросов</b> — блокирующий {@link HttpUrlConnectionTInvestApiClient} зовут
 *       в цикле один за другим: каждый запрос ждёт предыдущий;
 *   <li><b>Многопоточность</b> — тот же блокирующий клиент, но запросы раздаются пулу потоков
 *       (поток на запрос);
 *   <li><b>Reactor</b> — все N запросов уходят сразу в один поток событийного цикла;
 *   <li><b>HttpClient.sendAsync</b> — готовый клиент из JDK: все N запросов уходят сразу, ответы
 *       собираются в {@link CompletableFuture}.
 * </ol>
 *
 * <p>Запуск: {@code ClientThroughputDemo [запросов] [размер пула]} (по умолчанию 30 и 10). Моковый
 * сервер (Docker, см. {@code docker/mockserver}) должен быть поднят и отвечать с задержкой порядка
 * 300 мс — иначе разницу не видно. Перед каждым замером выполняется один прогревочный запрос
 * (рукопожатие, загрузка классов), чтобы не мерить разовые накладные.
 */
public class ClientThroughputDemo {

    private static final int DEFAULT_REQUESTS = 30;
    private static final int DEFAULT_POOL_SIZE = 10;

    public static void main(String[] args) throws Exception {
        int requests = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_REQUESTS;
        int poolSize = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_POOL_SIZE;
        TInvestApiProperties properties = TInvestApiProperties.localMock();

        System.out.printf("Запросов: %d. Размер пула для многопоточного варианта: %d.%n%n", requests, poolSize);

        HttpUrlConnectionTInvestApiClient blockingClient = new HttpUrlConnectionTInvestApiClient(properties);
        blockingClient.fetchQuotes();

        long oneThreadMs = measureOneThread(blockingClient, requests);
        report("1. Один поток, запросы подряд", requests, oneThreadMs);

        long poolMs = measureThreadPool(blockingClient, requests, poolSize);
        report("2. Многопоточность: пул из " + poolSize + " потоков", requests, poolMs);

        try (ReactorTInvestApiClient reactorClient = new ReactorTInvestApiClient(properties)) {
            reactorClient.fetchQuotes();
            long reactorMs = measureAsync(reactorClient::fetchQuotesAsync, requests);
            report("3. Reactor: один поток цикла", requests, reactorMs);
        }

        HttpClientTInvestApiClient httpClient = new HttpClientTInvestApiClient(properties);
        httpClient.fetchQuotes();
        long httpClientMs = measureAsync(httpClient::fetchQuotesAsync, requests);
        report("4. HttpClient.sendAsync", requests, httpClientMs);
    }

    private static long measureOneThread(TInvestApiClient client, int requests) {
        long start = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            client.fetchQuotes();
        }
        return elapsedMillis(start);
    }

    private static long measureAsync(Supplier<CompletableFuture<?>> request, int requests) {
        long start = System.nanoTime();
        List<CompletableFuture<?>> futures = new ArrayList<>(requests);
        for (int i = 0; i < requests; i++) {
            futures.add(request.get());
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return elapsedMillis(start);
    }

    private static long measureThreadPool(TInvestApiClient client, int requests, int poolSize)
            throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        try {
            List<Callable<Void>> tasks = new ArrayList<>(requests);
            for (int i = 0; i < requests; i++) {
                tasks.add(() -> {
                    client.fetchQuotes();
                    return null;
                });
            }
            long start = System.nanoTime();
            for (Future<Void> future : pool.invokeAll(tasks)) {
                try {
                    future.get();
                } catch (java.util.concurrent.ExecutionException e) {
                    throw new IllegalStateException(e.getCause());
                }
            }
            return elapsedMillis(start);
        } finally {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    private static void report(String title, int requests, long millis) {
        double perSecond = requests * 1000.0 / Math.max(millis, 1);
        System.out.printf("%-42s %6d мс   %7.1f запросов/с%n", title, millis, perSecond);
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
