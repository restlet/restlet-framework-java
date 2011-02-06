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

package org.restlet.ext.jdbc.internal;

import java.util.Properties;

import org.apache.commons.dbcp.PoolingDataSource;
import org.restlet.ext.jdbc.JdbcClientHelper;

/**
 * Pooling data source which remembers its connection properties and URI.
 * 
 * @author Jerome Louvel
 */
public class ConnectionSource extends PoolingDataSource {
    /** The connection properties. */
    protected Properties properties;

    /** The connection URI. */
    protected String uri;

    /**
     * Constructor.
     * 
     * @param uri
     *            The connection URI.
     * @param properties
     *            The connection properties.
     */
    public ConnectionSource(String uri, Properties properties) {
        super(JdbcClientHelper.createConnectionPool(uri, properties));
        this.uri = uri;
        this.properties = properties;
    }

    /**
     * Returns the connection properties.
     * 
     * @return The connection properties.
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Returns the connection URI.
     * 
     * @return The connection URI.
     */
    public String getUri() {
        return this.uri;
    }
}