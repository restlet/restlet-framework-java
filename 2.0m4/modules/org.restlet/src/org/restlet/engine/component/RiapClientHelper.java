package org.restlet.engine.component;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.ClientHelper;

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
