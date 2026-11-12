package academy.backend.market_pulse.model;

import lombok.Getter;

/**
 * Базовая абстракция финансового инструмента. Инкапсулирует общие для всех
 * инструментов данные (тикер, название, валюта) и защищает их инварианты
 * прямо в конструкторе.
 */
@Getter
public abstract class Instrument {

    private final String ticker;
    private final String name;
    private final Currency currency;

    public Instrument(String ticker, String name, Currency currency) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
    }

    public abstract String getDescription();

    /**
     * Код типа инструмента ({@code STOCK}, {@code BOND}, {@code ETF}) — используется вместо
     * {@code getClass().getSimpleName()} там, где нужно узнать тип, не завязываясь на рефлексию.
     */
    public abstract String getType();

    @Override
    public String toString() {
        return ticker + " — " + name + " (" + getDescription() + ")";
    }
}
