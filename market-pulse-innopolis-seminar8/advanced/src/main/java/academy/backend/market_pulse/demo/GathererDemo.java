package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.gather.MovingAverageGatherer;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

/**
 * Демо Gatherer API (семинар 6, этап 4). На примере скользящих окон по changePercent:
 * <ul>
 *   <li>{@link Gatherers#windowSliding} — встроенный гейтерер, материализует окна-списки;</li>
 *   <li>{@link MovingAverageGatherer} — кастомный гейтерер из проекта (скользящее среднее «на лету»);</li>
 *   <li>{@link #slidingMax(int)} — кастомный гейтерер (максимум в окне), который встроенными
 *       операциями (map/filter/limit/max) не выразить.</li>
 * </ul>
 */
@SuppressWarnings("JavaPrintToLogpoint")
public class GathererDemo {

    static void main() {
        List<Quote> quotes = sampleQuotes();

        System.out.println("=============== исходные котировки ===============");
        quotes.forEach(quote -> System.out.printf("  %s: change=%5s%n",
                quote.getInstrument().getTicker(), quote.getChangePercent()));

        System.out.println("=============== windowSliding(3) — встроенный ===============");
        quotes.stream()
                .gather(Gatherers.windowSliding(3))
                .forEach(window -> System.out.println("  " + window));

        System.out.println("=============== MovingAverageGatherer.movingAverage(3) ===============");
        quotes.stream()
                .map(Quote::getChangePercent)
                .gather(MovingAverageGatherer.movingAverage(3))
                .forEach(sma -> System.out.println("  SMA = " + sma));

        System.out.println("=============== slidingMax(3) — кастомный ===============");
        quotes.stream()
                .gather(slidingMax(3))
                .forEach(max -> System.out.println("  максимум окна = " + max));
    }

    /**
     * Кастомный гейтерер: максимум {@code changePercent} в скользящем окне из последних {@code k}
     * котировок. Состояние — окно значений и текущий максимум; максимум обновляется
     * инкрементально и пересчитывается лишь когда из окна вылетает актуальный максимум.
     */
    private static Gatherer<Quote, WindowMax, BigDecimal> slidingMax(int k) {
        // Уже готовый Gatherer.ofSequential
        return Gatherer.ofSequential(
                // Cоздаем наш начальный State
                WindowMax::new,
                // Это наша функция Integrator
                (state, quote, downstream) -> {
                    BigDecimal value = quote.getChangePercent();
                    // Добавляем элемент в наше скользящее окно
                    state.window.addLast(value);
                    if (state.window.size() > k) {
                        // Если окно превышает размер, то выкидываем первый элемент
                        BigDecimal removed = state.window.removeFirst();
                        // Если первый был максимальным, то пересчитываем максимум
                        if (removed.equals(state.max)) {
                            state.max = state.window.stream()
                                    .max(BigDecimal::compareTo)
                                    .orElse(BigDecimal.valueOf(Long.MIN_VALUE));
                        }
                    }
                    state.max = state.max.max(value);
                    // Вызываем downstream на результате
                    return downstream.push(state.max);  // Возвращаем boolean
                });
    }

    /* Наш State */
    private static final class WindowMax {
        private final Deque<BigDecimal> window = new ArrayDeque<>();
        private BigDecimal max = BigDecimal.valueOf(Long.MIN_VALUE);
    }

    private static List<Quote> sampleQuotes() {
        Instrument sber = InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB);
        return List.of(
                new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.2")),
                new Quote(sber, new BigDecimal("249.00"), new BigDecimal("-0.8")),
                new Quote(sber, new BigDecimal("252.00"), new BigDecimal("2.3")),
                new Quote(sber, new BigDecimal("253.00"), new BigDecimal("0.5")),
                new Quote(sber, new BigDecimal("251.00"), new BigDecimal("1.0")),
                new Quote(sber, new BigDecimal("261.00"), new BigDecimal("-0.8")),
                new Quote(sber, new BigDecimal("257.00"), new BigDecimal("-0.5")),
                new Quote(sber, new BigDecimal("253.00"), new BigDecimal("1.5")),
                new Quote(sber, new BigDecimal("250.50"), new BigDecimal("-1.1")));
    }
}