package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.service.QuoteSource;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Сводка по каталогу котировок: распределение по типу и валюте, средняя доходность и суммарная
 * цена (см. «План семинара.md», семинар 6, этап 2.1 — агрегация через Collectors). Независимые
 * агрегаты — отдельные конвейеры над одним источником.
 */
@Command(name = "stats", description = "Сводка по котировкам: распределение по типу и валюте, средняя доходность")
@RequiredArgsConstructor
public class StatsCommand implements Callable<Integer> {

    private final QuoteSource source;

    @Override
    public Integer call() {
        Collection<Quote> quotes = source.all();

        Map<String, Long> byType = quotes.stream()
                .collect(Collectors.groupingBy(
                        quote -> quote.getInstrument().getClass().getSimpleName(),
                        Collectors.counting()));

        Map<Currency, Long> byCurrency = quotes.stream()
                .collect(Collectors.groupingBy(
                        quote -> quote.getInstrument().getCurrency(),
                        Collectors.counting()));

        // Средняя доходность — через mapToDouble: перевод BigDecimal в double теряет точность,
        // но для отображаемого процента это приемлемо.
        double avgChange = quotes.stream()
                .mapToDouble(quote -> quote.getChangePercent().doubleValue())
                .average()
                .orElse(0.0);

        // Денежная сумма — через reduce над BigDecimal, а не mapToDouble().sum(): точность сохраняется.
        BigDecimal totalPrice = quotes.stream()
                .map(Quote::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        byType.forEach((type, count) -> System.out.println(type + ": " + count));
        byCurrency.forEach((currency, count) -> System.out.println(currency + ": " + count));
        System.out.printf("Средняя доходность: %.2f%%%n", avgChange);
        System.out.println("Суммарная цена инструментов: " + totalPrice);
        return 0;
    }
}