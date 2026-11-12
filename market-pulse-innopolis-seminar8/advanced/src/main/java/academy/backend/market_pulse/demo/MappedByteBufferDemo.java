package academy.backend.market_pulse.demo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Демо сравнения для «MappedByteBuffer vs Files» (см. «План семинара.md», семинар 7, этап 10): {@code Files.readAllBytes} копирует весь файл в один {@code byte[]} в куче;
 * {@code FileChannel.map} отображает файл в память процесса — обслуживается страничным кешем ОС, а
 * не кучей JVM. Точный замер аллокаций — в
 * {@code benchmark.MappedFileReadingBenchmark} (JMH, {@code -prof gc}). Здесь — только
 * качественная демонстрация, что оба способа читают один и тот же файл и дают один и тот же
 * результат.
 *
 * <p>Тестовые данные — реальный большой текстовый файл (например, глава «Войны и мира»,
 * общественное достояние) нужно подготовить заранее: взять полный текст и нарезать на главу. Без
 * файла-аргумента демо создаёт синтетический файл подходящего размера, чтобы не падать на пустом
 * месте, — для содержательной демонстрации нужен настоящий текст.
 */
public class MappedByteBufferDemo {

    public static void main(String[] args) throws IOException {
        Path path = args.length > 0 ? Path.of(args[0]) : createSampleFile();
        long size = Files.size(path);

        System.out.println("Файл: " + path + " (" + size + " байт)");

        System.out.println();
        System.out.println("1. Files.readAllBytes — весь файл копируется в byte[] в куче:");
        byte[] allBytes = Files.readAllBytes(path);
        System.out.println("   Прочитано байт: " + allBytes.length);

        System.out.println();
        System.out.println("2. FileChannel.map — файл отображается в память процесса, без копирования в кучу:");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            byte[] firstLine = new byte[Math.min(64, mapped.remaining())];
            mapped.get(firstLine);
            System.out.println("   Отображено байт: " + size);
            System.out.println("   Первые байты: " + new String(firstLine, StandardCharsets.UTF_8).lines().findFirst().orElse(""));
        }
    }

    private static Path createSampleFile() {
        try {
            Path path = Files.createTempFile("mapped-byte-buffer-demo", ".txt");
            path.toFile().deleteOnExit();
            try (var writer = Files.newBufferedWriter(path)) {
                for (int i = 0; i < 200_000; i++) {
                    writer.write("Война и мир — заглушка строки для демонстрации размера файла.");
                    writer.newLine();
                }
            }
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
