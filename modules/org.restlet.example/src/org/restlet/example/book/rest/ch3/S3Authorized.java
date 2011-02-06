/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.rest.ch3;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;

/**
 * Amazon S3 client. Support class handling authorized requests. Remember to
 * replace the access key id and secret below with your own values. For this you
 * need to sign-up with Amazon Web Services *and* with the S3 service.
 * 
 * @author Jerome Louvel
 */
public class S3Authorized {
    public final static String ACCESS_KEY_ID = "<REPLACE WITH YOUR OWN ID>";

    public final static String SECRET_ACCESS_KEY = "<REPLACE WITH YOUR OWN KEY>";

    public final static String HOST = "https://s3.amazonaws.com/";

    public static Response authorizedDelete(String uri) {
        return handleAuthorized(Method.DELETE, uri, null);
    }

    public static Response authorizedGet(String uri) {
        return handleAuthorized(Method.GET, uri, null);
    }

    public static Response authorizedHead(String uri) {
        return handleAuthorized(Method.HEAD, uri, null);
    }

    public static Response authorizedPut(String uri, Representation entity) {
        return handleAuthorized(Method.PUT, uri, entity);
    }

    private static Response handleAuthorized(Method method, String uri,
            Representation entity) {
        // Send an authenticated request
        final Request request = new Request(method, uri, entity);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_AWS_S3, ACCESS_KEY_ID, SECRET_ACCESS_KEY));
        return new Client(Protocol.HTTPS).handle(request);
    }
}
