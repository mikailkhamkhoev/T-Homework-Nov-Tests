package academy.backend.market_pulse.factory.impl;

import academy.backend.market_pulse.factory.InstrumentFactory;

import java.math.BigDecimal;
import java.time.Year;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

public class BondFactory implements InstrumentFactory {

    @Override
    public String type() {
        return "BOND";
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // couponRate и maturityYear не собираются через CLI на этом этапе — значения по умолчанию.
        return new Bond(ticker, name, currency, BigDecimal.ZERO, Year.now().getValue());
    }
}
