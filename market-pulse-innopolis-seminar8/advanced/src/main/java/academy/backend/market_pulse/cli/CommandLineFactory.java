package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.config.AppConfig;
import picocli.CommandLine;

/**
 * Сборка {@link CommandLine} из готовых зависимостей {@link AppConfig}. Команды регистрируются
 * вручную через {@code addSubcommand}, а не через атрибут {@code subcommands} аннотации
 * {@code @Command}: у команд нет конструктора без аргументов, picocli не смог бы создать их
 * самостоятельно по одному классу — каждой нужны уже готовые зависимости.
 */
public final class CommandLineFactory {

    private CommandLineFactory() {
    }

    public static CommandLine create(AppConfig config) {
        return new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(config.getInstrumentRepository()))
                .addSubcommand(new AddCommand(config.getInstrumentRepository()))
                .addSubcommand(new ListCommand(config.getInstrumentRepository()))
                .addSubcommand(new WatchCommand(config.getWatchlist(), config.getWatchlistStore(),
                        config.getWatchlistPath()))
                .addSubcommand(new QuoteCommand(config.getQuoteService()))
                .addSubcommand(new StatsCommand(config.getQuoteSource()))
                .addSubcommand(new CompareCommand(config.getQuoteService()))
                .addSubcommand(new MoversCommand(config.getQuoteSource()))
                .addSubcommand(new HistoryCommand(config.getPriceHistory()))
                .addSubcommand(new UpdateCommand(config.getApiClient(), config.getWatchlist(),
                        config.getFileBackedQuoteRepository(), config.getPriceHistoryStore(),
                        config.getHistoryDirectory()));
    }
}
