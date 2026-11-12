package academy.backend.market_pulse.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Контракт, которому должна соответствовать любая реализация {@link TInvestApiClient} — опорная
 * {@link HttpClientTInvestApiClient} и реакторная {@code ReactorTInvestApiClient} (см. «План
 * семинара.md», семинар 7). Наследники подставляют свою реализацию в {@link #createClient}.
 */
public abstract class TInvestApiClientContractTestBase {

    @RegisterExtension
    final InMemoryInstrumentsServiceExtension server = new InMemoryInstrumentsServiceExtension();

    protected abstract TInvestApiClient createClient(TInvestApiProperties properties);

    private TInvestApiProperties propertiesForServer() {
        return new TInvestApiProperties("localhost", server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(2));
    }

    @Test
    void fetchQuotes_parsesInstrumentsFromResponse() {
        server.respondWith(200, """
                [
                  {"ticker": "SBER", "name": "Сбер", "type": "STOCK", "currency": "RUB", "price": 250.00, "changePercent": 1.20},
                  {"ticker": "OFZ26233", "name": "ОФЗ 26233", "type": "BOND", "currency": "RUB", "price": 780.00, "changePercent": -0.15}
                ]
                """);

        List<Quote> quotes = createClient(propertiesForServer()).fetchQuotes();

        assertEquals(2, quotes.size());
        Quote sber = quotes.get(0);
        assertEquals("SBER", sber.getInstrument().getTicker());
        assertEquals("STOCK", sber.getInstrument().getType());
        assertEquals(0, new java.math.BigDecimal("250.00").compareTo(sber.getPrice()));
    }

    @Test
    void fetchQuotes_throwsOnServerError() {
        server.respondWith(500, "boom");

        TInvestApiClient client = createClient(propertiesForServer());
        assertThrows(TInvestApiException.class, client::fetchQuotes);
    }

    @Test
    void fetchQuotes_handlesEmptyCatalog() {
        server.respondWith(200, "[]");

        List<Quote> quotes = createClient(propertiesForServer()).fetchQuotes();

        assertEquals(0, quotes.size());
    }

    @Test
    void fetchQuotes_sendsMockScenarioHeaderWhenConfigured() {
        server.respondWith(200, "[]");

        createClient(propertiesForServer().withMockScenario("error")).fetchQuotes();

        assertEquals("error", server.getLastMockScenario());
    }

    @Test
    void fetchQuotes_omitsMockScenarioHeaderByDefault() {
        server.respondWith(200, "[]");

        createClient(propertiesForServer()).fetchQuotes();

        assertNull(server.getLastMockScenario());
    }
}
