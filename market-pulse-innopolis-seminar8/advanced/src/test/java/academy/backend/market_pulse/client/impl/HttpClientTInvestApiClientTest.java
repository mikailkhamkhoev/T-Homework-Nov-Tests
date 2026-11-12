package academy.backend.market_pulse.client.impl;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiClientContractTestBase;
import academy.backend.market_pulse.config.TInvestApiProperties;

/** Прогон общего контракта ({@link TInvestApiClientContractTestBase}) на готовой реализации на HttpClient. */
class HttpClientTInvestApiClientTest extends TInvestApiClientContractTestBase {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new HttpClientTInvestApiClient(properties);
    }
}
