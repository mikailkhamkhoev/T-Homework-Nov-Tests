package academy.backend.market_pulse.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * {@link WatchService} отслеживает появление/изменение файла с котировками и инвалидирует кеш
 * {@link FileBackedQuoteRepository} (см. «Требования к файловому хранилищу.md», семинар 7).
 * {@link WatchService} следит за каталогом, а не за отдельным файлом (так устроено API), поэтому
 * события на другие файлы того же каталога явно отфильтровываются.
 */
public class QuoteFileWatcher {

    private final FileBackedQuoteRepository repository;
    private Thread watcherThread;

    public QuoteFileWatcher(FileBackedQuoteRepository repository) {
        this.repository = repository;
    }

    /** Запускает фоновый поток слежения за файлом. Демон — не держит JVM от завершения CLI-команды. */
    public void start() {
        Path directory = repository.getPath().toAbsolutePath().getParent();
        Path fileName = repository.getPath().getFileName();

        watcherThread = new Thread(() -> watch(directory, fileName), "quote-file-watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private void watch(Path directory, Path fileName) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Files.createDirectories(directory); // каталог может не существовать при первом запуске
            directory.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take(); // блокируется, пока в каталоге ничего не произошло
                for (WatchEvent<?> event : key.pollEvents()) {
                    Object context = event.context();
                    if (context instanceof Path changed && changed.equals(fileName)) {
                        repository.invalidate();
                    }
                }
                if (!key.reset()) {
                    break; // каталог стал недоступен (например, удалён) — дальше следить не за чем
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось запустить отслеживание файла котировок", e);
        }
    }
}
