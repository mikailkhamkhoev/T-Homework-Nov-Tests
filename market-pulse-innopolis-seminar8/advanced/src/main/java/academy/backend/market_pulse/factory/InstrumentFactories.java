package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.util.Registry;

import java.util.ServiceLoader;

/**
 * Реестр фабрик инструментов. Реализации {@link InstrumentFactory} не регистрируют себя вручную —
 * они обнаруживаются через {@link ServiceLoader} по записи в
 * {@code META-INF/services/academy.backend.market_pulse.factory.InstrumentFactory}. Добавление
 * нового типа инструмента не требует правки этого класса — только новая реализация и строка в
 * файле сервиса.
 */
public final class InstrumentFactories {

    private static final Registry<String, InstrumentFactory> REGISTRY = loadRegistry();

    private InstrumentFactories() {
    }

    public static Instrument create(String type, String ticker, String name, Currency currency) {
        return REGISTRY.find(type.toUpperCase())
                .map(factory -> factory.create(ticker, name, currency))
                .orElseThrow(() -> new IllegalArgumentException("Unknown instrument type: " + type));
    }

    private static Registry<String, InstrumentFactory> loadRegistry() {
        Registry<String, InstrumentFactory> registry = new Registry<>();
        for (InstrumentFactory factory : ServiceLoader.load(InstrumentFactory.class)) {
            registry.register(factory.type().toUpperCase(), factory);
        }
        return registry;
    }
}