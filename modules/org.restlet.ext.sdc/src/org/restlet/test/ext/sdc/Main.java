package org.restlet.test.ext.sdc;

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

        Request request = new Request(Method.GET, "http://www.restlet.org");
        request.setProtocol(Protocol.valueOf("SDC"));
        request.setProxyChallengeResponse(new ChallengeResponse(ChallengeScheme
                .valueOf("SDC"), "myUser@example.com", "myPassword"));
        Response response = sdcClient.handle(request);
        response.getEntity().write(System.out);
    }
}
