package academy.backend.market_pulse.repository.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Контракт для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7): что
 * сохранили, то и прочитали, порядок добавления сохраняется. Чтобы проверить свою реализацию —
 * скопируйте этот файл, замените {@link #createStore} на свой класс (реализующий
 * {@link WatchlistStore}) и запустите тест.
 */
public abstract class WatchlistStoreContractTest {

    protected abstract WatchlistStore createStore();

    @Test
    void savedTickersComeBackInInsertionOrder(@TempDir Path tempDir) {
        WatchlistStore store = createStore();
        Path path = tempDir.resolve("watchlist.json");
        Set<String> tickers = new LinkedHashSet<>(Set.of());
        tickers.add("SBER");
        tickers.add("GAZP");

        store.save(path, tickers);
        Set<String> loaded = store.load(path);

        assertEquals(tickers, loaded);
    }

    @Test
    void loadingMissingFileReturnsEmptySet(@TempDir Path tempDir) {
        WatchlistStore store = createStore();
        Path path = tempDir.resolve("does-not-exist.json");

        assertTrue(store.load(path).isEmpty());
    }
}
