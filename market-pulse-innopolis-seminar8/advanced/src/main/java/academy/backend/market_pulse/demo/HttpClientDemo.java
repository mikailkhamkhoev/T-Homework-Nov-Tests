package academy.backend.market_pulse.demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.model.Quote;

/**
 * Показ этапа 8 (см. «План семинара.md», семинар 7): готовый {@code HttpClient} — то, что мы только
 * что писали руками на {@code Selector}, уже внутри. Один и тот же запрос двумя способами:
 * {@code send} (поток вызывающего кода ждёт ответа) и {@code sendAsync} (возвращает управление сразу,
 * результат придёт в {@link CompletableFuture}). Моковый сервер (Docker) поднять заранее.
 */
public class HttpClientDemo {

    public static void main(String[] args) {
        HttpClientTInvestApiClient client = new HttpClientTInvestApiClient(TInvestApiProperties.localMock());

        List<Quote> blocking = client.fetchQuotes();
        System.out.println("send: получено котировок — " + blocking.size());

        CompletableFuture<List<Quote>> future = client.fetchQuotesAsync();
        System.out.println("sendAsync: запрос ушёл, поток свободен — можно заняться другим делом");
        System.out.println("sendAsync: получено котировок — " + future.join().size());
    }
}
