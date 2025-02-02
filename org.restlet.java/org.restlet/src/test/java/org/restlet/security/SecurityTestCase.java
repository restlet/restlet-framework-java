/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Component;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.restlet.data.ChallengeScheme.HTTP_BASIC;

/**
 * Restlet unit tests for the security package.
 *
 * @author Jerome Louvel
 */
public class SecurityTestCase {

    private final ChallengeResponse lambdaUserCR = new ChallengeResponse(HTTP_BASIC, "stiger", "pwd");
    private final ChallengeResponse adminUserCR = new ChallengeResponse(HTTP_BASIC, "larmstrong", "pwd");

    private SaasComponent component;
    private int testPort;

    @BeforeEach
    public void startComponent() throws Exception {
        Engine.register();
        Engine.clearThreadLocalVariables();
        this.component = new SaasComponent();
        this.component.start();
        testPort = component.testPort;
    }

    @AfterEach
    public void stopServer() throws Exception {
        Engine.clearThreadLocalVariables();
        if (this.component.isStarted()) {
            this.component.stop();
        }

        this.component = null;
    }

    @Test
    public void withoutAuthenticationHttpBasicAuthenticatorShouldReturnUnauthorizedResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/httpBasicAuthenticator");
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, resource.getStatus());
    }

    @Test
    public void withAuthenticationHttpBasicAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/httpBasicAuthenticator");
        resource.setChallengeResponse(lambdaUserCR);
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    public void withoutAuthenticationAlwaysAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/alwaysAuthenticator");
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    public void withAuthenticationNeverAuthenticatorShouldReturnForbiddenResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/neverAuthenticator");
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    public void withLambdaUserAuthenticationAdminRoleAuthorizerAuthenticatorShouldReturnForbiddenResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/adminRoleAuthorizer");
        resource.setChallengeResponse(lambdaUserCR);
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    public void withAdminUserAuthenticationAdminRoleAuthorizerAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/adminRoleAuthorizer");
        resource.setChallengeResponse(adminUserCR);
        runClientResource(resource);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    public void withAdminUserAuthenticationAdminRoleForbiddenAuthorizerAuthenticatorShouldReturnForbiddenResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/adminRoleForbiddenAuthorizer");
        resource.setChallengeResponse(adminUserCR);
        runClientResource(resource);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    public void withLambdaUserAuthenticationAdminRoleForbiddenAuthorizerAuthenticatorShouldReturnOkResponse() {
        ClientResource resource = new ClientResource("http://localhost:" + testPort + "/adminRoleForbiddenAuthorizer");
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
