package com.netopyr.wurmloch.integration.examples;

import com.netopyr.wurmloch.crdt.PNCounter;
import com.netopyr.wurmloch.store.CrdtStore;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PNCounterExample {

    @Test
    public void runPNCounterExample() {

        // create two CrdtStores and connect them
        final CrdtStore crdtStore1 = new CrdtStore();
        final CrdtStore crdtStore2 = new CrdtStore();
        crdtStore1.connect(crdtStore2);

        // create a PN-Counter and find the according replica in the second store
        final PNCounter replica1 = crdtStore1.createPNCounter("ID_1");
        final PNCounter replica2 = crdtStore2.findPNCounter("ID_1").get();

        // change the value of both replicas of the counter
        replica1.increment();
        replica2.decrement(2L);

        // the stores are connected, thus the replicas are automatically synchronized
        assertThat(replica1.get(), is(-1L));
        assertThat(replica2.get(), is(-1L));

        // disconnect the stores simulating a network issue, offline mode etc.
        crdtStore1.disconnect(crdtStore2);

        // update both counters again
        replica1.decrement(3L);
        replica2.increment(5L);

        // the stores are not connected, thus the changes have only local effects
        assertThat(replica1.get(), is(-4L));
        assertThat(replica2.get(), is(4L));

        // reconnect the stores
        crdtStore1.connect(crdtStore2);

        // the counter is synchronized automatically and contains now the sum of all increments minus all decrements
        assertThat(replica1.get(), is(1L));
        assertThat(replica2.get(), is(1L));

    }
}
