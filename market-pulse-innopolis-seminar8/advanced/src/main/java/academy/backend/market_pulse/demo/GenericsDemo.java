package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.util.InstrumentUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generics, bounded types и wildcard (см. «План семинара.md», семинар 3, этап 6).
 * Демонстрирует {@code ? super T}: компаратор для {@link Instrument} принимается там, где
 * ожидается компаратор для {@link Stock} — {@code List<Stock>} не является подтипом
 * {@code List<Instrument>}, но {@code Comparator<? super Stock>} это позволяет.
 */
public class GenericsDemo {

    public static void main(String[] args) {
        List<Stock> stocks = new ArrayList<>(List.of(
                new Stock("YNDX", "Яндекс", Currency.RUB, "Технологии", BigDecimal.ZERO),
                new Stock("SBER", "Сбер", Currency.RUB, "Финансы", new BigDecimal("6.5")),
                new Stock("GAZP", "Газпром", Currency.RUB, "Энергетика", new BigDecimal("8.0"))
        ));

        InstrumentUtils.sortWith(stocks, Comparator.comparing(Instrument::getTicker));
        System.out.println("Отсортировано sortWith(Comparator<Instrument>): " + stocks);

        List<Stock> top = InstrumentUtils.topByTicker(stocks, 2);
        System.out.println("topByTicker(2): " + top);
    }
}