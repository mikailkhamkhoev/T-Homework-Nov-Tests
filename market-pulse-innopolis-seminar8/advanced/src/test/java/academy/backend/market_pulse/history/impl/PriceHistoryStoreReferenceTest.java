package academy.backend.market_pulse.history.impl;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PriceHistoryStoreContractTest;

/**
 * Прогон общего контракта ({@link PriceHistoryStoreContractTest}) на эталонной реализации —
 * сверка для семинариста в начале семинара 8, студентам заранее не показывать (см. «Требования к
 * файловому хранилищу.md», семинар 7).
 */
class PriceHistoryStoreReferenceTest extends PriceHistoryStoreContractTest {

    @Override
    protected PriceHistoryStore createStore() {
        return new PriceHistoryStoreReference();
    }
}
