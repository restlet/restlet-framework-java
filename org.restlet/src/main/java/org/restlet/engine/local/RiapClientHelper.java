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

package org.restlet.engine.local;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientHelper;

/**
 * Client connector for RIAP calls. Only the "component" authority is supported.
 * 
 * @author Thierry Boileau
 * @see Protocol#RIAP
 */
public class RiapClientHelper extends ClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public RiapClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.RIAP);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        final String scheme = request.getResourceRef().getScheme();

        if (Protocol.RIAP.getSchemeName().equalsIgnoreCase(scheme)) {
            // Support only the "component" authority
            LocalReference ref = new LocalReference(request.getResourceRef());

            if (ref.getRiapAuthorityType() == LocalReference.RIAP_COMPONENT) {
                if (RiapServerHelper.instance != null
                        && RiapServerHelper.instance.getContext() != null
                        && RiapServerHelper.instance.getContext()
                                .getClientDispatcher() != null) {
                    RiapServerHelper.instance.getContext()
                            .getClientDispatcher().handle(request, response);
                } else {
                    super.handle(request, response);
                }
            } else {
                throw new IllegalArgumentException(
                        "Authority \""
                                + ref.getAuthority()
                                + "\" not supported by the connector. Only \"component\" is supported.");
            }
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only RIAP is supported.");
        }

    }
}
