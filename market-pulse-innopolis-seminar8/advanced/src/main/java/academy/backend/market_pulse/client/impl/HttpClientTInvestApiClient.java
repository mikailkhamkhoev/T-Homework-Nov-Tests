package academy.backend.market_pulse.client.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import academy.backend.market_pulse.client.InstrumentQuoteMapper;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Реализация {@link TInvestApiClient} на {@link HttpClient} из JDK 11+ — показывается после
 * Reactor (см. «План семинара.md», семинар 7, этап 7): всё, что мы только что писали руками, здесь
 * уже готово. Под капотом у {@link HttpClient} — свой событийный цикл на {@code Selector}, поэтому
 * {@link #fetchQuotesAsync()} ({@code sendAsync}) не занимает по потоку на запрос, а {@link #fetchQuotes()}
 * ({@code send}) — удобная блокирующая обёртка для вызывающего потока. {@link HttpClient}
 * создаётся один раз в конструкторе и переиспользуется.
 */
public class HttpClientTInvestApiClient implements TInvestApiClient {

    private final HttpClient httpClient;
    private final TInvestApiProperties properties;

    public HttpClientTInvestApiClient(TInvestApiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

    /** Синхронный путь: {@code send} блокирует вызывающий поток до ответа. */
    @Override
    public List<Quote> fetchQuotes() {
        HttpResponse<String> response;
        try {
            response = httpClient.send(buildRequest(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new TInvestApiException("Не удалось выполнить запрос к instruments-service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TInvestApiException("Запрос к instruments-service прерван", e);
        }
        return toQuotes(response);
    }

    /** Асинхронный путь: {@code sendAsync} возвращает управление сразу, результат придёт в future. */
    public CompletableFuture<List<Quote>> fetchQuotesAsync() {
        return httpClient.sendAsync(buildRequest(), HttpResponse.BodyHandlers.ofString())
                .thenApply(this::toQuotes);
    }

    private HttpRequest buildRequest() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/v1/instruments"))
                .timeout(properties.getRequestTimeout())
                .header("Accept", "application/json")
                .GET();
        if (properties.getMockScenario() != null) {
            requestBuilder.header(TInvestApiProperties.MOCK_SCENARIO_HEADER, properties.getMockScenario());
        }
        return requestBuilder.build();
    }

    private List<Quote> toQuotes(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new TInvestApiException(
                    "instruments-service вернул неожиданный статус: " + response.statusCode());
        }
        return InstrumentQuoteMapper.fromJson(response.body());
    }
}
