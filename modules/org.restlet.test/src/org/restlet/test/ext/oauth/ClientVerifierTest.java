/**
 * Copyright 2005-2014 Restlet
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
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.test.ext.oauth;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.restlet.ext.oauth.OAuthResourceDefs.CLIENT_ID;
import static org.restlet.ext.oauth.OAuthResourceDefs.CLIENT_SECRET;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.ext.oauth.ClientVerifier;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.security.Verifier;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class ClientVerifierTest extends OAuthTestBase {

    private Context context;

    @Before
    public void setupContext() {
        context = new Context();
        context.getAttributes().put(ClientManager.class.getName(),
                new StubClientManager());
    }

    /**
     * Test case 1: Body-method auth is requested, but the verifier not accept
     * it.
     */
    @Test
    public void testCase1() {
        ClientVerifier verifier = new ClientVerifier(context);
        verifier.setAcceptBodyMethod(false);

        Request request = new Request();
        request.setChallengeResponse(null);
        Form form = new Form();
        form.set(CLIENT_ID, STUB_CLIENT_ID);
        form.set(CLIENT_SECRET, STUB_CLIENT_SECRET);
        request.setEntity(form.getWebRepresentation());

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_MISSING));
    }

    /**
     * Test case 2: Body-method auth is requested, but the client_id is missing.
     */
    @Test
    public void testCase2() {
        ClientVerifier verifier = new ClientVerifier(context);
        verifier.setAcceptBodyMethod(true);

        Request request = new Request();
        request.setChallengeResponse(null);
        Form form = new Form();
        // form.set(CLIENT_ID, TEST_CLIENT_ID);
        form.set(CLIENT_SECRET, STUB_CLIENT_SECRET);
        request.setEntity(form.getWebRepresentation());

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_MISSING));
    }

    /**
     * Test case 3: Unsupported ChallengeScheme.
     */
    @Test
    public void testCase3() {
        ClientVerifier verifier = new ClientVerifier(context);

        Request request = new Request();
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.CUSTOM));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_UNSUPPORTED));
    }

    /**
     * Test case 4: Unknown client_id.
     */
    @Test
    public void testCase4() {
        ClientVerifier verifier = new ClientVerifier(context);

        Request request = new Request();
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "foo", STUB_CLIENT_SECRET));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_UNKNOWN));
    }

    /**
     * Test case 5: Invalid client_secret.
     */
    @Test
    public void testCase5() {
        ClientVerifier verifier = new ClientVerifier(context);

        Request request = new Request();
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, STUB_CLIENT_ID, "bar"));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_INVALID));
    }

    /**
     * Test case 6: Valid authentication(Success).
     */
    @Test
    public void testCase6() {
        ClientVerifier verifier = new ClientVerifier(context);

        Request request = new Request();
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, STUB_CLIENT_ID, STUB_CLIENT_SECRET));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_VALID));
    }
}
