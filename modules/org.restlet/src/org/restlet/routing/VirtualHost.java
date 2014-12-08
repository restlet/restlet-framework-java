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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Router of calls from Server connectors to Restlets. The attached Restlets are
 * typically Applications.<br>
 * <br>
 * A virtual host is defined along three properties:
 * <ul>
 * <li>request's {@link Request#getHostRef()}: the URI of the host that received
 * the request. Note that the same IP address can correspond to multiple domain
 * names and therefore receive request with different "hostRef" URIs.</li>
 * <li>request's {@link Request#getResourceRef()}: the URI of the target
 * resource of the request. If this reference is relative, then it is based on
 * the "hostRef", otherwise it is maintained as received. This difference is
 * useful for resources identified by URNs or for Web proxies or Web caches.</li>
 * <li>response's {@link Response#getServerInfo()}: the information about the
 * server connector receiving the requests such as it IP address and port
 * number.</li>
 * </ul>
 * When creating a new instance, you can define Java regular expressions (
 * {@link java.util.regex.Pattern}) that must match the domain name, port,
 * scheme for references or IP address and port number for server information.
 * The default values match everything.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see java.util.regex.Pattern
 * @see <a href="http://en.wikipedia.org/wiki/Virtual_hosting">Wikipedia -
 *      Virtual Hosting</a>
 * @see <a href="http://httpd.apache.org/docs/2.2/vhosts/">Apache - Virtual
 *      Hosting</a>
 * @author Jerome Louvel
 */
public class VirtualHost extends Router {
    private static final ThreadLocal<Integer> CURRENT = new ThreadLocal<Integer>();

    /**
     * Returns the virtual host code associated to the current thread.
     * 
     * This variable is stored internally as a thread local variable and updated
     * each time a call is routed by a virtual host.
     * 
     * @return The current context.
     */
    public static Integer getCurrent() {
        return CURRENT.get();
    }

    /**
     * Returns the IP address of a given domain name.
     * 
     * @param domain
     *            The domain name.
     * @return The IP address.
     */
    public static String getIpAddress(String domain) {
        String result = null;

        try {
            result = InetAddress.getByName(domain).getHostAddress();
        } catch (UnknownHostException e) {
        }

        return result;
    }

    /**
     * Returns the local host IP address.
     * 
     * @return The local host IP address.
     */
    public static String getLocalHostAddress() {
        String result = null;

        try {
            result = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }

        return result;
    }

    /**
     * Returns the local host name.
     * 
     * @return The local host name.
     */
    public static String getLocalHostName() {
        String result = null;

        try {
            result = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }

        return result;
    }

    /**
     * Sets the virtual host code associated with the current thread.
     * 
     * @param code
     *            The thread's virtual host code.
     */
    public static void setCurrent(Integer code) {
        CURRENT.set(code);
    }

    /** The hostRef host domain pattern to match. */
    private volatile String hostDomain;

    /** The hostRef host port pattern to match. */
    private volatile String hostPort;

    /** The hostRef scheme pattern to match. */
    private volatile String hostScheme;

    /** The parent component's context. */
    private volatile Context parentContext;

    /** The resourceRef host domain pattern to match. */
    private volatile String resourceDomain;

    /** The resourceRef host port pattern to match. */
    private volatile String resourcePort;

    /** The resourceRef scheme pattern to match. */
    private volatile String resourceScheme;

    /** The listening server address pattern to match. */
    private volatile String serverAddress;

    /** The listening server port pattern to match. */
    private volatile String serverPort;

    /**
     * Constructor. Note that usage of this constructor is not recommended as
     * the virtual host won't have a proper context set. In general you will
     * prefer to use the other constructor and pass it the parent component's
     * context.
     */
    public VirtualHost() {
        this(null);
    }

