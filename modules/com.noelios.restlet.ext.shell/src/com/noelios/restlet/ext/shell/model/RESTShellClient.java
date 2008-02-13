package com.noelios.restlet.ext.shell.model;

import java.util.ArrayList;
import java.util.List;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class RESTShellClient extends Client {
    private static List<Protocol> protocols;
    
    static {
        protocols = new ArrayList<Protocol>();
        protocols.add(Protocol.HTTP);
    }

    private Request request;
    private Response response;
    private ChallengeResponse challengeResponse;

    public RESTShellClient(Context context) {
        super(context, protocols);
        request = new Request();
        response = new Response(request);
    }

    @Override
    public void handle(Request request, Response response) {
        if (challengeResponse != null) {
            request.setChallengeResponse(challengeResponse);
        }

        this.request = request;
        super.handle(request, response);
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public ChallengeResponse getChallengeResponse() {
        return challengeResponse;
    }

    public void setChallengeResponse(ChallengeResponse challengeResponse) {
        this.challengeResponse = challengeResponse;
    }
}
