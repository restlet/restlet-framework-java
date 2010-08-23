/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.nio;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Connector;
import org.restlet.engine.util.Pool;

/**
 * A connection pool to prevent to recreation of heavy byte buffers.
 * 
 * @author Jerome Louvel
 */
public class ConnectionPool<T extends Connector> extends Pool<Connection<T>> {

    /** The parent helper. */
    private BaseHelper<T> helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent helper.
     * @param initialSize
     *            The initial pool size.
     */
    public ConnectionPool(BaseHelper<T> helper, int initialSize) {
        super();
        this.helper = helper;
        preCreate(initialSize);
    }

    @Override
    protected void clear(Connection<T> connection) {
        connection.recycle();
    }

    @Override
    protected Connection<T> createObject() {
        Connection<T> result = null;

        try {
            result = this.helper.createConnection(null, null, null);
        } catch (IOException e) {
            helper.getLogger().log(Level.WARNING,
                    "Unable to create a pool object", e);
        }

        return result;
    }

}