    /**
     * Constructor. Accepts all incoming requests by default, use the set
     * methods to restrict the matchable patterns.
     * 
     * @param parentContext
     *            The parent component's context.
     */
    public VirtualHost(Context parentContext) {
        this(parentContext, ".*", ".*", ".*", ".*", ".*", ".*", ".*", ".*");
    }

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The parent component's context.
     * @param hostDomain
     *            The hostRef host domain pattern to match.
     * @param hostPort
     *            The hostRef host port pattern to match.
     * @param hostScheme
     *            The hostRef scheme protocol pattern to match.
     * @param resourceDomain
     *            The resourceRef host domain pattern to match.
     * @param resourcePort
     *            The resourceRef host port pattern to match.
     * @param resourceScheme
     *            The resourceRef scheme protocol pattern to match.
     * @param serverAddress
     *            The listening server address pattern to match.
     * @param serverPort
     *            The listening server port pattern to match.
     * @see java.util.regex.Pattern
     */
    public VirtualHost(Context parentContext, String hostDomain,
            String hostPort, String hostScheme, String resourceDomain,
            String resourcePort, String resourceScheme, String serverAddress,
            String serverPort) {
        super((parentContext == null) ? null : parentContext
                .createChildContext());

        // Override Router's default modes
        setDefaultMatchingMode(Template.MODE_STARTS_WITH);
        setRoutingMode(MODE_BEST_MATCH);

        this.parentContext = parentContext;

        this.hostDomain = hostDomain;
        this.hostPort = hostPort;
        this.hostScheme = hostScheme;

        this.resourceDomain = resourceDomain;
        this.resourcePort = resourcePort;
        this.resourceScheme = resourceScheme;

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Attaches a target Restlet to this router with an empty URI pattern. A new
     * route will be added routing to the target when any call is received.
     * 
     * In addition to super class behavior, this method will set the context of
     * the target if it is empty by creating a protected context via the
     * {@link Context#createChildContext()} method.
     * 
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    @Override
    public TemplateRoute attach(Restlet target) {
        checkContext(target);
        return super.attach(target);
    }

    /**
     * Attaches a target Restlet to this router based on a given URI pattern. A
     * new route will be added routing to the target when calls with a URI
     * matching the pattern will be received.
     * 
     * In addition to super class behavior, this method will set the context of
     * the target if it is empty by creating a protected context via the
     * {@link Context#createChildContext()} method.
     * 
     * @param uriPattern
     *            The URI pattern that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    @Override
    public TemplateRoute attach(String uriPattern, Restlet target) {
        checkContext(target);
        return super.attach(uriPattern, target);
    }

    /**
     * Attaches a Restlet to this router as the default target to invoke when no
     * route matches. It actually sets a default route that scores all calls to
     * 1.0.
     * 
     * In addition to super class behavior, this method will set the context of
     * the target if it is empty by creating a protected context via the
     * {@link Context#createChildContext()} method.
     * 
     * @param defaultTarget
     *            The Restlet to use as the default target.
     * @return The created route.
     */
    @Override
    public TemplateRoute attachDefault(Restlet defaultTarget) {
        checkContext(defaultTarget);
        return super.attachDefault(defaultTarget);
    }

    /**
     * Checks the context and sets it if necessary.
     * 
     * @param target
     *            The target Restlet.
     */
    protected void checkContext(Restlet target) {
        if ((target.getContext() == null) && (this.parentContext != null)) {
            target.setContext(this.parentContext.createChildContext());
        }
    }

    /**
     * Creates a new finder instance based on the "targetClass" property.
     * 
     * In addition to super class behavior, this method will set the context of
     * the finder by creating a protected context via the
     * {@link Context#createChildContext()} method.
     * 
     * @param targetClass
     *            The target Resource class to attach.
     * @return The new finder instance.
     */
    @Override
    public Finder createFinder(Class<? extends ServerResource> targetClass) {
        Finder result = super.createFinder(targetClass);
        result.setContext(getContext().createChildContext());
        return result;
    }

    @Override
    protected TemplateRoute createRoute(String uriPattern, Restlet target,
            int matchingMode) {
        TemplateRoute result = new TemplateRoute(this, uriPattern, target) {
            @Override
            protected int beforeHandle(Request request, Response response) {
                final int result = super.beforeHandle(request, response);

                // Set the request's root reference
                request.setRootRef(request.getResourceRef().getBaseRef());

                // Save the hash code of the current host
                setCurrent(VirtualHost.this.hashCode());

                return result;
            }
        };

        result.getTemplate().setMatchingMode(matchingMode);
        result.setMatchingQuery(getDefaultMatchingQuery());
        return result;
    }

    /**
     * Returns the hostRef host domain to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The hostRef host domain to match.
     */
    public String getHostDomain() {
        return this.hostDomain;
    }

    /**
     * Returns the hostRef host port to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The hostRef host port to match.
     */
    public String getHostPort() {
        return this.hostPort;
    }

    /**
     * Returns the hostRef scheme to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The hostRef scheme to match.
     */
    public String getHostScheme() {
        return this.hostScheme;
    }

    /**
     * Returns the resourceRef host domain to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The resourceRef host domain to match.
     */
    public String getResourceDomain() {
        return this.resourceDomain;
    }

    /**
     * Returns the resourceRef host port to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The resourceRef host port to match.
     */
    public String getResourcePort() {
        return this.resourcePort;
    }

    /**
     * Returns the resourceRef scheme to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The resourceRef scheme to match.
     */
    public String getResourceScheme() {
        return this.resourceScheme;
    }

    /**
     * Returns the listening server address. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The listening server address.
     */
    public String getServerAddress() {
        return this.serverAddress;
    }

    /**
     * Returns the listening server port. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @return The listening server port.
     */
    public String getServerPort() {
        return this.serverPort;
    }

    @Override
    public void setContext(Context parentContext) {
        this.parentContext = parentContext;
        super.setContext((parentContext == null) ? null : parentContext
                .createChildContext());
    }

    /**
     * Sets the hostRef host domain to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @param hostDomain
     *            The hostRef host domain to match.
     */
    public void setHostDomain(String hostDomain) {
        this.hostDomain = hostDomain;
    }

    /**
     * Sets the hostRef host port to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @param hostPort
     *            The hostRef host port to match.
     */
    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * Sets the hostRef scheme to match. See the {@link java.util.regex.Pattern}
     * class for details on the syntax.
     * 
     * @param hostScheme
     *            The hostRef scheme to match.
     */
    public void setHostScheme(String hostScheme) {
        this.hostScheme = hostScheme;
    }

    /**
     * Sets the resourceRef host domain to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @param resourceDomain
     *            The resourceRef host domain to match.
     */
    public void setResourceDomain(String resourceDomain) {
        this.resourceDomain = resourceDomain;
    }

    /**
     * Sets the resourceRef host port to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @param resourcePort
     *            The resourceRef host port to match.
     */
    public void setResourcePort(String resourcePort) {
        this.resourcePort = resourcePort;
    }

    /**
     * Sets the resourceRef scheme to match. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @param resourceScheme
     *            The resourceRef scheme to match.
     */
    public void setResourceScheme(String resourceScheme) {
        this.resourceScheme = resourceScheme;
    }

    /**
     * Sets the listening server address. See the
     * {@link java.util.regex.Pattern} class for details on the syntax.
     * 
     * @param serverAddress
     *            The listening server address.
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Sets the listening server port. See the {@link java.util.regex.Pattern}
     * class for details on the syntax.
     * 
     * @param serverPort
     *            The listening server port.
     */
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

}
