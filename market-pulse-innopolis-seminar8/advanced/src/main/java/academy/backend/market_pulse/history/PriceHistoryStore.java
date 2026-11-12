package academy.backend.market_pulse.history;

import java.nio.file.Path;
import java.util.List;

/**
 * Хранение истории цен инструментов для последующего построения статистики. В отличие от
 * {@code QuoteFileStore} (перезаписывает один файл целиком), история — это последовательность
 * точек во времени: каждое сохранение добавляет новый файл, а не переписывает предыдущий.
 */
public interface PriceHistoryStore {

    /** Записывает точку в НОВЫЙ файл внутри {@code directory} — существующие файлы не трогаются. */
    Path append(Path directory, PricePoint point);

    /** Читает все сохранённые точки по тикеру в порядке возрастания времени. */
    List<PricePoint> load(Path directory, String ticker);
}
