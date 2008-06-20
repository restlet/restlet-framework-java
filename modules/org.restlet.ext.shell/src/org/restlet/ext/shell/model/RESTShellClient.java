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

package org.restlet.ext.shell.model;

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

    private ChallengeResponse challengeResponse;

    private Request request;

    private Response response;

    public RESTShellClient(Context context) {
        super(context, protocols);
        request = new Request();
        response = new Response(request);
    }

    public ChallengeResponse getChallengeResponse() {
        return challengeResponse;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
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

    public void setChallengeResponse(ChallengeResponse challengeResponse) {
        this.challengeResponse = challengeResponse;
    }
}
