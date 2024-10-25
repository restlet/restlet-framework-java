/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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


    private final ChallengeResponse lambdaUserCR = new ChallengeResponse(
            ChallengeScheme.HTTP_BASIC, "stiger", "pwd");
    private final ChallengeResponse adminUserCR = new ChallengeResponse(
            ChallengeScheme.HTTP_BASIC, "larmstrong", "pwd");

    private Component component;

    @BeforeEach
    public void startComponent() throws Exception {
        this.component = new SaasComponent();
        this.component.start();
    }

    @AfterEach
    public void stopServer() throws Exception {
        if (this.component.isStarted()) {
            this.component.stop();
        }

        this.component = null;
    }

    @Test
    public void withoutAuthenticationHttpBasicAuthenticatorShouldReturnUnauthorizedResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/httpBasicAuthenticator");
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, resource.getStatus());
    }

    @Test
    public void withAuthenticationHttpBasicAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/httpBasicAuthenticator");
        resource.setChallengeResponse(lambdaUserCR);
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    public void withoutAuthenticationAlwaysAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/alwaysAuthenticator");
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    public void withAuthenticationNeverAuthenticatorShouldReturnForbiddenResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/neverAuthenticator");
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    public void withLambdaUserAuthenticationAdminRoleAuthorizerAuthenticatorShouldReturnForbiddenResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/adminRoleAuthorizer");
        resource.setChallengeResponse(lambdaUserCR);
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    public void withAdminUserAuthenticationAdminRoleAuthorizerAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/adminRoleAuthorizer");
        resource.setChallengeResponse(adminUserCR);
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    public void withAdminUserAuthenticationAdminRoleForbiddenAuthorizerAuthenticatorShouldReturnForbiddenResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/adminRoleForbiddenAuthorizer");
        resource.setChallengeResponse(adminUserCR);
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    public void withLambdaUserAuthenticationAdminRoleForbiddenAuthorizerAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + TEST_PORT + "/adminRoleForbiddenAuthorizer");
        resource.setChallengeResponse(lambdaUserCR);
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    private static void runClientResource(ClientResource resource) {
        try {
            resource.get();
        } catch (ResourceException e) {}
        resource.release();
    }

}
