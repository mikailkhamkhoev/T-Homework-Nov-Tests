package academy.backend.market_pulse.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class InstrumentTest {

    @Nested
    @DisplayName("Создание инструмента")
    class Creation {

        @ParameterizedTest(name = "тикер [{0}] не допускается")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void blankTickerIsRejected(String ticker) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Etf(ticker, "FinEx", Currency.USD, "S&P 500"))
                    .withMessageContaining("Ticker");
        }

        @Test
        void validInstrumentKeepsItsFields() {
            Instrument etf = new Etf("FXUS", "FinEx FXUS", Currency.USD, "S&P 500");

            assertThat(etf.getTicker()).isEqualTo("FXUS");
            assertThat(etf.getCurrency()).isEqualTo(Currency.USD);
            assertThat(etf.getType()).isEqualTo("ETF");
            assertThat(etf).hasToString("FXUS — FinEx FXUS (ETF, отслеживает индекс: S&P 500)");
        }
    }

    @Nested
    @DisplayName("Дивиденды акции")
    class Dividends {

        @ParameterizedTest(name = "цена {0} и доходность {1}% -> {2}")
        @CsvSource({
                "250.00, 6.5, 16.25",
                "100.00, 0, 0.00",
                "100.00, 10, 10.00",
                "33.33, 3, 1.00",      // 0.9999 округляется до 1.00 (HALF_UP)
                "10.00, 0.05, 0.01"    // 0.005 -> вверх
        })
        void dividendsAreRoundedHalfUpToTwoDigits(String price, String yield, String expected) {
            Stock stock = new Stock("SBER", "Сбер", Currency.RUB, "Финансы", new BigDecimal(yield));

            assertThat(stock.getDividends(new BigDecimal(price))).isEqualByComparingTo(expected);
        }
    }
}
