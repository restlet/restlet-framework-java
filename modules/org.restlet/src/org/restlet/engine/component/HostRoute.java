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

package org.restlet.engine.component;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;

/**
 * Route based on a target VirtualHost.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class HostRoute extends Route {
    /**
     * Constructor.
     * 
     * @param router
     *            The parent router.
     * @param target
     *            The target virtual host.
     */
    public HostRoute(Router router, VirtualHost target) {
        super(router, target);
    }

    /**
     * Allows filtering before processing by the next Restlet. Set the base
     * reference.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (request.getHostRef() == null) {
            request.getResourceRef().setBaseRef(
                    request.getResourceRef().getHostIdentifier());
        } else {
            request.getResourceRef().setBaseRef(request.getHostRef());
        }

        if (request.isLoggable() && getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(
                    "Base URI: \"" + request.getResourceRef().getBaseRef()
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
     * Matches a formatted string against a regex pattern, in a case insensitive
     * manner.
     * 
     * @param regex
     *            The pattern to use.
     * @param formattedString
     *            The formatted string to match.
     * @return True if the formatted string matched the pattern.
     */
    private boolean matches(String regex, String formattedString) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                .matcher(formattedString).matches();
    }

    /**
     * Returns the score for a given call (between 0 and 1.0).
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @return The score for a given call (between 0 and 1.0).
     */
    @Override
    public float score(Request request, Response response) {
        float result = 0F;

        // Prepare the value to be matched
        String hostDomain = "";
        String hostPort = "";
        String hostScheme = "";

        if (request.getHostRef() != null) {
            hostDomain = request.getHostRef().getHostDomain();

            if (hostDomain == null) {
                hostDomain = "";
            }

            int basePortValue = request.getHostRef().getHostPort();

            if (basePortValue == -1) {
                basePortValue = request.getHostRef().getSchemeProtocol()
                        .getDefaultPort();
            }

            hostPort = Integer.toString(basePortValue);

            hostScheme = request.getHostRef().getScheme();

            if (hostScheme == null) {
                hostScheme = "";
            }
        }

        if (request.getResourceRef() != null) {
            String resourceDomain = request.getResourceRef().getHostDomain();

            if (resourceDomain == null) {
                resourceDomain = "";
            }

            int resourcePortValue = request.getResourceRef().getHostPort();

            if (resourcePortValue == -1
                    && request.getResourceRef()
                            .getSchemeProtocol() != null) {
                resourcePortValue = request.getResourceRef()
                        .getSchemeProtocol().getDefaultPort();
            }

            String resourcePort = (resourcePortValue == -1) ? "" : Integer.toString(resourcePortValue);

            String resourceScheme = request.getResourceRef().getScheme();

            if (resourceScheme == null) {
                resourceScheme = "";
            }

            String serverAddress = response.getServerInfo().getAddress();

            if (serverAddress == null) {
                serverAddress = "";
            }

            int serverPortValue = response.getServerInfo().getPort();

            if (serverPortValue == -1) {
                serverPortValue = request.getProtocol().getDefaultPort();
            }

            String serverPort = Integer.toString(response.getServerInfo()
                    .getPort());

            // Check if all the criteria match
            if (matches(getVirtualHost().getHostDomain(), hostDomain)
                    && matches(getVirtualHost().getHostPort(), hostPort)
                    && matches(getVirtualHost().getHostScheme(), hostScheme)
                    && matches(getVirtualHost().getResourceDomain(), resourceDomain)
                    && matches(getVirtualHost().getResourcePort(), resourcePort)
                    && matches(getVirtualHost().getResourceScheme(), resourceScheme)
                    && matches(getVirtualHost().getServerAddress(), serverAddress)
                    && matches(getVirtualHost().getServerPort(), serverPort)) {
                result = 1F;
            }
        }

        // Log the result of the matching
        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer(
                    "Call score for the \"" + getVirtualHost().getName()
                            + "\" host: " + result);
        }

        return result;
    }

    /**
     * Sets the next virtual host.
     * 
     * @param next
     *            The next virtual host.
     */
    public void setNext(VirtualHost next) {
        super.setNext(next);
    }
}
