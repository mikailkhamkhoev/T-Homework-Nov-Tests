package academy.backend.market_pulse.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.exception.DuplicateTickerException;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

class InMemoryInstrumentRepositoryTest {

    private InMemoryInstrumentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInstrumentRepository();
    }

    @Test
    void startsWithTheSeedInstruments() {
        assertThat(repository.stream())
                .extracting(Instrument::getTicker)
                .containsExactlyInAnyOrder("SBER", "OFZ26233", "FXUS");
    }

    @Test
    void addedInstrumentCanBeFoundByTicker() {
        repository.add(new Stock("GAZP", "Газпром", Currency.RUB, "Энергетика", new BigDecimal("7.0")));

        assertThat(repository.findByTicker("GAZP"))
                .get()
                .extracting(Instrument::getName)
                .isEqualTo("Газпром");
    }

    @Test
    void searchByTickerIgnoresCase() {
        assertThat(repository.findByTicker("sber")).isPresent();
    }

    @Test
    void unknownTickerIsNotFound() {
        assertThat(repository.findByTicker("NOPE")).isEmpty();
    }

    @Test
    void duplicateTickerIsRejectedAndTheRepositoryIsLeftUntouched() {
        Stock duplicate = new Stock("SBER", "Другой Сбер", Currency.RUB, "Финансы", BigDecimal.ONE);

        assertThatThrownBy(() -> repository.add(duplicate))
                .isInstanceOf(DuplicateTickerException.class)
                .hasMessageContaining("SBER")
                .extracting("ticker").isEqualTo("SBER");
        assertThat(repository.findByTicker("SBER")).get().extracting(Instrument::getName).isEqualTo("Сбер");
        assertThat(repository.stream().count()).isEqualTo(3);
    }
}
