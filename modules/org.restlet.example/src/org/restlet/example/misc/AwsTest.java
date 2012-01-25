/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.misc;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Test the Amazon Web Service authentication.
 * 
 * @author Jerome Louvel
 */
public class AwsTest {
    public static void main(String[] args) throws Exception {
        // Prepare the request
        Request request = new Request(Method.GET,
                "http://s3.amazonaws.com/quotes/nelson");
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_AWS_S3, "44CF9590006BF252F707",
                "OtxrzxIsfpFjA7SwPzILwy8Bw21TLhquhboDYROV"));

        // Add some extra headers
        Series<Header> extraHeaders = new Series<Header>(Header.class);
        extraHeaders.add("X-Amz-Meta-Author", "foo@bar.com");
        extraHeaders.add("X-Amz-Magic", "abracadabra");

        // For the test we hard coded a special date header. Normally you don't
        // need this as the
        // HTTP client connector will automatically provide an accurate Date
        // header and use it
        // for authentication.
        // extraHeaders.add("X-Amz-Date", "Thu, 17 Nov 2005 18:49:58 GMT");
        request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                extraHeaders);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        output.write(System.out);
    }

}
