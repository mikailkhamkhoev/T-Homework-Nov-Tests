package academy.backend.market_pulse.benchmark;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Последовательная и параллельная агрегация одного и того же стрима (см. «План семинара.md»,
 * семинар 6, этап 3 — Parallel Streams). Параметр {@code size} показывает границу, за которой имеет
 * смысл параллелизм: на малом {@code N} накладные на split/merge через {@code ForkJoinPool}
 * превышают выигрыш, и {@code parallelStream} проигрывает; выигрыш появляется только с ростом объёма
 * (и стоимости операции на элемент). Запускается через JMH-плагин IntelliJ IDEA либо
 * {@code org.openjdk.jmh.Main} из собранного jar.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class StreamParallelBenchmark {

    @Param({"100", "10000", "1000000"})
    private int size;

    private List<Quote> quotes;

    @Setup
    public void setup() {
        Instrument stock = InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB);
        Random random = new Random(42);
        quotes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BigDecimal price = BigDecimal.valueOf(100 + random.nextInt(100));
            BigDecimal change = BigDecimal.valueOf(random.nextInt(2000) - 1000, 2);
            quotes.add(new Quote(stock, price, change));
        }
    }

    @Benchmark
    public double sequential() {
        return quotes.stream()
                .map(Quote::getChangePercent)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
    }

    @Benchmark
    public double parallel() {
        return quotes.parallelStream()
                .map(Quote::getChangePercent)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
    }
}