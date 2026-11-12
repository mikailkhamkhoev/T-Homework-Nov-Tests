package academy.backend.market_pulse.service;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Quote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Тест сервиса котировок с замоканным источником: пишется вживую человеком. Эталон —
 * {@code QuoteServiceReferenceTest}.
 */
@ExtendWith(MockitoExtension.class)
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

    @Mock
    private QuoteSource source;

    private QuoteService service;

    @BeforeEach
    void setUp() {
        service = new QuoteService(source);
    }

    @Test
    void cachesRepeatedTickerRequest() {
        // Given
        var quote = new Quote(new Etf("SBER", "Сбер", Currency.RUB, "-"), BigDecimal.valueOf(250), BigDecimal.ONE);
        when(source.fetch("SBER")).thenReturn(Optional.of(quote));

        // When
        service.quoteFor("sber");
        var secondResult = service.quoteFor("SBER");

        // Then
        assertThat(secondResult).contains(quote);
        verify(source, times(1)).fetch("SBER");
    }

    @Test
    void doesNotCacheUnknownTicker() {
        // Given
        when(source.fetch("NAME")).thenReturn(Optional.empty());

        // When
        var result = service.quoteFor("NAME");

        // Then
        assertThat(result).isEmpty();
        assertThat(service.cached("NAME")).isEmpty();
    }
}
