package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.impl.HttpUrlConnectionTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.model.Quote;

/**
 * Показ этапа 2 (см. «План семинара.md», семинар 7): опорный клиент к {@code instruments-service}
 * против мокового сервера (Docker, см. {@code docker/mockserver} — поднять заранее). Печатает
 * полученные котировки — ровно то, что заменяет собой текущую заглушку {@code QuoteSource}.
 */
public class TInvestApiClientDemo {

    public static void main(String[] args) {
        TInvestApiClient client = new HttpUrlConnectionTInvestApiClient(TInvestApiProperties.localMock());

        System.out.println("Запрос к instruments-service (http://localhost:1080/v1/instruments)...");
        for (Quote quote : client.fetchQuotes()) {
            System.out.println(quote);
        }
    }
}
