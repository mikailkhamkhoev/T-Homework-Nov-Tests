package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.factory.impl.StockFactory;
import academy.backend.market_pulse.model.Currency;
import jdk.jfr.Threshold;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест фильтра по цене: пишется вживую человеком. Эталон — {@code PriceFilterReferenceTest}.
 */
@Disabled("Пишется вживую")
class PriceFilterTest {
    private final static BigDecimal THRESHOLD = BigDecimal.valueOf(5);
    // NOTICE: техника — классы эквивалентности + граничные значения. Порог один (5.0), три класса
    // для акции: ниже порога, ровно на пороге, выше порога. Результат трёх операторов (GE/LE/EQ)
    // различается ТОЛЬКО на границе и рядом с ней — именно её проверяем в первую очередь.
    // NOTICE: отдельный класс эквивалентности — не акция (облигация, ETF): фильтр не проходят никогда,
    // независимо от оператора и порога.
    // NOTICE: одна таблица вместо девяти копий теста — @ParameterizedTest + @CsvSource
    // (оператор, дивидендная доходность, ожидаемый результат); проверка — assertThat(...).isEqualTo(...)
    // из AssertJ.
    // TODO: @ParameterizedTest @CsvSource: оператор, доходность акции, ожидаемое matches(...)
    // TODO: не-акция (Bond/Etf) -> false для любого оператора
    @ParameterizedTest
    @CsvSource({
            "LE, 4.99, true",
            "LE, 5.00, true",
            "LE, 5.01, false",
            "GE, 4.99, false",
            "GE, 5.00, true",
            "GE, 5.01, true",
            "EQ, 4.99, false",
            "EQ, 5.00, true",
            "EQ, 5.01, false",
    })
    void lessOrEqualThanThreshold(PriceFilter.Operator operator, double price, boolean expected) {
        // Given
        var stock = new StockFactory().create("SBER", "cбер", Currency.RUB, BigDecimal.valueOf(price));

        // When
        var mathces = new PriceFilter(operator, THRESHOLD).matches(stock);

        // Then
        assertThat(mathces).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BOND", "ETF"})
    void notStockPriceFilter(String type) {
        // Given
        var instrument = InstrumentFactories.create(type, "TICKER", "NAME", Currency.RUB);
        for (var operator : PriceFilter.Operator.values()) {
            // When
            var matches = new PriceFilter(operator, THRESHOLD).matches(instrument);
            // Then
            assertThat(matches).isEqualTo(false);
        }
    }
}
