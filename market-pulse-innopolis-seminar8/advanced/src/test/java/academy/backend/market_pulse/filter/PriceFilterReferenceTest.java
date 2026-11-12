package academy.backend.market_pulse.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import academy.backend.market_pulse.filter.PriceFilter.Operator;
import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Stock;

/** Эталон к {@code PriceFilterTest}: классы эквивалентности и границы, одна таблица на все операторы. */
class PriceFilterReferenceTest {

    private static final BigDecimal THRESHOLD = new BigDecimal("5.0");

    private static Stock stockWithYield(String dividendYield) {
        return new Stock("SBER", "Сбер", Currency.RUB, "Финансы", new BigDecimal(dividendYield));
    }

    @DisplayName("Акция: результат зависит от оператора и положения доходности относительно порога")
    @ParameterizedTest(name = "{0} при доходности {1} -> {2}")
    @CsvSource({
            // ниже порога
            "GE, 4.9, false", "LE, 4.9, true", "EQ, 4.9, false",
            // ровно на пороге (граница)
            "GE, 5.0, true", "LE, 5.0, true", "EQ, 5.0, true",
            // выше порога
            "GE, 5.1, true", "LE, 5.1, false", "EQ, 5.1, false",
            // то же число в другой записи: сравнение по значению, а не по scale
            "EQ, 5.00, true"
    })
    void stockIsMatchedByDividendYield(Operator operator, String dividendYield, boolean expected) {
        PriceFilter filter = new PriceFilter(operator, THRESHOLD);

        assertThat(filter.matches(stockWithYield(dividendYield))).isEqualTo(expected);
    }

    @DisplayName("Не акция: фильтр не проходит ни при каком операторе")
    @ParameterizedTest
    @EnumSource(Operator.class)
    void nonStockIsNeverMatched(Operator operator) {
        PriceFilter filter = new PriceFilter(operator, THRESHOLD);

        assertThat(filter.matches(new Bond("OFZ26233", "ОФЗ", Currency.RUB, new BigDecimal("8.5"), 2036))).isFalse();
        assertThat(filter.matches(new Etf("FXUS", "FinEx", Currency.USD, "S&P 500"))).isFalse();
    }

    @Test
    void zeroThresholdMatchesAnyNonNegativeYield() {
        PriceFilter filter = new PriceFilter(Operator.GE, BigDecimal.ZERO);

        assertThat(filter.matches(stockWithYield("0"))).isTrue();
    }
}
