package academy.backend.market_pulse.model;

import lombok.Getter;

@Getter
public class Etf extends Instrument {

    private final String trackingIndex;

    public Etf(String ticker, String name, Currency currency, String trackingIndex) {
        super(ticker, name, currency);
        this.trackingIndex = trackingIndex;
    }

    @Override
    public String getDescription() {
        return "ETF, отслеживает индекс: " + trackingIndex;
    }

    @Override
    public String getType() {
        return "ETF";
    }
}
