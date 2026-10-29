package academy.backend.market_pulse.history;

import academy.backend.market_pulse.history.impl.PriceHistoryStoreSkeleton;

public class PriceHistoryStoreSkeletonTest extends PriceHistoryStoreContractTest {

    @Override
    protected PriceHistoryStore createStore() {
        return new PriceHistoryStoreSkeleton();
    }
}