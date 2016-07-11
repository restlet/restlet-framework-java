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

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Resolver;

/**
 * Rewrites URIs then redirects the call or the client to a new destination.
 * There are various redirection modes that you can choose from: client-side
 * redirections ({@link #MODE_CLIENT_FOUND}, {@link #MODE_CLIENT_PERMANENT},
 * {@link #MODE_CLIENT_SEE_OTHER}, {@link #MODE_CLIENT_TEMPORARY}) or
 * server-side redirections, similar to a reverse proxy (
 * {@link #MODE_SERVER_OUTBOUND} and {@link #MODE_SERVER_INBOUND}).<br>
 * <br>
 * When setting the redirection URIs, you can also used special URI variables to
 * reuse most properties from the original request as well as URI template
 * variables. For a complete list of properties, please see the {@link Resolver}
 * class. For example "/target?referer={fi}" would redirect to the relative URI,
 * inserting the referrer URI as a query parameter.<br>
 * <br>
 * To create a reverse proxy, a typically configuration will use the
 * {@link #MODE_SERVER_OUTBOUND} constant and a target URI like
 * "http://targetHost/targetRootPath/{rr}" to ensure that all child URIs are
 * properly redirected as well, "rr" appending the remaining part of the current
 * request URI that hasn't been routed yet.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see org.restlet.routing.Template
 * @see <a href="http://wiki.restlet.org/docs_2.2/375-restlet.html">User Guide -
 *      URI rewriting and redirection</a>
 * @author Jerome Louvel
 */
