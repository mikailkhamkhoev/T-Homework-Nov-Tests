package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.filter.FilterFactory;
import academy.backend.market_pulse.filter.InstrumentFilter;
import academy.backend.market_pulse.filter.PriceFilter;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "list", description = "Список инструментов")
@RequiredArgsConstructor
public class ListCommand implements Callable<Integer> {

    @Option(names = "--type", description = "Фильтр по типу инструмента")
    private String type;

    @Option(names = "--ticker", description = "Фильтр по подстроке в тикере")
    private String ticker;

    @Option(names = "--currency", description = "Фильтр по валюте инструмента")
    private Currency currency;

    @Option(names = "--price-op", description = "Оператор сравнения цены: GE, LE или EQ")
    private PriceFilter.Operator priceOperator;

    @Option(names = "--price", description = "Пороговое значение цены (дивидендная доходность акции)")
    private BigDecimal price;

    private final InstrumentRepository repository;

    @Override
    public Integer call() {
        InstrumentFilter filter = FilterFactory.create(type, ticker, currency, priceOperator, price);

        // Рефакторинг (семинар 6, этап 2.3): императивный цикл отбора заменён на декларативный
        // стрим. InstrumentFilter используется как Predicate через ссылку на метод; поведение
        // (тот же отбор и вывод) не меняется.
        repository.stream()
                .filter(filter::matches)
                .collect(Collectors.toList())
                .forEach(System.out::println);
        return 0;
    }
}
