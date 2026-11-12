package academy.backend.market_pulse.client;

import java.util.List;

import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Клиент к {@code instruments-service} T-Invest API (за моковым сервером на занятии — см. «План
 * семинара.md», семинар 7). Один интерфейс, три реализации (см. пакет {@code client.impl}):
 * блокирующая на {@code HttpURLConnection} (опорная — этап 2), реакторная на {@code Selector}
 * ({@code ReactorTInvestApiClient}, этап 7) и готовая на {@code HttpClient} (этап 8) — вызывающий
 * код не знает и не должен знать, какая из них используется под {@link TInvestClientMode}.
 */
public interface TInvestApiClient {

    /**
     * Текущий набор инструментов с котировками. Бросает {@link TInvestApiException} при сетевой
     * ошибке, неожиданном статусе ответа или битом JSON.
     */
    List<Quote> fetchQuotes();
}
