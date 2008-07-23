/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.internal;

import java.util.ArrayList;
import java.util.List;

import org.restlet.gwt.Connector;
import org.restlet.gwt.Context;
import org.restlet.gwt.data.Protocol;

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
        this.protocols = new ArrayList<Protocol>();
    }

    @Override
    public Context createContext(String loggerName) {
        return null;
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
