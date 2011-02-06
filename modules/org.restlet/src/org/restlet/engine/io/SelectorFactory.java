/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.io;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.EmptyStackException;
import java.util.Stack;

// [excludes gwt]
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