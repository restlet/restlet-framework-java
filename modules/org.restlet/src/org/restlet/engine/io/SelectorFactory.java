/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
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

/**
 * Factory used to dispatch/share <code>Selector</code>.
 * 
 * @author Jean-Francois Arcand
 */
public class SelectorFactory {
    /**
     * The number of <code>Selector</code> to create.
     */
    public static int maxSelectors = 20;

    /**
     * Cache of <code>Selector</code>
     */
    private final static Stack<Selector> selectors = new Stack<Selector>();

    /**
     * The timeout before we exit.
     */
    public static long timeout = 5000;

    /**
     * Creates the <code>Selector</code>
     */
    static {
        try {
            for (int i = 0; i < maxSelectors; i++) {
                selectors.add(Selector.open());
            }
        } catch (IOException ex) {
            // do nothing.
        }
    }

    /**
     * Get a exclusive <code>Selector</code>
     * 
     * @return <code>Selector</code>
     */
    public final static Selector getSelector() {
        synchronized (selectors) {
            Selector selector = null;

            try {
                if (selectors.size() != 0) {
                    selector = selectors.pop();
                }
            } catch (EmptyStackException ex) {
            }

            int attempts = 0;
            try {
                while ((selector == null) && (attempts < 2)) {
                    selectors.wait(timeout);

                    try {
                        if (selectors.size() != 0) {
                            selector = selectors.pop();
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
     * Return the <code>Selector</code> to the cache
     * 
     * @param s
     *            <code>Selector</code>
     */
    public final static void returnSelector(Selector s) {
        synchronized (selectors) {
            selectors.push(s);
            if (selectors.size() == 1) {
                selectors.notify();
            }
        }
    }
}