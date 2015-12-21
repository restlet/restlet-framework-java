/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.routing;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Restlet filtering calls before passing them to an attached Restlet. The
 * purpose is to do some pre-processing or post-processing on the calls going
 * through it before or after they are actually handled by an attached Restlet.
 * Also note that you can attach and detach targets while handling incoming
 * calls as the filter is ensured to be thread-safe.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public abstract class Filter extends Restlet {

    /**
     * Indicates that the request processing should continue normally. If
     * returned from the {@link #beforeHandle(Request, Response)} method, the
     * filter then invokes the {@link #doHandle(Request, Response)} method. If
     * returned from the {@link #doHandle(Request, Response)} method, the filter
     * then invokes the {@link #afterHandle(Request, Response)} method.
     */
    public static final int CONTINUE = 0;

    /**
     * Indicates that after the {@link #beforeHandle(Request, Response)} method,
     * the request processing should skip the
     * {@link #doHandle(Request, Response)} method to continue with the
     * {@link #afterHandle(Request, Response)} method.
     */
    public static final int SKIP = 1;

    /**
     * Indicates that the request processing should stop and return the current
     * response from the filter.
     */
    public static final int STOP = 2;

    /** The next Restlet. */
    private volatile Restlet next;

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
     * Allows filtering before processing by the next Restlet. Returns
     * {@link #CONTINUE} by default.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status. Either {@link #CONTINUE} or
     *         {@link #SKIP} or {@link #STOP}.
     */
    protected int beforeHandle(Request request, Response response) {
        return CONTINUE;
    }

    /**
     * Handles the call by distributing it to the next Restlet. If no Restlet is
     * attached, then a {@link Status#SERVER_ERROR_INTERNAL} status is returned.
     * Returns {@link #CONTINUE} by default.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status. Either {@link #CONTINUE} or
     *         {@link #STOP}.
     */
    protected int doHandle(Request request, Response response) {
        final int result = CONTINUE;

        if (getNext() != null) {
            getNext().handle(request, response);

            // Re-associate the response to the current thread
            Response.setCurrent(response);

            // Associate the context to the current thread
            if (getContext() != null) {
                Context.setCurrent(getContext());
            }
        } else {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            getLogger()
                    .warning(
                            "The filter "
                                    + getName()
                                    + " was executed without a next Restlet attached to it.");
        }

        return result;
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
        super.handle(request, response);

        switch (beforeHandle(request, response)) {
        case CONTINUE:
            switch (doHandle(request, response)) {
            case CONTINUE:
                afterHandle(request, response);
                break;

            default:
                // Stop the processing
                break;
            }
            break;

        case SKIP:
            afterHandle(request, response);
            break;

        default:
            // Stop the processing
            break;
        }

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
     * Sets the next {@link Restlet} as a {@link Finder} for a given
     * {@link ServerResource} class. When the call is delegated to the
     * {@link Finder} instance, a new instance of the resource class will be
     * created and will actually handle the request.
     * 
     * @param targetClass
     *            The target resource class to attach.
     */
    public void setNext(Class<? extends ServerResource> targetClass) {
        setNext(createFinder(targetClass));
    }

    /**
     * Sets the next Restlet.
     * 
     * In addition, this method will set the context of the next Restlet if it
     * is null by passing a reference to its own context.
     * 
     * @param next
     *            The next Restlet.
     */
    public void setNext(Restlet next) {
        if ((next != null) && (next.getContext() == null)) {
            next.setContext(getContext());
        }

        this.next = next;
    }

    /**
     * Starts the filter and the next Restlet if attached.
     */
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            if (getNext() != null) {
                getNext().start();
            }

            // Must be invoked as a last step
            super.start();
        }
    }

    /**
     * Stops the filter and the next Restlet if attached.
     */
    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            // Must be invoked as a first step
            super.stop();

            if (getNext() != null) {
                getNext().stop();
            }
        }
    }

}
