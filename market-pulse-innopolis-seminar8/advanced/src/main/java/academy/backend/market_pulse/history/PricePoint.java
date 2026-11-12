package academy.backend.market_pulse.history;

import java.math.BigDecimal;
import java.time.Instant;

/** Одна историческая точка цены инструмента — то, из чего потом строится статистика. */
public record PricePoint(String ticker, Instant timestamp, BigDecimal price) {
}
