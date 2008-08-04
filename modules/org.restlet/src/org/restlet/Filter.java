/*
 * Copyright 2005-2007 Noelios Technologies.
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

package org.restlet;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

/**
 * Restlet filtering calls before passing them to an attached Restlet. The
 * purpose is to do some pre-processing or post-processing on the calls going
 * through it before or after they are actually handled by an attached Restlet.
 * Also note that you can attach and detach targets while handling incoming
 * calls as the filter is ensured to be thread-safe.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Filter extends Restlet {
    /** The next Restlet. */
    private Restlet next;

    /**
     * Constructor.
     */
    public Filter() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Filter(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     */
    public Filter(Context context, Restlet next) {
        super(context);
        this.next = next;
    }

    /**
     * Allows filtering after processing by the next Restlet. Does nothing by
     * default.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void afterHandle(Request request, Response response) {
        // To be overriden
    }

    /**
     * Allows filtering before processing by the next Restlet. Does nothing by
     * default.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void beforeHandle(Request request, Response response) {
        // To be overriden
    }

    /**
     * Handles the call by distributing it to the next Restlet.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void doHandle(Request request, Response response) {
        if (getNext() != null) {
            getNext().handle(request, response);
        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    /**
     * Returns the next Restlet.
     * 
     * @return The next Restlet or null.
     */
    public Restlet getNext() {
        return this.next;
    }

    /**
     * Handles a call by first invoking the beforeHandle() method for
     * pre-filtering, then distributing the call to the next Restlet via the
     * doHandle() method. When the handling is completed, it finally invokes the
     * afterHandle() method for post-filtering.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
	@Override
    public final void handle(Request request, Response response) {
        init(request, response);
        beforeHandle(request, response);
        doHandle(request, response);
        afterHandle(request, response);
    }

    /**
     * Indicates if there is a next Restlet.
     * 
     * @return True if there is a next Restlet.
     */
    public boolean hasNext() {
        return getNext() != null;
    }

    /**
     * Sets the next Restlet.
     * 
     * @param next
     *            The next Restlet.
     */
    public void setNext(Restlet next) {
        this.next = next;
    }

    /**
     * Sets the next Restlet as a Finder for a given Resource class. When the
     * call is delegated to the Finder instance, a new instance of the Resource
     * class will be created and will actually handle the request.
     * 
     * @param targetClass
     *            The target Resource class to attach.
     */
    public void setNext(Class<? extends Resource> targetClass) {
        setNext(new Finder(getContext(), targetClass));
    }

}
