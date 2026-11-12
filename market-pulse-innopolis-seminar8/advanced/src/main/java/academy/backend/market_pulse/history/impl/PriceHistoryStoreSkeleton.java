package academy.backend.market_pulse.history.impl;

import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (бинарный, текстовый и т.д.); главное,
 * чтобы каждый {@link #append} создавал НОВЫЙ файл (не переписывал предыдущий) и
 * {@link #load(Path, String)} возвращал все сохранённые точки по тикеру в порядке возрастания
 * времени. Проверьте себя тестом {@code PriceHistoryStoreContractTest} (замените в нём эталонную
 * реализацию на свою).
 */
public class PriceHistoryStoreSkeleton implements PriceHistoryStore {

    @Override
    public Path append(Path directory, PricePoint point) {
        // TODO: записать точку в новый файл внутри directory — существующие файлы не трогать.
        //  Подсказка: имя файла на основе point.timestamp() даёт сортировку по имени = сортировку
        //  по времени.
        throw new UnsupportedOperationException("TODO: реализовать запись точки истории цены");
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        // TODO: прочитать все точки по тикеру в порядке возрастания времени.
        //  Если для тикера ещё нет точек — вернуть пустой список, а не бросать исключение.
        throw new UnsupportedOperationException("TODO: реализовать чтение истории цены");
    }
}
