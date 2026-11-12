package academy.backend.market_pulse.util;

import academy.backend.market_pulse.model.Instrument;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Обобщённые утилиты для работы со списками инструментов (см. «План семинара.md»,
 * семинар 3, этапы 5–6 — generics, bounded types, wildcard).
 */
public final class InstrumentUtils {

    private InstrumentUtils() {
    }

    /**
     * Возвращает первые {@code n} элементов, отсортированных по тикеру.
     * Bounded type parameter {@code <T extends Instrument>}: внутри generic-кода доступен
     * метод {@link Instrument#getTicker()}.
     */
    public static <T extends Instrument> List<T> topByTicker(List<T> items, int n) {
        List<T> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(Instrument::getTicker));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    /**
     * Сортирует список переданным компаратором. {@code Comparator<? super T>} по PECS —
     * компаратор «потребляет» элементы: компаратор, умеющий сравнивать более общий тип,
     * годится и для его подтипа (например, {@code Comparator<Instrument>} для {@code List<Stock>}).
     */
    public static <T> void sortWith(List<T> items, Comparator<? super T> comparator) {
        items.sort(comparator);
    }
}