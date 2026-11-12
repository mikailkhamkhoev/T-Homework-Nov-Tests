package academy.backend.market_pulse.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Тест сервиса котировок с замоканным источником: пишется вживую человеком. Эталон —
 * {@code QuoteServiceReferenceTest}.
 */
@Disabled("Пишется вживую")
class QuoteServiceTest {

    // NOTICE: QuoteService — единственный код с логикой кеша; источник котировок (QuoteSource) —
    // его внешняя зависимость, поэтому мокаем именно её: @ExtendWith(MockitoExtension.class),
    // @Mock QuoteSource, сервис создаём вручную через конструктор.
    // NOTICE: главное, что проверяем, — повторный запрос того же тикера НЕ ходит в источник снова:
    // verify(source, times(1)).fetch("SBER"). Это проверка взаимодействия, а не значения.
    // NOTICE: when(source.fetch("SBER")).thenReturn(Optional.of(quote)) — заглушка ответа; тикер
    // сервис нормализует к верхнему регистру, мок увидит уже "SBER".
    // TODO: два запроса одного тикера -> один вызов source.fetch
    // TODO: неизвестный тикер (Optional.empty()) -> в кеше пусто
    @Test
    void todo() {
    }
}
