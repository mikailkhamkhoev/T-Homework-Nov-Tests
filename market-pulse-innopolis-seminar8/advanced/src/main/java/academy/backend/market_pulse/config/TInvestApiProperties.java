package academy.backend.market_pulse.config;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Настройки клиента T-Invest API — вынесены в отдельный класс, а не захардкожены внутри клиента:
 * обе реализации ({@code HttpClientTInvestApiClient}, реакторная) принимают их через конструктор
 * вместе с остальными зависимостями (см. «План семинара.md», семинар 7, этап 2). {@code host}/
 * {@code port} указывают на локальный моковый сервер (Docker, см. {@code docker/mockserver}), а не
 * на настоящий T-Invest API.
 *
 * <p>{@code mockScenario} — необязательный сценарий мокового сервера ({@code error},
 * {@code timeout}, {@code ok}): если задан, клиенты отправляют его заголовком
 * {@code X-Mock-Scenario}, и сбой можно воспроизвести прямо командой {@code update}. {@code null} —
 * заголовок не отправляется.
 */
@Getter
@AllArgsConstructor
public class TInvestApiProperties {

    public static final String MOCK_SCENARIO_HEADER = "X-Mock-Scenario";

    private final String host;
    private final int port;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final String mockScenario;

    public TInvestApiProperties(String host, int port, Duration connectTimeout, Duration requestTimeout) {
        this(host, port, connectTimeout, requestTimeout, null);
    }

    public static TInvestApiProperties localMock() {
        return new TInvestApiProperties("localhost", 1080, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    /** Копия настроек с заданным сценарием мокового сервера; пустое значение означает "без сценария". */
    public TInvestApiProperties withMockScenario(String scenario) {
        String normalized = scenario == null || scenario.isBlank() ? null : scenario.trim();
        return new TInvestApiProperties(host, port, connectTimeout, requestTimeout, normalized);
    }

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }
}
