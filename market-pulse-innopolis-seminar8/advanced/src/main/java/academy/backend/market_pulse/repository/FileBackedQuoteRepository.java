package academy.backend.market_pulse.repository;

import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Кеширующий доступ к котировкам, сохранённым на диске (см. «Требования к файловому хранилищу.md»,
 * семинар 7). Читает файл один раз и держит результат в памяти — {@link #all()} не ходит на диск на
 * каждый вызов, а только после {@link #invalidate()}, вызываемого {@link QuoteFileWatcher} при
 * изменении файла.
 */
public class FileBackedQuoteRepository {

    private final QuoteFileStore store;
    private final Path path;
    private volatile List<Quote> cache;

    public FileBackedQuoteRepository(QuoteFileStore store, Path path) {
        this.store = store;
        this.path = path;
    }

    public List<Quote> all() {
        List<Quote> snapshot = cache;
        if (snapshot == null) {
            synchronized (this) {
                snapshot = cache;
                if (snapshot == null) {
                    snapshot = store.load(path);
                    cache = snapshot;
                }
            }
        }
        return snapshot;
    }

    /** Перезаписывает файл и сразу обновляет кеш — следующий {@link #all()} не идёт на диск. */
    public void save(List<Quote> quotes) {
        store.save(path, quotes);
        cache = quotes;
    }

    /**
     * Сбрасывает кеш: следующий {@link #all()} перечитает файл с диска. Вызывается
     * {@link QuoteFileWatcher} при обнаружении внешнего изменения файла.
     */
    public void invalidate() {
        cache = null;
    }

    public Path getPath() {
        return path;
    }
}
