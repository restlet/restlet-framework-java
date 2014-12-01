package org.restlet.ext.apispark.internal.agent.module;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Redirector;

/**
 * @author Manuel Boillod
 */
public class ReverseProxyModule extends Redirector {

    private final boolean authenticationEnabled;

    public ReverseProxyModule(Context context, String targetTemplate, boolean authenticationEnabled) {
        super(context, targetTemplate, Redirector.MODE_SERVER_OUTBOUND);
        this.authenticationEnabled = authenticationEnabled;
    }

    @Override
    public void handle(Request request, Response response) {
        if (authenticationEnabled) {
            //Do not add Authentication info from redirection request since authentication has already been done.
            request.getHeaders().removeAll("Authorization");
            request.setChallengeResponse(null);
        }
        super.handle(request, response);
    }
}
