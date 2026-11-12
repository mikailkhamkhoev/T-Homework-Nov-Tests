package academy.backend.market_pulse.client.impl;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiClientContractTestBase;
import academy.backend.market_pulse.config.TInvestApiProperties;

/** Прогон общего контракта ({@link TInvestApiClientContractTestBase}) на опорной реализации. */
class HttpUrlConnectionTInvestApiClientTest extends TInvestApiClientContractTestBase {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new HttpUrlConnectionTInvestApiClient(properties);
    }
}
