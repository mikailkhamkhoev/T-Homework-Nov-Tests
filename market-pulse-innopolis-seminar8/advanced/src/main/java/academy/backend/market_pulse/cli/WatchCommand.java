package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.store.WatchlistStore;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "watch", description = "Отслеживать инструмент: добавить тикер в watchlist или показать текущий список")
@RequiredArgsConstructor
public class WatchCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Тикер инструмента (без аргумента — показать watchlist)")
    private String ticker;

    private final Watchlist watchlist;
    private final WatchlistStore watchlistStore;
    private final Path watchlistPath;

    @Override
    public Integer call() {
        if (ticker != null) {
            boolean added = watchlist.add(ticker);
            if (added) {
                // Сохраняем сразу, а не по выходу из CLI: процесс завершается после каждой
                // команды, отложенное сохранение просто никогда бы не произошло.
                watchlistStore.save(watchlistPath, watchlist.tickers());
            }
            System.out.println(added
                    ? "Добавлено в watchlist: " + ticker.toUpperCase()
                    : "Уже отслеживается: " + ticker.toUpperCase());
        }
        System.out.println("Watchlist: " + watchlist.tickers());
        return 0;
    }
}
