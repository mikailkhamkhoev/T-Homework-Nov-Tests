package academy.backend.market_pulse.repository.store.impl;

import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (JSON, CSV, построчный текст); главное,
 * чтобы {@code load(save(quotes))} возвращал те же котировки. Проверьте себя тестом
 * {@code QuoteFileStoreContractTest} (замените в нём эталонную реализацию на свою).
 */
public class QuoteFileStoreSkeleton implements QuoteFileStore {

    @Override
    public void save(Path path, List<Quote> quotes) {
        // TODO: сохранить котировки в файл по указанному пути.
        //  Подсказка: на каждую котировку нужно сохранить тикер, тип и валюту инструмента (чтобы
        //  потом восстановить его через InstrumentFactories.create), а также цену и изменение за
        //  период — этого достаточно, чтобы собрать Quote обратно.
        throw new UnsupportedOperationException("TODO: реализовать сохранение котировок в файл");
    }

    @Override
    public List<Quote> load(Path path) {
        // TODO: прочитать котировки из файла и восстановить при старте.
        //  Если файла ещё нет (первый запуск) — вернуть пустой список, а не бросать исключение.
        throw new UnsupportedOperationException("TODO: реализовать чтение котировок из файла");
    }
}
