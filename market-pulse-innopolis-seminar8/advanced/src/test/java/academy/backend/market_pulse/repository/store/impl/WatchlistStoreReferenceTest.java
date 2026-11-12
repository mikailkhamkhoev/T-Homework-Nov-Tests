package academy.backend.market_pulse.repository.store.impl;

import academy.backend.market_pulse.repository.store.WatchlistStore;
import academy.backend.market_pulse.repository.store.WatchlistStoreContractTest;

/**
 * Прогон общего контракта ({@link WatchlistStoreContractTest}) на эталонной реализации — сверка
 * для семинариста в начале семинара 8, студентам заранее не показывать (см. «Требования к
 * файловому хранилищу.md», семинар 7).
 */
class WatchlistStoreReferenceTest extends WatchlistStoreContractTest {

    @Override
    protected WatchlistStore createStore() {
        return new WatchlistStoreReference();
    }
}
