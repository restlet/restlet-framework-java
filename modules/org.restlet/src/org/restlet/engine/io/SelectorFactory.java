/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.io;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.EmptyStackException;
import java.util.Stack;

// [excludes gwt,android]
/**
 * Factory used to dispatch/share <code>Selector</code>.
 * 
 * @author Jean-Francois Arcand
 */
public class SelectorFactory {
    /** The maximum number of <code>Selector</code> to create. */
    public static final int MAX_SELECTORS = 20;

    /** The number of attempts to find an available selector. */
    public static final int MAX_ATTEMPTS = 2;

    /** Cache of <code>Selector</code>. */
    private static final Stack<Selector> SELECTORS = new Stack<Selector>();

    /** The timeout before we exit. */
    public static final long TIMEOUT = 5000;

    // [ifndef gae]
    /** Creates the <code>Selector</code>. */
    static {
        try {
            for (int i = 0; i < MAX_SELECTORS; i++) {
                SELECTORS.add(Selector.open());
            }
        } catch (IOException ex) {
            // do nothing.
        }
    }

    // [enddef]

    /**
     * Get an exclusive <code>Selector</code>.
     * 
     * @return An exclusive <code>Selector</code>.
     */
    public final static Selector getSelector() {
        synchronized (SELECTORS) {
            Selector selector = null;

            try {
                if (SELECTORS.size() != 0) {
                    selector = SELECTORS.pop();
                }
            } catch (EmptyStackException ex) {
            }

            int attempts = 0;
            try {
                while ((selector == null) && (attempts < MAX_ATTEMPTS)) {
                    SELECTORS.wait(TIMEOUT);

                    try {
                        if (SELECTORS.size() != 0) {
                            selector = SELECTORS.pop();
                        }
                    } catch (EmptyStackException ex) {
                        break;
                    }

                    attempts++;
                }
            } catch (InterruptedException ex) {
            }

            return selector;
        }
    }

    /**
     * Returns the <code>Selector</code> to the cache.
     * 
     * @param selector
     *            The <code>Selector</code> to return.
     */
    public final static void returnSelector(Selector selector) {
        synchronized (SELECTORS) {
            SELECTORS.push(selector);
            if (SELECTORS.size() == 1) {
                SELECTORS.notify();
            }
        }
    }
}
