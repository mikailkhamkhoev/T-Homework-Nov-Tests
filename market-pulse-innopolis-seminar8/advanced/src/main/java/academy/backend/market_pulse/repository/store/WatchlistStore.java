package academy.backend.market_pulse.repository.store;

import java.nio.file.Path;
import java.util.Set;

/**
 * Персистентность watchlist-а: какие тикеры отслеживает пользователь должно переживать перезапуск
 * CLI, а не жить только в памяти процесса.
 */
public interface WatchlistStore {

    void save(Path path, Set<String> tickers);

    Set<String> load(Path path);
}
