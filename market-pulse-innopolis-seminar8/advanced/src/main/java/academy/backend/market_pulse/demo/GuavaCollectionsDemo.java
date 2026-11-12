package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Коллекции Guava, которых нет в стандартном JCF (см. «План семинара.md», семинар 5, этап 6):
 * иммутабельная копия {@link ImmutableMap} (в отличие от обёртки {@code Collections.unmodifiableMap})
 * и {@link Multimap} «один ключ → несколько значений» вместо ручного {@code Map<K, List<V>>}.
 * Не внедряется в проект — только демонстрация.
 */
public class GuavaCollectionsDemo {

    public static void main(String[] args) {
        Instrument sber = InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB);
        Instrument gazp = InstrumentFactories.create("STOCK", "GAZP", "Газпром", Currency.RUB);
        Quote sberQuote = new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.20"));
        Quote gazpQuote = new Quote(gazp, new BigDecimal("120.50"), new BigDecimal("-0.80"));

        // Иммутабельный seed для заглушки источника котировок:
        // изменить после создания нельзя.
        Map<String, Quote> seed = ImmutableMap.of(
                "SBER", sberQuote,
                "GAZP", gazpQuote);
        System.out.println("Иммутабельный seed: " + seed.keySet());

        // Multimap: один ключ (валюта) → несколько значений,
        // без ручного Map<K, List<V>>.
        Multimap<Currency, Instrument> byCurrency = ArrayListMultimap.create();
        byCurrency.put(Currency.RUB, sber);
        byCurrency.put(Currency.RUB, gazp);

        Collection<Instrument> rub = byCurrency.get(Currency.RUB); // [sber, gazp]
        System.out.println("Инструментов в RUB: " + rub.size());
    }
}
