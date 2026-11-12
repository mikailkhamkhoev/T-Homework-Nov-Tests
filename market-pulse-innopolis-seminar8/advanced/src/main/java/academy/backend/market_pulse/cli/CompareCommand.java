package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.service.QuoteService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Сравнение доходности инструментов по тикерам, по убыванию изменения цены (см. «План семинара.md»,
 * семинар 6, этап 2.2 — map + flatMap(Optional::stream) + sorted). {@code quoteFor} в {@code map} —
 * операция с побочным эффектом (пишет в кеш/очередь {@code QuoteService}); здесь это осознанно
 * допустимо, так как стрим последовательный.
 */
@Command(name = "compare", description = "Сравнить доходность инструментов по тикерам (по убыванию изменения цены)")
@RequiredArgsConstructor
public class CompareCommand implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Тикеры инструментов")
    private List<String> tickers;

    private final QuoteService quoteService;

    @Override
    public Integer call() {
        tickers.stream()
                .map(quoteService::quoteFor)          // String -> Optional<Quote>
                .flatMap(Optional::stream)            // отбрасываем ненайденные
                .sorted(Comparator.comparing(Quote::getChangePercent).reversed())
                .forEach(System.out::println);
        return 0;
    }
}