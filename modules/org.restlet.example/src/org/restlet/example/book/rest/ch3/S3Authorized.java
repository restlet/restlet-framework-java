/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.rest.ch3;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Amazon S3 client. Support class handling authorized requests.
 * 
 * @author Jerome Louvel
 */
public class S3Authorized {
    public final static String PUBLIC_KEY = "0F9DBXKB5274JKTJ8DG2";

    public final static String PRIVATE_KEY = "GuUHQ086WawbwvVl3JPl9JIk4VOtLcllkvIb0b7w";

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
                ChallengeScheme.HTTP_AWS_S3, PUBLIC_KEY, PRIVATE_KEY));
        return new Client(Protocol.HTTPS).handle(request);
    }
}
