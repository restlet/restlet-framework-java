/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.component;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.restlet.Route;
import org.restlet.Router;
import org.restlet.VirtualHost;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Route based on a target VirtualHost.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
        super(router, "", target);
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
     * Sets the next virtual host.
     * 
     * @param next
     *            The next virtual host.
     */
    public void setNext(VirtualHost next) {
        super.setNext(next);
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
            if (hostDomain == null)
                hostDomain = "";

            int basePortValue = request.getHostRef().getHostPort();
            if (basePortValue == -1)
                basePortValue = request.getHostRef().getSchemeProtocol()
                        .getDefaultPort();
            hostPort = Integer.toString(basePortValue);

            hostScheme = request.getHostRef().getScheme();
            if (hostScheme == null)
                hostScheme = "";
        }

        String resourceDomain = request.getResourceRef().getHostDomain();
        if (resourceDomain == null)
            resourceDomain = "";

        int resourcePortValue = request.getResourceRef().getHostPort();
        if (resourcePortValue == -1)
            resourcePortValue = request.getResourceRef().getSchemeProtocol()
                    .getDefaultPort();
        String resourcePort = Integer.toString(resourcePortValue);

        String resourceScheme = request.getResourceRef().getScheme();
        if (resourceScheme == null)
            resourceScheme = "";

        String serverAddress = response.getServerInfo().getAddress();
        if (serverAddress == null)
            serverAddress = "";

        String serverPort = "";
        if (response.getServerInfo().getPort() != -1)
            serverPort = Integer.toString(response.getServerInfo().getPort());

        // Check if all the criterias match
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

        // Log the result of the matching
        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer(
                    "Call score for the \"" + getVirtualHost().getName()
                            + "\" host: " + result);
        }

        return result;
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
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(
                formattedString).matches();
    }

    /**
     * Allows filtering before processing by the next Restlet. Set the base
     * reference.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
	@Override
    protected void beforeHandle(Request request, Response response) {
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(
                    "New base URI: " + request.getResourceRef().getBaseRef());
            getLogger().fine(
                    "New remaining part: "
                            + request.getResourceRef().getRemainingPart());
        }
    }
}
