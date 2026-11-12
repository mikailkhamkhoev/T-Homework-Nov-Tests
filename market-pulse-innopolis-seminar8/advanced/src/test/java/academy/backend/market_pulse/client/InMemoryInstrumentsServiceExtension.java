package academy.backend.market_pulse.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.sun.net.httpserver.HttpServer;

/**
 * Мини-сервер на {@code com.sun.net.httpserver.HttpServer} (стандартная библиотека JDK, без
 * дополнительных зависимостей) — имитирует {@code GET /v1/instruments} ровно настолько, насколько
 * нужно тестам {@link TInvestApiClient} и {@code ReactorTInvestApiClient}. Докеровский MockServer
 * с занятия (см. «План семинара.md», семинар 7) этот тест не задействует и не подменяет —
 * используется только для проверки кода в этой среде подготовки.
 */
class InMemoryInstrumentsServiceExtension implements BeforeEachCallback, AfterEachCallback {

    private HttpServer server;
    private volatile String responseBody = "[]";
    private volatile int responseStatus = 200;
    private volatile long responseDelayMillis = 0;
    private volatile String lastMockScenario;

    int getPort() {
        return server.getAddress().getPort();
    }

    /** Значение заголовка {@code X-Mock-Scenario} последнего запроса; {@code null}, если его не было. */
    String getLastMockScenario() {
        return lastMockScenario;
    }

    void respondWith(int status, String body) {
        this.responseStatus = status;
        this.responseBody = body;
        this.responseDelayMillis = 0;
    }

    void respondAfterDelay(long millis, String body) {
        this.responseStatus = 200;
        this.responseBody = body;
        this.responseDelayMillis = millis;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v1/instruments", exchange -> {
            lastMockScenario = exchange.getRequestHeaders().getFirst("X-Mock-Scenario");
            if (responseDelayMillis > 0) {
                try {
                    Thread.sleep(responseDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        server.start();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        server.stop(0);
    }
}
