package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;

@Deprecated
public class TickerFilter implements InstrumentFilter {

    private final String query;

    public TickerFilter(String query) {
        this.query = query;
    }

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getTicker().toUpperCase().contains(query.toUpperCase());
    }
}
