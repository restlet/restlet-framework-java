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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Restlet;

class ChildContextTestCase {

    @Test
    void constructor_withParentContext_copiesServerDispatcher() {
        Context parent = new Context();
        ChildContext child = new ChildContext(parent);
        assertEquals(parent.getServerDispatcher(), child.getServerDispatcher());
        assertEquals(parent, child.getParentContext());
    }

    @Test
    void constructor_setsChildClientDispatcher() {
        Context parent = new Context();
        ChildContext child = new ChildContext(parent);
        assertNotNull(child.getClientDispatcher());
    }

    @Test
    void constructor_withNullParentContext_leavesServerDispatcherNull() {
        ChildContext child = new ChildContext(null);
        assertNull(child.getServerDispatcher());
        assertNull(child.getExecutorService());
    }

    @Test
    void getChildAndSetChild_roundTrip() {
        Context parent = new Context();
        ChildContext child = new ChildContext(parent);
        Restlet restlet = new Restlet() {};

        child.setChild(restlet);

        assertEquals(restlet, child.getChild());
    }
}
