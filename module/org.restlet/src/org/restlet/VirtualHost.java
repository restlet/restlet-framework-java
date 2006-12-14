/*
 * Copyright 2005-2006 Noelios Consulting.
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

/**
 * Router of calls from Server connectors to Restlets. The attached Restlets are
 * typically Applications.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class VirtualHost extends Router {
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

    /** The display name. */
    private String name;

    /** The hostRef host domain pattern to match. */
    private String hostDomain;

    /** The hostRef host port pattern to match. */
    private String hostPort;

    /** The hostRef scheme pattern to match. */
    private String hostScheme;

    /** The resourceRef host domain pattern to match. */
    private String resourceDomain;

    /** The resourceRef host port pattern to match. */
    private String resourcePort;

    /** The resourceRef scheme pattern to match. */
    private String resourceScheme;

    /** The listening server address pattern to match. */
    private String serverAddress;

    /** The listening server port pattern to match. */
    private String serverPort;

    /**
     * Constructor.
     */
    public VirtualHost() {
        this(null);
    }

    /**
     * Constructor. Accepts all incoming requests by default, use the set
     * methods to restrict the matchable patterns.
     * 
     * @param context
     *            The context.
     */
    public VirtualHost(Context context) {
        this(context, ".*", ".*", ".*", ".*", ".*", ".*", ".*", ".*");
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
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

    /**
     * Returns the hostRef host domain to match. Uses patterns in
     * java.util.regex.
     * 
     * @return The hostRef host domain to match.
     * @deprecated Use getHostDomain instead.
     */
    @Deprecated
    public String getBaseDomain() {
        return this.hostDomain;
    }

    /**
     * Returns the hostRef host port to match. Uses patterns in java.util.regex.
     * 
     * @return The hostRef host port to match.
     * @deprecated Use getHostDomain instead.
     */
    @Deprecated
    public String getBasePort() {
        return this.hostPort;
    }

    /**
     * Returns the hostRef scheme to match. Uses patterns in java.util.regex.
     * 
     * @return The hostRef scheme to match.
     * @deprecated Use getHostDomain instead.
     */
    @Deprecated
    public String getBaseScheme() {
        return this.hostScheme;
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
     * Sets the baseRef host domain to match. Uses patterns in java.util.regex.
     * 
     * @param baseDomain
     *            The baseRef host domain to match.
     */
    public void setBaseDomain(String baseDomain) {
        this.hostDomain = baseDomain;
    }

    /**
     * Sets the baseRef host port to match. Uses patterns in java.util.regex.
     * 
     * @param basePort
     *            The baseRef host port to match.
     */
    public void setBasePort(String basePort) {
        this.hostPort = basePort;
    }

    /**
     * Sets the baseRef scheme to match. Uses patterns in java.util.regex.
     * 
     * @param baseScheme
     *            The baseRef scheme to match.
     */
    public void setBaseScheme(String baseScheme) {
        this.hostScheme = baseScheme;
    }

    /**
     * Sets the display name.
     * 
     * @param name
     *            The display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the resourceRef host domain to match. Uses patterns in
     * java.util.regex.
     * 
     * @param resourceDomain
     *            The resourceRef host domain to match.
     */
    public void setResourceDomain(String resourceDomain) {
        this.resourceDomain = resourceDomain;
    }

    /**
     * Sets the resourceRef host port to match. Uses patterns in
     * java.util.regex.
     * 
     * @param resourcePort
     *            The resourceRef host port to match.
     */
    public void setResourcePort(String resourcePort) {
        this.resourcePort = resourcePort;
    }

    /**
     * Sets the resourceRef scheme to match. Uses patterns in java.util.regex.
     * 
     * @param resourceScheme
     *            The resourceRef scheme to match.
     */
    public void setResourceScheme(String resourceScheme) {
        this.resourceScheme = resourceScheme;
    }

    /**
     * Sets the listening server address. Uses patterns in java.util.regex.
     * 
     * @param serverAddress
     *            The listening server address.
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Sets the listening server port. Uses patterns in java.util.regex.
     * 
     * @param serverPort
     *            The listening server port.
     */
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

}