public class Redirector extends Restlet {
    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern using the {@link Status#REDIRECTION_FOUND} status.
     * Note: this is a client-side redirection.<br>
     * 
     * @see Status#REDIRECTION_FOUND
     */
    public static final int MODE_CLIENT_FOUND = 2;

    /**
     * In this mode, the client is permanently redirected to the URI generated
     * from the target URI pattern, using the
     * {@link Status#REDIRECTION_PERMANENT} status. Note: this is a client-side
     * redirection.<br>
     * 
     * @see Status#REDIRECTION_PERMANENT
     */
    public static final int MODE_CLIENT_PERMANENT = 1;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern using the {@link Status#REDIRECTION_SEE_OTHER}
     * status. Note: this is a client-side redirection.<br>
     * 
     * @see Status#REDIRECTION_SEE_OTHER
     */
    public static final int MODE_CLIENT_SEE_OTHER = 3;

    /**
     * In this mode, the client is temporarily redirected to the URI generated
     * from the target URI pattern using the
     * {@link Status#REDIRECTION_TEMPORARY} status. Note: this is a client-side
     * redirection.<br>
     * 
     * @see Status#REDIRECTION_TEMPORARY
     */
    public static final int MODE_CLIENT_TEMPORARY = 4;

    /**
     * In this mode, the call is sent to {@link Context#getServerDispatcher()}.
     * Once the selected client connector has completed the request handling,
     * the response is normally returned to the client. In this case, you can
     * view the Redirector as acting as a transparent proxy Restlet. Note: this
     * is a server-side redirection.<br>
     * <br>
     * Warning: remember to add the required connectors to the parent
     * {@link Component} and to declare them in the list of required connectors
     * on the {@link Application#getConnectorService()} property.<br>
     * <br>
     * Note that in this mode, the headers of HTTP requests, stored in the
     * request's attributes, are removed before dispatching. Also, when a HTTP
     * response comes back the headers are also removed. You can control this
     * behavior by setting the {@link #headersCleaning} attribute or by
     * overriding the {@link #rewrite(Request)} or {@link #rewrite(Response)}.
     * 
     * @see Context#getServerDispatcher()
     */
    public static final int MODE_SERVER_INBOUND = 7;

    /**
     * In this mode, the call is sent to {@link Application#getOutboundRoot()}
     * or if null to {@link Context#getClientDispatcher()}. Once the selected
     * client connector has completed the request handling, the response is
     * normally returned to the client. In this case, you can view the
     * {@link Redirector} as acting as a transparent server-side proxy. Note:
     * this is a server-side redirection.<br>
     * <br>
     * Warning: remember to add the required connectors to the parent
     * {@link Component} and to declare them in the list of required connectors
     * on the {@link Application#getConnectorService()} property.<br>
     * <br>
     * Note that in this mode, the headers of HTTP requests, stored in the
     * request's attributes, are removed before dispatching. Also, when a HTTP
     * response comes back the headers are also removed. You can control this
     * behavior by setting the {@link #headersCleaning} attribute or by
     * overriding the {@link #rewrite(Request)} or {@link #rewrite(Response)}.
     * 
     * @see Application#getOutboundRoot()
     * @see Context#getClientDispatcher()
     */
    public static final int MODE_SERVER_OUTBOUND = 6;

    /**
     * Indicates if the headers of HTTP requests stored in the request's
     * attributes, and the .
     */
    protected volatile boolean headersCleaning;

    /** The redirection mode. */
    protected volatile int mode;

    /** The target URI pattern. */
    protected volatile String targetTemplate;

    /**
     * Constructor for the client dispatcher mode.
     * 
     * @param context
     *            The context.
     * @param targetTemplate
     *            The template to build the target URI.
     * @see org.restlet.routing.Template
     */
    public Redirector(Context context, String targetTemplate) {
        this(context, targetTemplate, MODE_SERVER_OUTBOUND);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetPattern
     *            The pattern to build the target URI (using StringTemplate
     *            syntax and the CallModel for variables).
     * @param mode
     *            The redirection mode.
     */
    public Redirector(Context context, String targetPattern, int mode) {
        super(context);
        this.targetTemplate = targetPattern;
        this.mode = mode;
        this.headersCleaning = true;
    }

    /**
     * Computes the new location of the given reference, after applying the
     * redirection template. Returns null in case it cannot compute the new
     * reference.
     * 
     * @param locationRef
     *            The reference to translate.
     * @param request
     *            The current request.
     * @return The new location of the given reference.
     */
    private String getLocation(Reference locationRef, Request request) {
        Reference resourceRef = request.getResourceRef();
        Reference baseRef = resourceRef.getBaseRef();

        Template rt = new Template(this.targetTemplate);
        rt.setLogger(getLogger());
        int matched = rt.parse(locationRef.toString(), request);

        if (matched > 0) {
            String remainingPart = (String) request.getAttributes().get("rr");

            if (remainingPart != null) {
                return baseRef.toString() + remainingPart;
            }
        }

        return null;
    }

    /**
     * Returns the redirection mode.
     * 
     * @return The redirection mode.
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * Returns the target reference to redirect to by automatically resolving
     * URI template variables found using the {@link Template} class using the
     * request and response as data models.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target reference to redirect to.
     */
    protected Reference getTargetRef(Request request, Response response) {
        // Create the template
        Template rt = new Template(this.targetTemplate);
        rt.setLogger(getLogger());

        // Return the formatted target URI
        if (new Reference(this.targetTemplate).isRelative()) {
            // Be sure to keep the resource's base reference.
            return new Reference(request.getResourceRef(), rt.format(request,
                    response));
        }

        return new Reference(rt.format(request, response));
    }

    /**
     * Returns the target URI pattern.
     * 
     * @return The target URI pattern.
     */
    public String getTargetTemplate() {
        return this.targetTemplate;
    }

    /**
     * Handles a call by redirecting using the selected redirection mode.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Generate the target reference
        Reference targetRef = getTargetRef(request, response);

        switch (this.mode) {
        case MODE_CLIENT_PERMANENT:
            if (request.isLoggable()) {
                getLogger().log(Level.FINE,
                        "Permanently redirecting client to: " + targetRef);
            }

            response.redirectPermanent(targetRef);
            break;

        case MODE_CLIENT_FOUND:
            if (request.isLoggable()) {
                getLogger().log(Level.FINE,
                        "Redirecting client to found location: " + targetRef);
            }

            response.setLocationRef(targetRef);
            response.setStatus(Status.REDIRECTION_FOUND);
            break;

        case MODE_CLIENT_SEE_OTHER:
            if (request.isLoggable()) {
                getLogger().log(Level.FINE,
                        "Redirecting client to another location: " + targetRef);
            }

            response.redirectSeeOther(targetRef);
            break;

        case MODE_CLIENT_TEMPORARY:
            if (request.isLoggable()) {
                getLogger().log(Level.FINE,
                        "Temporarily redirecting client to: " + targetRef);
            }

            response.redirectTemporary(targetRef);
            break;

        case MODE_SERVER_OUTBOUND:
            if (request.isLoggable()) {
                getLogger().log(Level.FINE,
                        "Redirecting via client dispatcher to: " + targetRef);
            }

            outboundServerRedirect(targetRef, request, response);
            break;

        case MODE_SERVER_INBOUND:
            if (request.isLoggable()) {
                getLogger().log(Level.FINE,
                        "Redirecting via server dispatcher to: " + targetRef);
            }

            inboundServerRedirect(targetRef, request, response);
            break;
        }
    }

    /**
     * Redirects a given call to a target reference. In the default
     * implementation, the request HTTP headers, stored in the request's
     * attributes, are removed before dispatching. After dispatching, the
     * response HTTP headers are also removed to prevent conflicts with the main
     * call.
     * 
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void inboundServerRedirect(Reference targetRef, Request request,
            Response response) {
        serverRedirect(getContext().getServerDispatcher(), targetRef, request,
                response);
    }

    /**
     * Indicates if the headers must be cleaned.
     * 
     * @return True if the headers must be cleaned.
     */
    public boolean isHeadersCleaning() {
        return headersCleaning;
    }

    /**
     * Redirects a given call to a target reference. In the default
     * implementation, the request HTTP headers, stored in the request's
     * attributes, are removed before dispatching. After dispatching, the
     * response HTTP headers are also removed to prevent conflicts with the main
     * call.
     * 
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void outboundServerRedirect(Reference targetRef, Request request,
            Response response) {
        Restlet next = (getApplication() == null) ? null : getApplication()
                .getOutboundRoot();

        if (next == null) {
            next = getContext().getClientDispatcher();
        }

        serverRedirect(next, targetRef, request, response);
        if (response.getEntity() != null
                && !request.getResourceRef().getScheme()
                        .equalsIgnoreCase(targetRef.getScheme())) {
            // Distinct protocol, this data cannot be exposed.
            response.getEntity().setLocationRef((Reference) null);
        }
    }

    /**
     * Optionally rewrites the response entity returned in the
     * {@link #MODE_SERVER_INBOUND} and {@link #MODE_SERVER_OUTBOUND} modes. By
     * default, it just returns the initial entity without any modification.
     * 
     * @param initialEntity
     *            The initial entity returned.
     * @return The rewritten entity.
     */
    protected Representation rewrite(Representation initialEntity) {
        return initialEntity;
    }

    /**
     * Optionally updates the request sent in the {@link #MODE_SERVER_INBOUND}
     * and {@link #MODE_SERVER_OUTBOUND} modes. By default, it leverages the
     * {@link #headersCleaning} attribute in order to clean the headers: if set
     * to true, it removes all headers, otherwise it keeps only the extension
     * (or non HTTP standard) headers<br>
     * 
     * @param initialRequest
     *            The initial request returned.
     * @return The updated request.
     */
    protected void rewrite(Request initialRequest) {
        if (isHeadersCleaning()) {
            initialRequest.getAttributes().remove(
                    HeaderConstants.ATTRIBUTE_HEADERS);
        } else {
            HeaderUtils.keepExtensionHeadersOnly(initialRequest);
        }
    }

    /**
     * Optionally updates the response sent in the {@link #MODE_SERVER_INBOUND}
     * and {@link #MODE_SERVER_OUTBOUND} modes. By default, it leverages the
     * {@link #headersCleaning} attribute in order to clean the headers: if set
     * to true, it removes all headers, otherwise it keeps only the extension
     * (or non HTTP standard) headers<br>
     * 
     * @param initialRequest
     *            The initial request returned.
     * @return The updated request.
     */
    protected void rewrite(Response initialResponse) {
        if (isHeadersCleaning()) {
            initialResponse.getAttributes().remove(
                    HeaderConstants.ATTRIBUTE_HEADERS);
        } else {
            HeaderUtils.keepExtensionHeadersOnly(initialResponse);
        }
    }

    /**
     * Rewrite the location of the response, and the Location of the entity, if
     * any.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void rewriteLocation(Request request, Response response) {
        if (response.getLocationRef() != null) {
            Reference locationRef = response.getLocationRef();

            String newLocation = getLocation(locationRef, request);
            if (newLocation != null) {
                response.setLocationRef(newLocation);
            }
        }
        if (response.getEntity() != null
                && response.getEntity().getLocationRef() != null) {
            Reference locationRef = response.getEntity().getLocationRef();

            String newLocation = getLocation(locationRef, request);
            if (newLocation != null) {
                response.getEntity().setLocationRef(newLocation);
            }
        }
    }

    /**
     * Redirects a given call on the server-side to a next Restlet with a given
     * target reference. In the default implementation, the request HTTP
     * headers, stored in the request's attributes, are removed before
     * dispatching. After dispatching, the response HTTP headers are also
     * removed to prevent conflicts with the main call.
     * 
     * @param next
     *            The next Restlet to forward the call to.
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void serverRedirect(Restlet next, Reference targetRef,
            Request request, Response response) {
        if (next == null) {
            getLogger().warning(
                    "No next Restlet provided for server redirection to "
                            + targetRef);
        } else {
            // Save the base URI if it exists as we might need it for
            // redirections
            Reference resourceRef = request.getResourceRef();

            // Reset the protocol and let the dispatcher handle the protocol
            request.setProtocol(null);

            // Update the request to cleanly go to the target URI
            request.setResourceRef(targetRef);
            rewrite(request);
            next.handle(request, response);

            request.setResourceRef(resourceRef);
            // Allow for response rewriting and clean the headers
            response.setEntity(rewrite(response.getEntity()));
            rewrite(response);

            // In case of redirection, we may have to rewrite the redirect URI
            rewriteLocation(request, response);
        }
    }

    /**
     * Indicates if the headers must be cleaned.
     * 
     * @param headersCleaning
     *            True if the headers must be cleaned.
     */
    public void setHeadersCleaning(boolean headersCleaning) {
        this.headersCleaning = headersCleaning;
    }

    /**
     * Sets the redirection mode.
     * 
     * @param mode
     *            The redirection mode.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Sets the target URI pattern.
     * 
     * @param targetTemplate
     *            The target URI pattern.
     */
    public void setTargetTemplate(String targetTemplate) {
        this.targetTemplate = targetTemplate;
    }

}
