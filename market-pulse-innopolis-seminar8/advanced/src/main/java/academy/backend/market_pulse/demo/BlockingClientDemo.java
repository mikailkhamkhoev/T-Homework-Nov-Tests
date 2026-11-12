package academy.backend.market_pulse.demo;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.impl.HttpUrlConnectionTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Демо этапа 3 «Наивное решение: больше потоков» (см. «План семинара.md», семинар 7): показывает
 * потолок этого решения — проблему, которую решает Reactor, — вживую, а не рассказывает о ней.
 * Небольшой фиксированный пул потоков ({@value #POOL_SIZE}) обслуживает {@value #REQUEST_COUNT}
 * одновременных запросов через блокирующий {@link HttpUrlConnectionTInvestApiClient#fetchQuotes()} — по
 * логу видно, что одновременно работает не больше {@value #POOL_SIZE} запросов, остальные ждут
 * своей очереди на свободный поток, хотя каждый запрос сам по себе ничего не вычисляет — только
 * ждёт сеть.
 *
 * <p>Моковый сервер (Docker) должен отвечать не мгновенно — иначе разницу не видно; expectations
 * для сценария по умолчанию добавляют задержку порядка нескольких сотен миллисекунд (см.
 * {@code docker/mockserver}).
 */
public class BlockingClientDemo {

    private static final int POOL_SIZE = 3;
    private static final int REQUEST_COUNT = 8;

    public static void main(String[] args) throws InterruptedException {
        TInvestApiClient client = new HttpUrlConnectionTInvestApiClient(TInvestApiProperties.localMock());
        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

        System.out.printf("Пул потоков: %d. Одновременных запросов: %d.%n", POOL_SIZE, REQUEST_COUNT);
        System.out.println("Ожидаем: не больше " + POOL_SIZE + " запросов в работе одновременно,");
        System.out.println("остальные стоят в очереди на свободный поток.");
        System.out.println();

        long start = System.nanoTime();
        List<Callable<Void>> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < REQUEST_COUNT; i++) {
            int requestNumber = i;
            tasks.add(() -> {
                long requestStart = elapsedMillis(start);
                System.out.printf("[%4d мс] %-12s запрос #%d: отправлен%n",
                        requestStart, Thread.currentThread().getName(), requestNumber);

                client.fetchQuotes(); // блокирует поток на всё время ожидания ответа сети

                long requestEnd = elapsedMillis(start);
                System.out.printf("[%4d мс] %-12s запрос #%d: получен ответ (ждал %d мс)%n",
                        requestEnd, Thread.currentThread().getName(), requestNumber, requestEnd - requestStart);
                return null;
            });
        }

        List<Future<Void>> futures = pool.invokeAll(tasks);
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println();
        System.out.println("Все " + futures.size() + " запросов завершены за " + elapsedMillis(start) + " мс.");
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
