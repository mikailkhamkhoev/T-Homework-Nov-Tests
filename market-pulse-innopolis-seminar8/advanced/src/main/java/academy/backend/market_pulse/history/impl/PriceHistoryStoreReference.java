package academy.backend.market_pulse.history.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

/**
 * ЭТАЛОН: бинарный формат на {@link DataOutputStream}/{@link DataInputStream} — три поля подряд,
 * без разделителей (ticker, epoch-миллисекунды, цена строкой — {@link BigDecimal} через
 * {@code writeUTF}, чтобы не терять точность, в отличие от {@code writeDouble}). Один файл — одна
 * точка; имя файла — метка времени в миллисекундах, поэтому сортировка по имени = сортировка по
 * времени.
 */
public class PriceHistoryStoreReference implements PriceHistoryStore {

    @Override
    public Path append(Path directory, PricePoint point) {
        Path tickerDir = directory.resolve(point.ticker());
        long epochMillis = point.timestamp().toEpochMilli();
        Path file = tickerDir.resolve(epochMillis + ".bin");
        try {
            Files.createDirectories(tickerDir);
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
                out.writeUTF(point.ticker());
                out.writeLong(epochMillis);
                out.writeUTF(point.price().toPlainString());
            }
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать историю цены в " + file, e);
        }
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        Path tickerDir = directory.resolve(ticker);
        if (!Files.isDirectory(tickerDir)) {
            return List.of();
        }
        try (var files = Files.list(tickerDir)) {
            List<Path> sorted = files
                    .filter(path -> path.toString().endsWith(".bin"))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();
            List<PricePoint> points = new ArrayList<>(sorted.size());
            for (Path file : sorted) {
                points.add(readPoint(file));
            }
            return points;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать историю цены из " + tickerDir, e);
        }
    }

    private PricePoint readPoint(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            String ticker = in.readUTF();
            long epochMillis = in.readLong();
            BigDecimal price = new BigDecimal(in.readUTF());
            return new PricePoint(ticker, Instant.ofEpochMilli(epochMillis), price);
        }
    }
}
