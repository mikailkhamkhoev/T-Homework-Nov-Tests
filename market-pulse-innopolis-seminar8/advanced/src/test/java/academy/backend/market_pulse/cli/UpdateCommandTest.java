package academy.backend.market_pulse.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.FileBackedQuoteRepository;

/** Команда {@code update} целиком на моках: клиент, файловое хранилище и история — внешние зависимости. */
@ExtendWith(MockitoExtension.class)
class UpdateCommandTest {

    private static final Path HISTORY_DIR = Path.of("data", "history");

    @Mock
    private TInvestApiClient client;
    @Mock
    private FileBackedQuoteRepository repository;
    @Mock
    private PriceHistoryStore historyStore;

    private Watchlist watchlist;
    private UpdateCommand command;

    @BeforeEach
    void setUp() {
        watchlist = new Watchlist(Set.of("SBER", "FXUS"));
        command = new UpdateCommand(client, watchlist, repository, historyStore, HISTORY_DIR);
    }

    private static Quote quote(String ticker, String price) {
        return new Quote(new Etf(ticker, ticker, Currency.RUB, "-"), new BigDecimal(price), BigDecimal.ONE);
    }

    @Test
    void emptyWatchlistMeansNothingToDo() {
        UpdateCommand emptyWatchlistCommand =
                new UpdateCommand(client, new Watchlist(), repository, historyStore, HISTORY_DIR);

        assertThat(emptyWatchlistCommand.call()).isEqualTo(1);

        verifyNoInteractions(client, repository, historyStore);
    }

    @Test
    void onlyTrackedQuotesAreSavedAndEachOneGetsAHistoryPoint() {
        when(client.fetchQuotes()).thenReturn(List.of(quote("SBER", "250"), quote("GAZP", "120"), quote("FXUS", "6.85")));

        assertThat(command.call()).isZero();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Quote>> saved = ArgumentCaptor.forClass(List.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue())
                .extracting(q -> q.getInstrument().getTicker())
                .containsExactly("SBER", "FXUS");

        ArgumentCaptor<PricePoint> points = ArgumentCaptor.forClass(PricePoint.class);
        verify(historyStore, times(2)).append(any(), points.capture());
        assertThat(points.getAllValues())
                .extracting(PricePoint::ticker, PricePoint::price)
                .containsExactly(
                        tuple("SBER", new BigDecimal("250")),
                        tuple("FXUS", new BigDecimal("6.85")));
    }

    @Test
    void failureOfTheClientLeavesTheStorageUntouched() {
        when(client.fetchQuotes()).thenThrow(new TInvestApiException("сервис недоступен"));

        assertThat(command.call()).isEqualTo(1);

        verify(repository, never()).save(any());
        verifyNoInteractions(historyStore);
    }
}
