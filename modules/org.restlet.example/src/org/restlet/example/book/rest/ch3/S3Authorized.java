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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class S3Authorized {
    public final static String PUBLIC_KEY = "0F9DBXKB5274JKTJ8DG2";

    public final static String PRIVATE_KEY = "GuUHQ086WawbwvVl3JPl9JIk4VOtLcllkvIb0b7w";

    public final static String HOST = "https://s3.amazonaws.com/";

    private static Response handleAuthorized(Method method, String uri,
            Representation entity) {
        // Send an authenticated request
        Request request = new Request(method, uri, entity);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_AWS_S3, PUBLIC_KEY, PRIVATE_KEY));
        return new Client(Protocol.HTTPS).handle(request);
    }

    public static Response authorizedHead(String uri) {
        return handleAuthorized(Method.HEAD, uri, null);
    }

    public static Response authorizedGet(String uri) {
        return handleAuthorized(Method.GET, uri, null);
    }

    public static Response authorizedPut(String uri, Representation entity) {
        return handleAuthorized(Method.PUT, uri, entity);
    }

    public static Response authorizedDelete(String uri) {
        return handleAuthorized(Method.DELETE, uri, null);
    }
}
