/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.nio.internal.connection;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Connector;
import org.restlet.engine.util.Pool;
import org.restlet.ext.nio.ConnectionHelper;

/**
 * A connection pool to prevent to recreation of heavy byte buffers.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to favor lower-level network extensions allowing more control at the Restlet API level.
 */
@Deprecated
public class ConnectionPool<T extends Connector> extends Pool<Connection<T>> {

    /** The parent helper. */
    private ConnectionHelper<T> helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent helper.
     * @param initialSize
     *            The initial pool size.
     */
    public ConnectionPool(ConnectionHelper<T> helper, int initialSize) {
        super();
        this.helper = helper;
        preCreate(initialSize);
    }

    @Override
    protected void clear(Connection<T> connection) {
        connection.clear();
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
