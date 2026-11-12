package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.service.QuoteService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "quote", description = "Получить котировки по одному или нескольким тикерам")
@RequiredArgsConstructor
public class QuoteCommand implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Тикеры инструментов")
    private List<String> tickers;

    private final QuoteService quoteService;

    @Override
    public Integer call() {
        tickers.forEach(quoteService::request);
        quoteService.processAll();
        for (String ticker : tickers) {
            quoteService.cached(ticker).ifPresentOrElse(
                    quote -> System.out.println(quote),
                    () -> System.out.println("Котировка не найдена: " + ticker.toUpperCase()));
        }
        return 0;
    }
}
