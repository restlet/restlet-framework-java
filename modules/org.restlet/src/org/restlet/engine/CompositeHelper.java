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
public abstract class CompositeHelper<T extends Restlet> extends
        RestletHelper<T> {

    /** The first outbound Filter. */
    private volatile Filter firstOutboundFilter;

    /** The first inbound Restlet. */
    private volatile Restlet inboundNext;

    /** The last inbound Filter. */
    private volatile Filter lastInboundFilter;

    /** The last outbound Filter. */
    private volatile Filter lastOutboundFilter;

    /** The first outbound Restlet. */
    private volatile Restlet outboundNext;

    /**
     * Constructor.
     * 
     * @param helped
     *            The helped Restlet.
     */
    public CompositeHelper(T helped) {
        super(helped);
        this.inboundNext = null;
        this.firstOutboundFilter = null;
        this.lastInboundFilter = null;
        this.lastOutboundFilter = null;
        this.outboundNext = null;
    }

    /**
     * Adds a new inbound filter to the chain.
     * 
     * @param filter
     *            The inbound filter to add.
     */
    protected synchronized void addInboundFilter(Filter filter) {
        Restlet next = getInboundNext();

        if (getLastInboundFilter() != null) {
            getLastInboundFilter().setNext(filter);
        }

        setLastInboundFilter(filter);
        setInboundNext(next);
    }

    /**
     * Adds a new outbound filter to the chain.
     * 
     * @param filter
     *            The outbound filter to add.
     */
    protected synchronized void addOutboundFilter(Filter filter) {
        Restlet next = getOutboundNext();

        if (getFirstOutboundFilter() == null) {
            setFirstOutboundFilter(filter);
        } else if (getLastOutboundFilter() != null) {
            getLastOutboundFilter().setNext(filter);
        }

        setLastOutboundFilter(filter);
        setOutboundNext(next);
    }

    /**
     * Clears the chain. Sets the first and last filters to null.
     */
    public void clear() {
        setInboundNext(null);
        setInboundNext(null);
        setOutboundNext(null);
        setOutboundNext(null);
    }

    /**
     * Returns the first outbound filter.
     * 
     * @return The first outbound filter.
     */
    public Filter getFirstOutboundFilter() {
        return firstOutboundFilter;
    }

    /**
     * Returns the next Restlet in the inbound chain.
     * 
     * @return The next Restlet in the inbound chain.
     */
    protected synchronized Restlet getInboundNext() {
        Restlet result = null;

        if (getLastInboundFilter() != null) {
            result = getLastInboundFilter().getNext();
        } else {
            result = this.inboundNext;
        }

        return result;
    }

    /**
     * Returns the last inbound Filter.
     * 
     * @return the last inbound Filter.
     */
    protected Filter getLastInboundFilter() {
        return this.lastInboundFilter;
    }

    /**
     * Returns the last outbound Filter.
     * 
     * @return the last outbound Filter.
     */
    protected Filter getLastOutboundFilter() {
        return this.lastOutboundFilter;
    }

    /**
     * Returns the next Restlet in the outbound chain.
     * 
     * @return The next Restlet in the outbound chain.
     */
    public synchronized Restlet getOutboundNext() {
        Restlet result = null;

        if (getLastOutboundFilter() != null) {
            result = getLastOutboundFilter().getNext();
        } else {
            result = this.outboundNext;
        }

        return result;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getInboundNext() != null) {
            getInboundNext().handle(request, response);
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
     * Sets the first outbound filter.
     * 
     * @param firstOutboundFilter
     *            The first outbound filter.
     */
    protected void setFirstOutboundFilter(Filter firstOutboundFilter) {
        this.firstOutboundFilter = firstOutboundFilter;
    }

    /**
     * Sets the next Restlet after the inbound chain.
     * 
     * @param next
     *            The Restlet to process after the inbound chain.
     */
    protected synchronized void setInboundNext(Restlet next) {
        if (getLastInboundFilter() != null) {
            getLastInboundFilter().setNext(next);
        }

        this.inboundNext = next;
    }

    /**
     * Sets the last inbound Filter.
     * 
     * @param last
     *            The last inbound Filter.
     */
    protected void setLastInboundFilter(Filter last) {
        this.lastInboundFilter = last;
    }

    /**
     * Sets the last outbound Filter.
     * 
     * @param last
     *            The last outbound Filter.
     */
    protected void setLastOutboundFilter(Filter last) {
        this.lastOutboundFilter = last;
    }

    /**
     * Sets the next Restlet after the outbound chain.
     * 
     * @param next
     *            The Restlet to process after the outbound chain.
     */
    protected synchronized void setOutboundNext(Restlet next) {
        if (getLastOutboundFilter() != null) {
            getLastOutboundFilter().setNext(next);
        }

        this.outboundNext = next;
    }

}
