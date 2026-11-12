package academy.backend.market_pulse.fallback;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.exception.TInvestApiException;

/**
 * Запасной вариант: тест ошибок {@link HttpClientTInvestApiClient}, сгенерированный ИИ по промпту из
 * {@code docs/seminar8/Промпт для генерации тестов.md} и проверенный по чек-листу ревью. Нужен, если
 * генерация на занятии не удалась.
 */
class HttpClientErrorsFallbackTest {

    private static final String PATH = "/v1/instruments";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private static HttpClientTInvestApiClient clientWithRequestTimeout(Duration requestTimeout) {
        return new HttpClientTInvestApiClient(
                new TInvestApiProperties("localhost", wireMock.getPort(), Duration.ofSeconds(2), requestTimeout));
    }

    @ParameterizedTest(name = "статус {0} -> TInvestApiException")
    @ValueSource(ints = {400, 401, 404, 429, 500, 502, 503})
    void unexpectedStatusIsReportedAsAnApiException(int status) {
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status).withBody("{}")));

        assertThatThrownBy(() -> clientWithRequestTimeout(Duration.ofSeconds(2)).fetchQuotes())
                .isInstanceOf(TInvestApiException.class)
                .hasMessageContaining(String.valueOf(status));
    }

    @ParameterizedTest(name = "тело [{0}] -> TInvestApiException")
    @ValueSource(strings = {"", "not a json", "{", "[{\"ticker\": }]", "{\"ticker\": \"SBER\"}"})
    void malformedBodyIsReportedAsAnApiException(String body) {
        wireMock.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200).withBody(body)));

        assertThatThrownBy(() -> clientWithRequestTimeout(Duration.ofSeconds(2)).fetchQuotes())
                .isInstanceOf(TInvestApiException.class);
    }

    @Test
    void responseSlowerThanTheRequestTimeoutIsReportedAsAnApiException() {
        wireMock.stubFor(get(urlEqualTo(PATH))
                .willReturn(aResponse().withStatus(200).withBody("[]").withFixedDelay(1500)));

        assertThatThrownBy(() -> clientWithRequestTimeout(Duration.ofMillis(300)).fetchQuotes())
                .isInstanceOf(TInvestApiException.class);
    }
}
