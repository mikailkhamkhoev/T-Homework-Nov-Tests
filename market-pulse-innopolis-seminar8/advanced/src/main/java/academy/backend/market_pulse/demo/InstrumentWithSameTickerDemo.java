package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.cli.SearchCommand;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine;

import java.util.Arrays;

/**
 * Иллюстрация проблемы в логике при добавлении инструментов с одинаковым тикером.
 * Инструмент добавлен, но найти его нельзя.
 */
@SuppressWarnings("JavaPrintToLogpoint")
public class InstrumentWithSameTickerDemo {
    static void main() {
        InstrumentRepository repository = new InMemoryInstrumentRepository();
        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository));
        System.out.println("===============add instruments==============");
        var args = new String[]{"add", "STOCK", "SBER", "Сбербанк", "RUB"};
        System.out.println(Arrays.toString(args));
        cli.execute(args);

        args = new String[]{"list"};
        System.out.println("===============list instruments==============");
        cli.execute(args);

        args = new String[]{"search", "SBER"};
        System.out.println("===============search instruments==============");
        cli.execute(args);
    }
}
