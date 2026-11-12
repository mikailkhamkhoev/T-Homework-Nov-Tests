package academy.backend.market_pulse.repository;

import academy.backend.market_pulse.model.Instrument;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Хранилище инструментов. Выделено в интерфейс (в отличие от базового трека) — нужен для
 * JDK Dynamic Proxy и CGLIB, которым для работы требуется тип, отдельный от конкретной
 * реализации.
 */
public interface InstrumentRepository extends Iterable<Instrument> {

    void add(Instrument instrument);

    /**
     * {@code Optional} вместо {@code null}: отсутствие значения заявлено в сигнатуре
     */
    Optional<Instrument> findByTicker(String ticker);

    /**
     * Потоковый доступ к инструментам — вход в Stream API для декларативной фильтрации, сортировки
     * и агрегации (см. «План семинара.md», семинар 6, этап 2.3).
     */
    Stream<Instrument> stream();
}
