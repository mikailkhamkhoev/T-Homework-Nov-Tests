package academy.backend.market_pulse.repository.store;

import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.model.Quote;

/**
 * Контракт хранения котировок в файле (см. «Требования к файловому хранилищу.md», семинар 7) —
 * самостоятельная работа, вне тайминга занятия. Формат файла не навязывается; важен только
 * контракт: что сохранили, то и прочитали.
 */
public interface QuoteFileStore {

    /** Полностью перезаписывает файл по пути {@code path} текущим набором котировок. */
    void save(Path path, List<Quote> quotes);

    /** Читает котировки из файла по пути {@code path}; пустой список, если файла ещё нет. */
    List<Quote> load(Path path);
}
