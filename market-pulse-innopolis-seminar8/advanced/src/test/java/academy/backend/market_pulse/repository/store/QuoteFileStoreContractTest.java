package academy.backend.market_pulse.repository.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;

/**
 * Контракт для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7): что
 * сохранили, то и прочитали. Чтобы проверить свою реализацию — скопируйте этот файл, замените
 * {@link #createStore} на свой класс (реализующий {@link QuoteFileStore}) и запустите тест.
 */
public abstract class QuoteFileStoreContractTest {

    protected abstract QuoteFileStore createStore();

    @Test
    void savedQuotesComeBackUnchanged(@TempDir Path tempDir) {
        QuoteFileStore store = createStore();
        Path path = tempDir.resolve("quotes.json");

        Instrument sber = InstrumentFactories.create("STOCK", "SBER", "Сбер", Currency.RUB);
        Instrument ofz = InstrumentFactories.create("BOND", "OFZ26233", "ОФЗ 26233", Currency.RUB);
        List<Quote> quotes = List.of(
                new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.20")),
                new Quote(ofz, new BigDecimal("780.00"), new BigDecimal("-0.15")));

        store.save(path, quotes);
        List<Quote> loaded = store.load(path);

        assertEquals(2, loaded.size());
        assertEquals("SBER", loaded.get(0).getInstrument().getTicker());
        assertEquals("STOCK", loaded.get(0).getInstrument().getType());
        assertEquals(Currency.RUB, loaded.get(0).getInstrument().getCurrency());
        assertEquals(0, new BigDecimal("250.00").compareTo(loaded.get(0).getPrice()));
        assertEquals("OFZ26233", loaded.get(1).getInstrument().getTicker());
    }

    @Test
    void loadingMissingFileReturnsEmptyList(@TempDir Path tempDir) {
        QuoteFileStore store = createStore();
        Path path = tempDir.resolve("does-not-exist.json");

        assertTrue(store.load(path).isEmpty());
    }
}
