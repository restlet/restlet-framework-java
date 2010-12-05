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

package org.restlet.engine;

import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/**
 * Chain helper serving as base class for Application and Component helpers.
 * 
 * @author Jerome Louvel
 */
public class CompositeHelper<T extends Restlet> extends RestletHelper<T> {

    /** The first inbound Restlet. */
    private volatile Restlet firstInbound;

    /** The last inbound Filter. */
    private volatile Filter lastInbound;

    /**
     * Constructor.
     * 
     * @param helped
     *            The helped Restlet.
     */
    public CompositeHelper(T helped) {
        super(helped);
        this.firstInbound = null;
    }

    /**
     * Adds a new inbound filter to the chain.
     * 
     * @param filter
     *            The inbound filter to add.
     */
    protected synchronized void addInboundFilter(Filter filter) {
        if (getLastInbound() != null) {
            getLastInbound().setNext(filter);
            setLastInbound(filter);
        } else {
            setFirstInbound(filter);
            setLastInbound(filter);
        }
    }

    /**
     * Clears the chain. Sets the first and last filters to null.
     */
    public void clear() {
        setFirstInbound(null);
        setInboundNext(null);
    }

    /**
     * Returns the first inbound Restlet.
     * 
     * @return the first inbound Restlet.
     */
    protected Restlet getFirstInbound() {
        return this.firstInbound;
    }

    /**
     * Returns the last inbound Filter.
     * 
     * @return the last inbound Filter.
     */
    protected Filter getLastInbound() {
        return this.lastInbound;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getFirstInbound() != null) {
            getFirstInbound().handle(request, response);
        } else {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            getHelped()
                    .getLogger()
                    .log(Level.SEVERE,
                            "The "
                                    + getHelped().getClass().getName()
                                    + " class has no Restlet defined to process calls. Maybe it wasn't properly started.");
        }
    }

    /**
     * Sets the first inbound Restlet.
     * 
     * @param first
     *            The first inbound Restlet.
     */
    protected void setFirstInbound(Restlet first) {
        this.firstInbound = first;
    }

    /**
     * Sets the next Restlet after the inbound chain.
     * 
     * @param next
     *            The Restlet to process after the inbound chain.
     */
    protected synchronized void setInboundNext(Restlet next) {
        if (getFirstInbound() == null) {
            setFirstInbound(next);
        } else {
            getLastInbound().setNext(next);
        }
    }

    /**
     * Sets the last inbound Filter.
     * 
     * @param last
     *            The last inbound Filter.
     */
    protected void setLastInbound(Filter last) {
        this.lastInbound = last;
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
