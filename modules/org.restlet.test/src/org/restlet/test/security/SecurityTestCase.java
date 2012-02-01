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

package org.restlet.test.security;

import org.junit.After;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

/**
 * Restlet unit tests for the security package.
 * 
 * @author Jerome Louvel
 */
public class SecurityTestCase extends RestletTestCase {

    private Component component;

    @Before
    public void startComponent() throws Exception {
        this.component = new SaasComponent();
        this.component.start();
    }

    @After
    public void stopServer() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }

        this.component = null;
    }

    public void testSecurity() {
        try {
            startComponent();

            String uri = "http://localhost:" + TEST_PORT + "/test1";
            ClientResource resource = new ClientResource(uri);

            // TEST SERIES 1

            // Try without authentication
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, resource.getStatus());

            // Try with authentication
            resource.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, "stiger", "pwd"));
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.SUCCESS_OK, resource.getStatus());

            // TEST SERIES 2
            uri = "http://localhost:" + TEST_PORT + "/test2";
            resource = new ClientResource(uri);
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.SUCCESS_OK, resource.getStatus());

            // TEST SERIES 3
            uri = "http://localhost:" + TEST_PORT + "/test3";
            resource = new ClientResource(uri);
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());

            // TEST SERIES 4
            uri = "http://localhost:" + TEST_PORT + "/test4";
            resource = new ClientResource(uri);
            resource.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, "stiger", "pwd"));
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());

            // Try again with another user
            resource.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, "larmstrong", "pwd"));
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.SUCCESS_OK, resource.getStatus());

            // TEST SERIES 5
            uri = "http://localhost:" + TEST_PORT + "/test5";
            resource = new ClientResource(uri);
            resource.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, "stiger", "pwd"));
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.SUCCESS_OK, resource.getStatus());

            // Try again with another user
            resource.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, "larmstrong", "pwd"));
            try {
                resource.get();
            } catch (ResourceException e) {
            }
            resource.release();
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());

            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
