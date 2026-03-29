/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.component;

import java.util.logging.Level;
import java.util.regex.Pattern;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;

/**
 * Route based on a target VirtualHost.
 *
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state in member variables.
 *
 * @author Jerome Louvel
 */
public class HostRoute extends Route {
    /**
     * Constructor.
     *
     * @param router The parent router.
     * @param target The target virtual host.
     */
    public HostRoute(Router router, VirtualHost target) {
        super(router, target);
    }

    /**
     * Allows filtering before processing by the next Restlet. Set the base reference.
     *
     * @param request The request to handle.
     * @param response The response to modify.
     * @return The continuation status.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (request.getHostRef() == null) {
            request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());
        } else {
            request.getResourceRef().setBaseRef(request.getHostRef());
        }

        if (request.isLoggable() && getLogger().isLoggable(Level.FINE)) {
            getLogger()
                    .fine(
                            "Base URI: \""
                                    + request.getResourceRef().getBaseRef()
                                    + "\". Remaining part: \""
                                    + request.getResourceRef().getRemainingPart()
                                    + "\"");
        }

        return CONTINUE;
    }

    /**
     * Returns the target virtual host.
     *
     * @return The target virtual host.
     */
    public VirtualHost getVirtualHost() {
        return (VirtualHost) getNext();
    }

    /**
     * Matches a domain against a regex pattern, in a case-insensitive manner.
     *
     * @param regex The pattern to use.
     * @param domain The domain name to match.
     * @return True if the formatted string matched the pattern.
     */
    private boolean matchesDomain(String regex, String domain) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                .matcher(domain == null ? "" : domain)
                .matches();
    }

    /**
     * Matches a port number against a regex pattern.
     *
     * @param regex The pattern to use.
     * @param port The port to match.
     * @return True if the port matched the pattern.
     */
    private boolean matchesPort(String regex, int port) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                .matcher(port == -1 ? "" : Integer.toString(port))
                .matches();
    }

    /**
     * Matches a scheme against a regex pattern, in a case-insensitive manner.
     *
     * @param regex The pattern to use.
     * @param scheme The scheme to match.
     * @return True if the scheme matched the pattern.
     */
    private boolean matchesScheme(String regex, String scheme) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                .matcher(scheme == null ? "" : scheme)
                .matches();
    }

    /**
     * Returns the score for a given call (between 0 and 1.0).
     *
     * @param request The request to score.
     * @param response The response to score.
     * @return The score for a given call (between 0 and 1.0).
     */
    @Override
    public float score(Request request, Response response) {
        final float result;

        // Prepare the value to be matched
        String hostScheme = null;
        String hostDomain = null;
        int hostPort = -1;

        if (request.getHostRef() != null) {
            hostDomain = request.getHostRef().getHostDomain();
            hostScheme = request.getHostRef().getScheme();
            hostPort = getHostPortOrProtocolDefaultPort(request.getHostRef());
        }

        if (request.getResourceRef() != null) {
            String resourceScheme = request.getResourceRef().getScheme();
            String resourceDomain = request.getResourceRef().getHostDomain();
            final int resourcePort = getHostPortOrProtocolDefaultPort(request.getResourceRef());

            String serverAddress = response.getServerInfo().getAddress();
            int serverPort = response.getServerInfo().getPort();
            if (serverPort == -1) {
                serverPort = request.getProtocol().getDefaultPort();
            }

            // Check if all the criteria match
            if (matchesDomain(getVirtualHost().getHostDomain(), hostDomain)
                    && matchesPort(getVirtualHost().getHostPort(), hostPort)
                    && matchesScheme(getVirtualHost().getHostScheme(), hostScheme)
                    && matchesDomain(getVirtualHost().getResourceDomain(), resourceDomain)
                    && matchesPort(getVirtualHost().getResourcePort(), resourcePort)
                    && matchesScheme(getVirtualHost().getResourceScheme(), resourceScheme)
                    && matchesDomain(getVirtualHost().getServerAddress(), serverAddress)
                    && matchesPort(getVirtualHost().getServerPort(), serverPort)) {
                result = 1F;
            } else {
                result = 0F;
            }
        } else {
            result = 0F;
        }

        // Log the result of the matching
        getLogger()
                .finer(
                        () ->
                                "Call score for the \""
                                        + getVirtualHost().getName()
                                        + "\" host: "
                                        + result);

        return result;
    }

    private int getHostPortOrProtocolDefaultPort(final Reference reference) {
        int hostPort = reference.getHostPort();

        if (hostPort == -1 && reference.getSchemeProtocol() != null) {
            hostPort = reference.getSchemeProtocol().getDefaultPort();
        }
        return hostPort;
    }
}
