package academy.backend.market_pulse.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Обобщённое хранилище «ключ → значение». Семинар 2: {@code InstrumentFactories} держал
 * регистр фабрик на {@link Map}; семинар 3: эта структура вынесена в обобщённый класс
 * (см. «План семинара.md», семинар 3, этап 6 — {@code Registry<K, V>}).
 *
 * @param <K> тип ключа
 * @param <V> тип значения
 */
public class Registry<K, V> {

    private final Map<K, V> items = new HashMap<>();

    public void register(K key, V value) {
        items.put(key, value);
    }

    /**
     * {@code Optional} вместо {@code null}: отсутствие значения заявлено в сигнатуре
     * (см. «План семинара.md», семинар 3, этап 7 — Optional).
     */
    public Optional<V> find(K key) {
        return Optional.ofNullable(items.get(key));
    }
}