package academy.backend.market_pulse.benchmark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Замер для «MappedByteBuffer vs Files» (см. «План семинара.md», семинар 7, вне тайминга занятия).
 * Запускать с {@code -prof gc} — время чтения у обоих подходов сопоставимо (оба читают один и тот
 * же файл с диска), а вот аллокации в куче отличаются на порядки: {@code Files.readAllBytes}
 * выделяет {@code byte[]} размером с файл на каждый вызов, {@code FileChannel.map} — нет (страницы
 * файла обслуживает кеш страниц ОС).
 *
 * <p>Тестовый файл — синтетический (см. {@link #setup()}): для содержательной демонстрации на
 * занятии нужен настоящий большой текст (например, глава «Войны и мира», общественное достояние) —
 * подготовить заранее и передать путь явным образом вместо {@link #createSampleFile()}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MappedFileReadingBenchmark {

    private Path path;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        path = createSampleFile();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        Files.deleteIfExists(path);
    }

    @Benchmark
    public void readAllBytes(Blackhole blackhole) throws IOException {
        blackhole.consume(Files.readAllBytes(path));
    }

    @Benchmark
    public void mappedByteBuffer(Blackhole blackhole) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            // Проход по страницам, чтобы они реально подгрузились (map() сам по себе ленив) —
            // без этого сравнение было бы нечестным: читаем не меньше данных, чем readAllBytes.
            long checksum = 0;
            while (buffer.hasRemaining()) {
                checksum += buffer.get();
            }
            blackhole.consume(checksum);
        }
    }

    private static Path createSampleFile() throws IOException {
        Path file = Files.createTempFile("mapped-file-benchmark", ".txt");
        try (var writer = Files.newBufferedWriter(file)) {
            for (int i = 0; i < 500_000; i++) {
                writer.write("Война и мир — заглушка строки для замера, не настоящий текст романа.");
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }
}
