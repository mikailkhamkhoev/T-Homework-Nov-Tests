package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.cli.CommandLineFactory;
import academy.backend.market_pulse.config.AppConfig;
import picocli.CommandLine;

/**
 * Точка входа CLI. Сборка зависимостей — в {@link AppConfig} (композиционный корень), сборка
 * {@link CommandLine} — в {@link CommandLineFactory}; сам {@code Main} ничего не создаёт и не
 * знает деталей, только запускает.
 */
public class Main {

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        CommandLine cli = CommandLineFactory.create(config);

        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
