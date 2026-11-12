package academy.backend.market_pulse.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Контракт для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7):
 * каждое сохранение создаёт новый файл (не переписывает предыдущий), чтение отдаёт точки по
 * тикеру в порядке возрастания времени. Чтобы проверить свою реализацию — скопируйте этот файл,
 * замените {@link #createStore} на свой класс (реализующий {@link PriceHistoryStore}) и запустите
 * тест.
 */
public abstract class PriceHistoryStoreContractTest {

    protected abstract PriceHistoryStore createStore();

    @Test
    void eachAppendCreatesANewFile(@TempDir Path tempDir) {
        PriceHistoryStore store = createStore();
        PricePoint first = new PricePoint("SBER", Instant.ofEpochMilli(1_000), new BigDecimal("250.00"));
        PricePoint second = new PricePoint("SBER", Instant.ofEpochMilli(2_000), new BigDecimal("251.50"));

        Path firstFile = store.append(tempDir, first);
        Path secondFile = store.append(tempDir, second);

        assertTrue(Files.exists(firstFile));
        assertTrue(Files.exists(secondFile));
        assertEquals(firstFile.getParent(), secondFile.getParent());

        List<PricePoint> loaded = store.load(tempDir, "SBER");
        assertEquals(List.of(first, second), loaded);
    }

    @Test
    void loadingUnknownTickerReturnsEmptyList(@TempDir Path tempDir) {
        PriceHistoryStore store = createStore();

        assertTrue(store.load(tempDir, "UNKNOWN").isEmpty());
    }
}
