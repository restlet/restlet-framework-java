/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Pool}. */
class PoolTestCase {

    private static class CountingPool extends Pool<Integer> {
        private static final AtomicInteger COUNTER = new AtomicInteger();

        CountingPool() {
            super();
        }

        CountingPool(int initialSize) {
            super(initialSize);
        }

        @Override
        protected Integer createObject() {
            return COUNTER.incrementAndGet();
        }
    }

    @Test
    void checkoutWithoutCheckin_createsNewObjectsEachTime() {
        CountingPool pool = new CountingPool();
        Integer first = pool.checkout();
        Integer second = pool.checkout();
        assertNotNull(first);
        assertNotNull(second);
    }

    @Test
    void checkinThenCheckout_reusesObject() {
        CountingPool pool = new CountingPool();
        Integer created = pool.checkout();
        pool.checkin(created);
        assertEquals(created, pool.checkout());
    }

    @Test
    void checkin_null_isIgnored() {
        CountingPool pool = new CountingPool();
        pool.checkin(null);
        assertNotNull(pool.checkout());
    }

    @Test
    void constructorWithInitialSize_preCreatesObjects() {
        CountingPool pool = new CountingPool(3);
        assertEquals(3, pool.getStore().size());
    }

    @Test
    void clear_emptiesTheStore() {
        CountingPool pool = new CountingPool(2);
        pool.clear();
        assertEquals(0, pool.getStore().size());
    }
}
