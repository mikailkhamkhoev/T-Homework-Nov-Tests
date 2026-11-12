package academy.backend.market_pulse.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

class InstrumentFactoriesTest {

    @ParameterizedTest(name = "тип [{0}] -> {1}")
    @CsvSource({
            "STOCK, Stock",
            "BOND, Bond",
            "ETF, Etf",
            "stock, Stock",   // регистр не важен
            "Etf, Etf"
    })
    void createsInstrumentOfRequestedType(String type, String expectedClass) {
        Instrument instrument = InstrumentFactories.create(type, "TEST", "Тест", Currency.RUB);

        assertThat(instrument.getClass().getSimpleName()).isEqualTo(expectedClass);
        assertThat(instrument.getTicker()).isEqualTo("TEST");
        assertThat(instrument.getCurrency()).isEqualTo(Currency.RUB);
    }

    @Test
    void knownTypesMapToTheExpectedClasses() {
        assertThat(InstrumentFactories.create("STOCK", "A", "a", Currency.RUB)).isInstanceOf(Stock.class);
        assertThat(InstrumentFactories.create("BOND", "B", "b", Currency.RUB)).isInstanceOf(Bond.class);
        assertThat(InstrumentFactories.create("ETF", "C", "c", Currency.USD)).isInstanceOf(Etf.class);
    }

    @Test
    void unknownTypeIsRejectedWithAnExplanation() {
        assertThatThrownBy(() -> InstrumentFactories.create("CRYPTO", "BTC", "Bitcoin", Currency.USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CRYPTO");
    }
}
