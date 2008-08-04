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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Connector;
import org.restlet.data.Protocol;
import org.restlet.util.Helper;

/**
 * Base connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class ConnectorHelper<T extends Connector> extends Helper<T> {
    /** The protocols simultaneously supported. */
    private volatile List<Protocol> protocols;

    /**
     * Constructor.
     */
    public ConnectorHelper(T connector) {
        super(connector);
        this.protocols = new CopyOnWriteArrayList<Protocol>();
    }

    /**
     * Returns the protocols simultaneously supported.
     * 
     * @return The protocols simultaneously supported.
     */
    public List<Protocol> getProtocols() {
        return this.protocols;
    }

    @Override
    public synchronized void start() throws Exception {
    }

    @Override
    public synchronized void stop() throws Exception {
    }

    @Override
    public void update() throws Exception {
    }

}
