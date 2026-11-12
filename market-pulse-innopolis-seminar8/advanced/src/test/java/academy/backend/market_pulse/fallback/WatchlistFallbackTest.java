package academy.backend.market_pulse.fallback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import academy.backend.market_pulse.model.Watchlist;

/**
 * Запасной вариант: тест для {@link Watchlist}, сгенерированный ИИ по промпту из
 * {@code docs/seminar8/Промпт для генерации тестов.md} и проверенный по чек-листу ревью. Нужен, если
 * генерация на занятии не удалась.
 */
class WatchlistFallbackTest {

    @Nested
    @DisplayName("add / contains")
    class AddAndContains {

        @Test
        void newTickerIsAdded() {
            Watchlist watchlist = new Watchlist();

            assertThat(watchlist.add("SBER")).isTrue();
            assertThat(watchlist.contains("SBER")).isTrue();
        }

        @ParameterizedTest(name = "повторное добавление [{1}] после [{0}] -> дубликат")
        @CsvSource({"SBER, SBER", "SBER, sber", "sber, SBER", "SbEr, sBeR"})
        void duplicateIgnoringCaseIsNotAddedTwice(String first, String second) {
            Watchlist watchlist = new Watchlist();
            watchlist.add(first);

            assertThat(watchlist.add(second)).isFalse();
            assertThat(watchlist.tickers()).hasSize(1);
        }

        @ParameterizedTest(name = "contains({0}) -> true")
        @CsvSource({"SBER", "sber", "Sber"})
        void containsIgnoresCase(String query) {
            Watchlist watchlist = new Watchlist();
            watchlist.add("SBER");

            assertThat(watchlist.contains(query)).isTrue();
        }

        @Test
        void unknownTickerIsNotContained() {
            Watchlist watchlist = new Watchlist();
            watchlist.add("SBER");

            assertThat(watchlist.contains("GAZP")).isFalse();
        }

        @Test
        void nullTickerFailsFast() {
            Watchlist watchlist = new Watchlist();

            assertThatThrownBy(() -> watchlist.add(null)).isInstanceOf(NullPointerException.class);
        }

        // NOTICE: нашли пробел в самом классе: пустая строка принимается как тикер. Тест описывает
        // желаемое поведение и отключён до решения — отличный повод показать, что тесты ищут дефекты.
        @Disabled("Известный пробел: Watchlist принимает пустой тикер")
        @Test
        void blankTickerShouldBeRejected() {
            Watchlist watchlist = new Watchlist();

            assertThatThrownBy(() -> watchlist.add("  ")).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("tickers()")
    class Tickers {

        @Test
        void tickersAreNormalizedAndKeepInsertionOrder() {
            Watchlist watchlist = new Watchlist();
            watchlist.add("lkoh");
            watchlist.add("SBER");
            watchlist.add("gazp");

            assertThat(watchlist.tickers()).containsExactly("LKOH", "SBER", "GAZP");
        }

        @Test
        void returnedViewCannotBeModified() {
            Watchlist watchlist = new Watchlist();
            watchlist.add("SBER");

            assertThatThrownBy(() -> watchlist.tickers().add("GAZP")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> watchlist.tickers().clear()).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void emptyWatchlistHasNoTickers() {
            assertThat(new Watchlist().tickers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("восстановление из сохранённого списка")
    class Restore {

        @Test
        void initialTickersAreNormalizedAndDeduplicated() {
            Watchlist watchlist = new Watchlist(Set.of("sber", "SBER", "gazp"));

            assertThat(watchlist.tickers()).containsExactlyInAnyOrder("SBER", "GAZP");
        }
    }
}
