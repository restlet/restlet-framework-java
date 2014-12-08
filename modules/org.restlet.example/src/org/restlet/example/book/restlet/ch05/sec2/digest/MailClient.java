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

package org.restlet.example.book.restlet.ch05.sec2.digest;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Mail client retrieving a mail then storing it again on the same resource.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        // Create and configure HTTPS client
        Client client = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = client.getContext().getParameters();
        parameters.add("truststorePath",
                "src/org/restlet/example/book/restlet/ch05/clientTrust.jks");
        parameters.add("truststorePassword", "password");
        parameters.add("truststoreType", "JKS");

        // Create and configure client resource
        ClientResource clientResource = new ClientResource(
                "https://localhost:8183/accounts/chunkylover53/mails/123");
        clientResource.setNext(client);
        MailResource mailClient = clientResource.wrap(MailResource.class);

        try {
            // Obtain the authentication options via the challenge requests
            mailClient.retrieve();
        } catch (ResourceException re) {
            if (Status.CLIENT_ERROR_UNAUTHORIZED.equals(re.getStatus())) {
                // Retrieve the HTTP Digest hints from the server
                ChallengeRequest digestChallenge = null;

                for (ChallengeRequest challengeRequest : clientResource
                        .getChallengeRequests()) {
                    if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest
                            .getScheme())) {
                        digestChallenge = challengeRequest;
                        break;
                    }
                }

                // Configure the authentication credentials
                ChallengeResponse authentication = new ChallengeResponse(
                        digestChallenge, clientResource.getResponse(),
                        "chunkylover53", "pwd");
                clientResource.setChallengeResponse(authentication);
            }
        }

        // Communicate with remote resource
        mailClient.store(mailClient.retrieve());

        // Store HTTPS client
        client.stop();
    }
}
