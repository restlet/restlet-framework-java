/**
 * Copyright 2005-2009 Noelios Technologies.
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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Utility methods for NIO processing.
 * 
 * @author Jerome Louvel
 */
public class NioUtils {
    /** The number of milliseconds after which NIO operation will time out. */
    public static final int NIO_TIMEOUT = 60000;

    /**
     * Release the selection key, working around for bug #6403933.
     * 
     * @param selector
     *            The associated selector.
     * @param selectionKey
     *            The used selection key.
     * @throws IOException
     */
    public static void release(Selector selector, SelectionKey selectionKey)
            throws IOException {
        if (selectionKey != null) {
            // The key you registered on the temporary selector
            selectionKey.cancel();

            if (selector != null) {
                // Flush the canceled key
                selector.selectNow();
                SelectorFactory.returnSelector(selector);
            }
        }

    }

    /**
     * Waits for the given channel to be ready for a specific operation.
     * 
     * @param selectableChannel
     *            The channel to monitor.
     * @param operations
     *            The operations to be ready to do.
     * @throws IOException
     */
    public static void waitForState(SelectableChannel selectableChannel,
            int operations) throws IOException {
        if (selectableChannel != null) {
            Selector selector = null;
            SelectionKey selectionKey = null;
            int selected = 0;

            try {
                selector = SelectorFactory.getSelector();

                while (selected == 0) {
                    selectionKey = selectableChannel.register(selector,
                            operations);
                    selected = selector.select(NIO_TIMEOUT);
                }
            } finally {
                NioUtils.release(selector, selectionKey);
            }
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private NioUtils() {
    }

}
