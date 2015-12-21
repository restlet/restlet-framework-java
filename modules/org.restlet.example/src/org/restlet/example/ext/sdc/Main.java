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

package org.restlet.example.ext.sdc;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

public class Main {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Client sdcClient = new Client(new Context(), Protocol.valueOf("SDC"));
        Series<Parameter> parameters = sdcClient.getContext().getParameters();
        parameters.add("keystorePath", "sdc.keystore");
        parameters.add("keystorePassword", "password");
        parameters.add("enabledCipherSuites", "TLS_RSA_WITH_AES_128_CBC_SHA");
        parameters.add("sslProtocol", "TLSv1");
        sdcClient.start();

        System.out
                .println("Press a key when the SDC agent is started and has established a tunnel...");
        System.in.read();

        Request request = new Request(Method.GET, "http://restlet.org");
        request.setProtocol(Protocol.valueOf("SDC"));
        request.setProxyChallengeResponse(new ChallengeResponse(ChallengeScheme
                .valueOf("SDC"), "myUser@example.com", "myPassword"));
        Response response = sdcClient.handle(request);
        response.getEntity().write(System.out);
    }
}
