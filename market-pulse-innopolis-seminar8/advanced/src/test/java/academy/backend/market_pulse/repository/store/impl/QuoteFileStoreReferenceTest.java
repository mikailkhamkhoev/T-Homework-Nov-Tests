package academy.backend.market_pulse.repository.store.impl;

import academy.backend.market_pulse.repository.store.QuoteFileStore;
import academy.backend.market_pulse.repository.store.QuoteFileStoreContractTest;

/**
 * Прогон общего контракта ({@link QuoteFileStoreContractTest}) на эталонной реализации — сверка
 * для семинариста в начале семинара 8, студентам заранее не показывать (см. «Требования к
 * файловому хранилищу.md», семинар 7).
 */
class QuoteFileStoreReferenceTest extends QuoteFileStoreContractTest {

    @Override
    protected QuoteFileStore createStore() {
        return new QuoteFileStoreReference();
    }
}
