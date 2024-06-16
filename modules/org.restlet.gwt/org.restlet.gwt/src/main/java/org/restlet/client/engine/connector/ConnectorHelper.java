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

package org.restlet.client.engine.connector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.client.Connector;
import org.restlet.client.Context;
import org.restlet.client.data.Protocol;
import org.restlet.client.engine.Edition;
import org.restlet.client.engine.RestletHelper;

/**
 * Base connector helper.
 * 
 * @author Jerome Louvel
 */
public abstract class ConnectorHelper<T extends Connector> extends
        RestletHelper<T> {


    /** The protocols simultaneously supported. */
    private final List<Protocol> protocols;

    /**
     * Constructor.
     */
    public ConnectorHelper(T connector) {
        super(connector);
        this.protocols = new CopyOnWriteArrayList<Protocol>();
    }

    /**
     * Returns the helped Restlet context.
     * 
     * @return The helped Restlet context.
     */
    @Override
    public Context getContext() {
        if (Edition.CURRENT == Edition.GWT) {
            return null;
        }

        return super.getContext();
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
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void update() throws Exception {
    }

}
