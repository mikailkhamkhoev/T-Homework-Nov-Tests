package academy.backend.market_pulse.repository.store.impl;

import java.nio.file.Path;
import java.util.Set;

import academy.backend.market_pulse.repository.store.WatchlistStore;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (JSON, построчный текст и т.д.); главное,
 * чтобы {@code load(save(tickers))} возвращал тот же набор тикеров в том же порядке. Проверьте себя
 * тестом {@code WatchlistStoreContractTest} (замените в нём эталонную реализацию на свою).
 */
public class WatchlistStoreSkeleton implements WatchlistStore {

    @Override
    public void save(Path path, Set<String> tickers) {
        // TODO: сохранить тикеры watchlist-а в файл по указанному пути, сохранив порядок.
        throw new UnsupportedOperationException("TODO: реализовать сохранение watchlist-а в файл");
    }

    @Override
    public Set<String> load(Path path) {
        // TODO: прочитать тикеры watchlist-а из файла.
        //  Если файла ещё нет (первый запуск) — вернуть пустой набор, а не бросать исключение.
        throw new UnsupportedOperationException("TODO: реализовать чтение watchlist-а из файла");
    }
}
