package academy.backend.market_pulse.client.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestClientMode;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Один и тот же набор сценариев для всех реализаций {@link TInvestApiClient} против WireMock — вместо
 * Docker-мока и самописного сервера. WireMock поднимается на свободном порту, заглушки задаются
 * из теста, а {@code verify} проверяет, что именно ушло в сеть.
 */
class TInvestApiClientWireMockTest {

    private static final String INSTRUMENTS = "/v1/instruments";
    private static final String TWO_INSTRUMENTS = """
            [
              {"ticker": "SBER", "name": "Сбер", "type": "STOCK", "currency": "RUB", "price": 250.00, "changePercent": 1.20},
              {"ticker": "FXUS", "name": "FinEx FXUS", "type": "ETF", "currency": "USD", "price": 6.85, "changePercent": 0.90}
            ]
            """;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private static TInvestApiProperties properties() {
        return new TInvestApiProperties("localhost", wireMock.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(2));
    }

    private static TInvestApiClient client(TInvestClientMode mode) {
        return TInvestClientMode.createClient(mode, properties());
    }

    @ParameterizedTest
    @EnumSource(TInvestClientMode.class)
    void parsesInstrumentsFromTheResponse(TInvestClientMode mode) {
        wireMock.stubFor(get(urlEqualTo(INSTRUMENTS))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TWO_INSTRUMENTS)));

        List<Quote> quotes = client(mode).fetchQuotes();

        assertThat(quotes)
                .extracting(q -> q.getInstrument().getTicker(), q -> q.getInstrument().getType())
                .containsExactly(tuple("SBER", "STOCK"),
                        tuple("FXUS", "ETF"));
    }

    @ParameterizedTest
    @EnumSource(TInvestClientMode.class)
    void asksForJsonAndSendsExactlyOneRequest(TInvestClientMode mode) {
        wireMock.stubFor(get(urlEqualTo(INSTRUMENTS)).willReturn(aResponse().withStatus(200).withBody("[]")));

        client(mode).fetchQuotes();

        wireMock.verify(1, getRequestedFor(urlEqualTo(INSTRUMENTS))
                .withHeader("Accept", equalTo("application/json")));
    }

    @ParameterizedTest
    @EnumSource(TInvestClientMode.class)
    void forwardsTheMockScenarioHeaderWhenConfigured(TInvestClientMode mode) {
        wireMock.stubFor(get(urlEqualTo(INSTRUMENTS)).willReturn(aResponse().withStatus(200).withBody("[]")));
        TInvestApiClient client = TInvestClientMode.createClient(mode, properties().withMockScenario("error"));

        client.fetchQuotes();

        wireMock.verify(getRequestedFor(urlEqualTo(INSTRUMENTS))
                .withHeader(TInvestApiProperties.MOCK_SCENARIO_HEADER, equalTo("error")));
    }

    @ParameterizedTest
    @EnumSource(TInvestClientMode.class)
    void emptyCatalogIsAnEmptyList(TInvestClientMode mode) {
        wireMock.stubFor(get(urlEqualTo(INSTRUMENTS)).willReturn(aResponse().withStatus(200).withBody("[]")));

        assertThat(client(mode).fetchQuotes()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(TInvestClientMode.class)
    void brokenConnectionIsReportedAsAnApiException(TInvestClientMode mode) {
        wireMock.stubFor(get(urlEqualTo(INSTRUMENTS))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> client(mode).fetchQuotes()).isInstanceOf(TInvestApiException.class);
    }
}
