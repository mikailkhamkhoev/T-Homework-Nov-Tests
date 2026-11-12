package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.repository.store.WatchlistStore;

/**
 * ЭТАЛОН: хранение watchlist-а в формате JSON — плоский массив тикеров
 * ({@code ["SBER", "GAZP"]}), порядок добавления сохраняется.
 */
public class WatchlistStoreReference implements WatchlistStore {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void save(Path path, Set<String> tickers) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(path.toFile(), tickers);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить watchlist в " + path, e);
        }
    }

    @Override
    public Set<String> load(Path path) {
        if (!Files.exists(path)) {
            return Set.of();
        }
        try {
            List<String> tickers = objectMapper.readValue(path.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            return new LinkedHashSet<>(tickers);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать watchlist из " + path, e);
        }
    }
}
