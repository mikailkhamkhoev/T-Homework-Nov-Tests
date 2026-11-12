package academy.backend.market_pulse.cli;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.FileBackedQuoteRepository;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

/**
 * Обновляет на диске котировки отслеживаемых инструментов (см. «Требования к файловому
 * хранилищу.md», семинар 7): запрашивает актуальные данные через {@link TInvestApiClient} и
 * формирует новый файл с обновлёнными значениями. Самостоятельная работа — не часть тайминга
 * занятия, эталон для сверки в начале семинара 8 при необходимости. Заодно пишет точку в историю
 * цен каждого обновлённого инструмента — материал для будущей статистики.
 */
@Command(name = "update", description = "Обновить на диске котировки отслеживаемых инструментов")
@RequiredArgsConstructor
public class UpdateCommand implements Callable<Integer> {

    private final TInvestApiClient client;
    private final Watchlist watchlist;
    private final FileBackedQuoteRepository repository;
    private final PriceHistoryStore historyStore;
    private final Path historyDirectory;

    @Override
    public Integer call() {
        Set<String> tracked = watchlist.tickers();
        if (tracked.isEmpty()) {
            System.out.println("Watchlist пуст — нечего обновлять. Добавьте тикеры командой watch.");
            return 1;
        }

        List<Quote> quotes;
        try {
            quotes = client.fetchQuotes();
        } catch (TInvestApiException e) {
            System.out.println("Не удалось получить данные от instruments-service: " + e.getMessage());
            return 1;
        }

        List<Quote> trackedQuotes = quotes.stream()
                .filter(quote -> tracked.contains(quote.getInstrument().getTicker()))
                .toList();

        repository.save(trackedQuotes);

        Instant now = Instant.now();
        for (Quote quote : trackedQuotes) {
            historyStore.append(historyDirectory,
                    new PricePoint(quote.getInstrument().getTicker(), now, quote.getPrice()));
        }

        System.out.println("Обновлено котировок: " + trackedQuotes.size() + " -> " + repository.getPath());
        return 0;
    }
}
