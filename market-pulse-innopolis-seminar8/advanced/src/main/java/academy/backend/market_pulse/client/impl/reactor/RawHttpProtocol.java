package academy.backend.market_pulse.client.impl.reactor;

import java.nio.charset.StandardCharsets;

import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Минимальный HTTP/1.1 поверх сырых байтов — ровно столько, сколько нужно для одного {@code GET} и
 * его ответа (см. «План семинара.md», семинар 7, этап 7). Не разбирает {@code Content-Length} и
 * chunked-кодирование: запрос явно шлёт {@code Connection: close}, поэтому {@link Reactor} читает
 * канал до EOF и не должен сам решать, когда ответ «закончился» — это упрощение уместно для
 * реактора-иллюстрации, не для универсального HTTP-клиента.
 */
final class RawHttpProtocol {

    private RawHttpProtocol() {
    }

    static byte[] buildGetRequest(String host, int port, String path) {
        return buildGetRequest(host, port, path, null);
    }

    /** Если {@code mockScenario} не {@code null}, добавляет заголовок {@code X-Mock-Scenario}. */
    static byte[] buildGetRequest(String host, int port, String path, String mockScenario) {
        StringBuilder request = new StringBuilder()
                .append("GET ").append(path).append(" HTTP/1.1\r\n")
                .append("Host: ").append(host).append(':').append(port).append("\r\n")
                .append("Accept: application/json\r\n");
        if (mockScenario != null) {
            request.append(TInvestApiProperties.MOCK_SCENARIO_HEADER).append(": ").append(mockScenario)
                    .append("\r\n");
        }
        request.append("Connection: close\r\n").append("\r\n");
        return request.toString().getBytes(StandardCharsets.US_ASCII);
    }

    record ParsedResponse(int statusCode, String body) {
    }

    static ParsedResponse parse(byte[] raw) {
        String text = new String(raw, StandardCharsets.UTF_8);
        int headerEnd = text.indexOf("\r\n\r\n");
        if (headerEnd < 0) {
            throw new IllegalStateException("Ответ сервера не содержит разделителя заголовков/тела");
        }
        String headers = text.substring(0, headerEnd);
        String body = text.substring(headerEnd + 4);

        String statusLine = headers.lines().findFirst()
                .orElseThrow(() -> new IllegalStateException("Пустой ответ сервера"));
        // "HTTP/1.1 200 OK" -> 200
        String[] parts = statusLine.split(" ", 3);
        int statusCode = Integer.parseInt(parts[1]);

        return new ParsedResponse(statusCode, body);
    }
}
