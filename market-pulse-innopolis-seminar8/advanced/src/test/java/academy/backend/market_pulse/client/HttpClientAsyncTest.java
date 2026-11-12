package academy.backend.market_pulse.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;

/** Асинхронный путь {@code sendAsync} готового {@code HttpClient}-клиента. */
class HttpClientAsyncTest {

    @RegisterExtension
    final InMemoryInstrumentsServiceExtension server = new InMemoryInstrumentsServiceExtension();

    @Test
    void fetchQuotesAsync_completesWithParsedQuotes() {
        server.respondWith(200, """
                [{"ticker": "SBER", "name": "Сбер", "type": "STOCK", "currency": "RUB", "price": 250.00, "changePercent": 1.20}]
                """);
        HttpClientTInvestApiClient client = new HttpClientTInvestApiClient(
                new TInvestApiProperties("localhost", server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(2)));

        assertEquals(1, client.fetchQuotesAsync().join().size());
    }
}
