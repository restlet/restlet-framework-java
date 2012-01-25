/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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
