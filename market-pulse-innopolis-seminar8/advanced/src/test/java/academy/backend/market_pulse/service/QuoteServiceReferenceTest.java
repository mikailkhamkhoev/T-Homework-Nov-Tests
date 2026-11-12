package academy.backend.market_pulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Quote;


/** Эталон к {@code QuoteServiceTest}: логика кеша проверяется через взаимодействие с замоканным источником. */
@ExtendWith(MockitoExtension.class)
class QuoteServiceReferenceTest {

    @Mock
    private QuoteSource source;

    private QuoteService service;
    private Quote sber;

    @BeforeEach
    void setUp() {
        service = new QuoteService(source);
        sber = new Quote(new Etf("SBER", "Сбер", Currency.RUB, "-"), new BigDecimal("250.00"), new BigDecimal("1.20"));
    }

    @Test
    void repeatedRequestIsServedFromCache() {
        when(source.fetch("SBER")).thenReturn(Optional.of(sber));

        service.request("SBER");
        service.request("SBER");
        service.processAll();

        assertThat(service.cached("SBER")).contains(sber);
        verify(source, times(1)).fetch("SBER");
    }

    @Test
    void tickerIsNormalizedToUpperCaseBeforeTheSourceIsCalled() {
        when(source.fetch("SBER")).thenReturn(Optional.of(sber));

        Optional<Quote> quote = service.quoteFor("sber");

        assertThat(quote).contains(sber);
        verify(source).fetch("SBER");
    }

    @Test
    void unknownTickerIsNotCached() {
        when(source.fetch("XXXX")).thenReturn(Optional.empty());

        Optional<Quote> quote = service.quoteFor("XXXX");

        assertThat(quote).isEmpty();
        assertThat(service.cached("XXXX")).isEmpty();
    }

    @Test
    void nothingIsRequestedUntilProcessAllIsCalled() {
        service.request("SBER");

        verify(source, never()).fetch("SBER");
        verifyNoMoreInteractions(source);
    }
}
