/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.example.misc;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

/**
 * Test the Amazon Web Service authentication.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AwsTest {
    public static void main(String[] args) throws Exception {
        // Prepare the request
        Request request = new Request(Method.GET,
                "http://s3.amazonaws.com/quotes/nelson");
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_AWS, "44CF9590006BF252F707",
                "OtxrzxIsfpFjA7SwPzILwy8Bw21TLhquhboDYROV"));

        // Add some extra headers
        Series extraHeaders = new Form();
        extraHeaders.add("X-Amz-Meta-Author", "foo@bar.com");
        extraHeaders.add("X-Amz-Magic", "abracadabra");

        // For the test we hard coded a special date header. Normally you don't
        // need this as the
        // HTTP client connector will automatically provide an accurate Date
        // header and use it
        // for authentication.
        // extraHeaders.add("X-Amz-Date", "Thu, 17 Nov 2005 18:49:58 GMT");

        request.getAttributes().put("org.restlet.http.headers", extraHeaders);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        output.write(System.out);
    }

}
