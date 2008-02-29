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

package org.restlet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Router of calls from Server connectors to Restlets. The attached Restlets are
 * typically Applications.<br>
 * <br>
 * A virtual host is defined along three properties:
 * <ul>
 * <li>request's {@link Request#getHostRef()}: the URI of the host that
 * received the request. Note that the same IP address can correspond to
 * multiple domain names and therefore receive request with different "hostRef"
 * URIs.</li>
 * <li>request's {@link Request#getResourceRef()}: the URI of the target
 * resource of the request. If this reference is relative, then it is based on
 * the "hostRef", otherwise it is maintained as received. This difference is
 * useful for resources identified by URNs or for Web proxies or Web caches.</li>
 * <li>response's {@link Response#getServerInfo()}: the information about the
 * server connector receiving the requests such as it IP address and port
 * number.</li>
 * </ul>
 * When creating a new instance, you can define Java regular expressions ({@link java.util.regex.Pattern})
 * that must match the domain name, port, scheme for references or IP address
 * and port number for server information. The default values match everything.
 * 
 * @see java.util.regex.Pattern
 * @see <a href="http://en.wikipedia.org/wiki/Virtual_hosting">Wikipedia -
 *      Virtual Hosting</a>
 * @see <a href="http://httpd.apache.org/docs/2.2/vhosts/">Apache - Virtual
 *      Hosting</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class VirtualHost extends Router {
    /**
     * Returns the IP address of a given domain name.
     * 
     * @param domain
     *                The domain name.
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

    /** The display name. */
    private volatile String name;

    /** The hostRef host domain pattern to match. */
    private volatile String hostDomain;

    /** The hostRef host port pattern to match. */
    private volatile String hostPort;

    /** The hostRef scheme pattern to match. */
    private volatile String hostScheme;

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
     * the Router won't have a proper context set. In general you will prefer to
     * use the other constructor and pass it the parent component's context.
     */
    public VirtualHost() {
        this(null);
    }

    /**
     * Constructor. Accepts all incoming requests by default, use the set
     * methods to restrict the matchable patterns.
     * 
     * @param context
     *                The context.
     */
    public VirtualHost(Context context) {
        this(context, ".*", ".*", ".*", ".*", ".*", ".*", ".*", ".*");
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param hostDomain
     *                The hostRef host domain pattern to match.
     * @param hostPort
     *                The hostRef host port pattern to match.
     * @param hostScheme
     *                The hostRef scheme protocol pattern to match.
     * @param resourceDomain
     *                The resourceRef host domain pattern to match.
     * @param resourcePort
     *                The resourceRef host port pattern to match.
     * @param resourceScheme
     *                The resourceRef scheme protocol pattern to match.
     * @param serverAddress
     *                The listening server address pattern to match.
     * @param serverPort
     *                The listening server port pattern to match.
     */
    public VirtualHost(Context context, String hostDomain, String hostPort,
            String hostScheme, String resourceDomain, String resourcePort,
            String resourceScheme, String serverAddress, String serverPort) {
        super(context);
        this.hostDomain = hostDomain;
        this.hostPort = hostPort;
        this.hostScheme = hostScheme;

        this.resourceDomain = resourceDomain;
        this.resourcePort = resourcePort;
        this.resourceScheme = resourceScheme;

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    protected Route createRoute(String uriPattern, Restlet target) {
        return new Route(this, uriPattern, target) {
            @Override
            protected int beforeHandle(Request request, Response response) {
                int result = super.beforeHandle(request, response);

                // Set the request's root reference
                request.setRootRef(request.getResourceRef().getBaseRef());

                return result;
            }
        };
    }

    /**
     * Returns the hostRef host domain to match. Uses patterns in
     * java.util.regex.
     * 
     * @return The hostRef host domain to match.
     */
    public String getHostDomain() {
        return this.hostDomain;
    }

    /**
     * Returns the hostRef host port to match. Uses patterns in java.util.regex.
     * 
     * @return The hostRef host port to match.
     */
    public String getHostPort() {
        return this.hostPort;
    }

    /**
     * Returns the hostRef scheme to match. Uses patterns in java.util.regex.
     * 
     * @return The hostRef scheme to match.
     */
    public String getHostScheme() {
        return this.hostScheme;
    }

    /**
     * Returns the display name.
     * 
     * @return The display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the resourceRef host domain to match. Uses patterns in
     * java.util.regex.
     * 
     * @return The resourceRef host domain to match.
     */
    public String getResourceDomain() {
        return this.resourceDomain;
    }

    /**
     * Returns the resourceRef host port to match. Uses patterns in
     * java.util.regex.
     * 
     * @return The resourceRef host port to match.
     */
    public String getResourcePort() {
        return this.resourcePort;
    }

    /**
     * Returns the resourceRef scheme to match. Uses patterns in
     * java.util.regex.
     * 
     * @return The resourceRef scheme to match.
     */
    public String getResourceScheme() {
        return this.resourceScheme;
    }

    /**
     * Returns the listening server address. Uses patterns in java.util.regex.
     * 
     * @return The listening server address.
     */
    public String getServerAddress() {
        return this.serverAddress;
    }

    /**
     * Returns the listening server port. Uses patterns in java.util.regex.
     * 
     * @return The listening server port.
     */
    public String getServerPort() {
        return this.serverPort;
    }

    /**
     * Sets the hostRef host domain to match. Uses patterns in java.util.regex.
     * 
     * @param hostDomain
     *                The hostRef host domain to match.
     */
    public void setHostDomain(String hostDomain) {
        this.hostDomain = hostDomain;
    }

    /**
     * Sets the hostRef host port to match. Uses patterns in java.util.regex.
     * 
     * @param hostPort
     *                The hostRef host port to match.
     */
    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * Sets the hostRef scheme to match. Uses patterns in java.util.regex.
     * 
     * @param hostScheme
     *                The hostRef scheme to match.
     */
    public void setHostScheme(String hostScheme) {
        this.hostScheme = hostScheme;
    }

    /**
     * Sets the display name.
     * 
     * @param name
     *                The display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the resourceRef host domain to match. Uses patterns in
     * java.util.regex.
     * 
     * @param resourceDomain
     *                The resourceRef host domain to match.
     */
    public void setResourceDomain(String resourceDomain) {
        this.resourceDomain = resourceDomain;
    }

    /**
     * Sets the resourceRef host port to match. Uses patterns in
     * java.util.regex.
     * 
     * @param resourcePort
     *                The resourceRef host port to match.
     */
    public void setResourcePort(String resourcePort) {
        this.resourcePort = resourcePort;
    }

    /**
     * Sets the resourceRef scheme to match. Uses patterns in java.util.regex.
     * 
     * @param resourceScheme
     *                The resourceRef scheme to match.
     */
    public void setResourceScheme(String resourceScheme) {
        this.resourceScheme = resourceScheme;
    }

    /**
     * Sets the listening server address. Uses patterns in java.util.regex.
     * 
     * @param serverAddress
     *                The listening server address.
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Sets the listening server port. Uses patterns in java.util.regex.
     * 
     * @param serverPort
     *                The listening server port.
     */
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

}
