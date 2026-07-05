/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit tests for the {@link ServerResource} abstract class, exercised through minimal local
 * subclasses.
 *
 * @author Jerome Louvel
 */
class ServerResourceTestCase {

    /** Server resource with no overridden methods: exercises default behaviors. */
    private static class PlainServerResource extends ServerResource {}

    /** Server resource overriding the standard uniform interface methods. */
    private static class EchoServerResource extends ServerResource {

        volatile boolean putInvoked;

        volatile boolean deleteInvoked;

        @Override
        protected Representation get() {
            return new StringRepresentation("get-result");
        }

        @Override
        protected Representation put(Representation entity) {
            putInvoked = true;
            return new StringRepresentation("put-result");
        }

        @Override
        protected Representation delete() {
            deleteInvoked = true;
            return new StringRepresentation("delete-result");
        }
    }

    @BeforeEach
    void setUpEach() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
    }

    @AfterEach
    void tearDownEach() {
        Engine.clearThreadLocalVariables();
    }

    private static EchoServerResource createInitializedResource(Method method) {
        EchoServerResource resource = new EchoServerResource();
        Context context = new Context();
        Request request = new Request(method, "http://localhost/test");
        Response response = new Response(request);
        resource.init(context, request, response);
        return resource;
    }

    @Test
    void defaultFlags_areTrueByDefault() {
        PlainServerResource resource = new PlainServerResource();

        assertTrue(resource.isAnnotated());
        assertTrue(resource.isConditional());
        assertTrue(resource.isExisting());
        assertTrue(resource.isNegotiated());
    }

    @Test
    void nameAndDescription_roundTrip() {
        PlainServerResource resource = new PlainServerResource();

        resource.setName("myResource");
        resource.setDescription("myDescription");

        assertEquals("myResource", resource.getName());
        assertEquals("myDescription", resource.getDescription());
    }

    @Test
    void setAnnotated_updatesFlag() {
        PlainServerResource resource = new PlainServerResource();

        resource.setAnnotated(false);

        assertFalse(resource.isAnnotated());
    }

    @Test
    void setConditional_updatesFlag() {
        PlainServerResource resource = new PlainServerResource();

        resource.setConditional(false);

        assertFalse(resource.isConditional());
    }

    @Test
    void setExisting_updatesFlag() {
        PlainServerResource resource = new PlainServerResource();

        resource.setExisting(false);

        assertFalse(resource.isExisting());
    }

    @Test
    void setNegotiated_updatesFlag() {
        PlainServerResource resource = new PlainServerResource();

        resource.setNegotiated(false);

        assertFalse(resource.isNegotiated());
    }

    @Test
    void getAttribute_readsFromRequestAttributes() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        resource.getRequestAttributes().put("key", "value");

        assertEquals("value", resource.getAttribute("key"));
    }

    @Test
    void getAttribute_returnsNullWhenMissing() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        assertNull(resource.getAttribute("missing"));
    }

    @Test
    void setAttribute_writesToResponseAttributes() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setAttribute("key", "value");

        assertEquals("value", resource.getResponseAttributes().get("key"));
    }

    @Test
    void handle_withGetMethod_dispatchesToGetAndSetsEntity() throws Exception {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.handle();

        assertEquals(Status.SUCCESS_OK, resource.getStatus());
        assertEquals("get-result", resource.getResponseEntity().getText());
    }

    @Test
    void handle_withPutMethod_dispatchesToPutRegardlessOfExisting() throws Exception {
        EchoServerResource resource = createInitializedResource(Method.PUT);
        resource.setExisting(false);

        resource.handle();

        assertTrue(resource.putInvoked);
        assertEquals("put-result", resource.getResponseEntity().getText());
    }

    @Test
    void handle_withDeleteMethod_dispatchesToDelete() throws Exception {
        EchoServerResource resource = createInitializedResource(Method.DELETE);

        resource.handle();

        assertTrue(resource.deleteInvoked);
        assertEquals("delete-result", resource.getResponseEntity().getText());
    }

    @Test
    void handle_withUnsupportedMethodOnPlainResource_setsMethodNotAllowed() {
        PlainServerResource resource = new PlainServerResource();
        Context context = new Context();
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        resource.init(context, request, response);

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, resource.getStatus());
    }

    @Test
    void handle_whenNotExistingAndMethodSafe_setsNotFound() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        resource.setExisting(false);

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, resource.getStatus());
    }

    @Test
    void doError_setsResponseStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.doError(Status.CLIENT_ERROR_CONFLICT);

        assertEquals(Status.CLIENT_ERROR_CONFLICT, resource.getStatus());
    }

    @Test
    void isInRole_returnsFalseWhenClientHasNoRoles() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        assertFalse(resource.isInRole("admin"));
    }

    @Test
    void getRole_createsRoleWithGivenName() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        assertEquals("admin", resource.getRole("admin").getName());
    }

    @Test
    void redirectPermanent_withStringUri_setsLocationAndStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.redirectPermanent("http://localhost/target");

        assertEquals(Status.REDIRECTION_PERMANENT, resource.getStatus());
        assertEquals("http://localhost/target", resource.getLocationRef().toString());
    }

    @Test
    void redirectPermanent_withReference_setsLocationAndStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.redirectPermanent(new org.restlet.data.Reference("http://localhost/target"));

        assertEquals(Status.REDIRECTION_PERMANENT, resource.getStatus());
    }

    @Test
    void redirectSeeOther_withStringUri_setsLocationAndStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.redirectSeeOther("http://localhost/target");

        assertEquals(Status.REDIRECTION_SEE_OTHER, resource.getStatus());
    }

    @Test
    void redirectSeeOther_withReference_setsLocationAndStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.redirectSeeOther(new org.restlet.data.Reference("http://localhost/target"));

        assertEquals(Status.REDIRECTION_SEE_OTHER, resource.getStatus());
    }

    @Test
    void redirectTemporary_withStringUri_setsLocationAndStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.redirectTemporary("http://localhost/target");

        assertEquals(Status.REDIRECTION_TEMPORARY, resource.getStatus());
    }

    @Test
    void redirectTemporary_withReference_setsLocationAndStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.redirectTemporary(new org.restlet.data.Reference("http://localhost/target"));

        assertEquals(Status.REDIRECTION_TEMPORARY, resource.getStatus());
    }

    @Test
    void setLocationRef_withString_setsLocation() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setLocationRef("http://localhost/other");

        assertEquals("http://localhost/other", resource.getLocationRef().toString());
    }

    @Test
    void setStatus_statusOnly_updatesResponseStatus() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setStatus(Status.CLIENT_ERROR_FORBIDDEN);

        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
    }

    @Test
    void setStatus_statusAndMessage_setsReasonPhrase() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "custom message");

        assertEquals("custom message", resource.getStatus().getDescription());
    }

    @Test
    void setStatus_statusAndThrowable_attachesThrowable() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        Exception cause = new IllegalStateException("boom");

        resource.setStatus(Status.SERVER_ERROR_INTERNAL, cause);

        assertEquals(cause, resource.getStatus().getThrowable());
    }

    @Test
    void setStatus_statusThrowableAndMessage_setsBoth() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        Exception cause = new IllegalStateException("boom");

        resource.setStatus(Status.SERVER_ERROR_INTERNAL, cause, "custom message");

        assertEquals(cause, resource.getStatus().getThrowable());
        assertEquals("custom message", resource.getStatus().getReasonPhrase());
    }

    @Test
    void commit_delegatesToResponseWithoutThrowing() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        assertFalse(resource.isCommitted());

        resource.commit();

        assertFalse(resource.isCommitted());
    }

    @Test
    void setCommitted_updatesFlag() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setCommitted(true);

        assertTrue(resource.isCommitted());
    }

    @Test
    void isAutoCommitting_defaultsToTrue() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        assertTrue(resource.isAutoCommitting());
    }

    @Test
    void setAutoCommitting_updatesFlag() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setAutoCommitting(false);

        assertFalse(resource.isAutoCommitting());
    }

    @Test
    void abort_delegatesToResponseWithoutThrowing() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.abort();

        assertNotNull(resource.getResponse());
    }

    @Test
    void setAllowedMethods_updatesAllowedMethodsSet() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setAllowedMethods(java.util.Set.of(Method.GET, Method.POST));

        assertTrue(resource.getResponse().getAllowedMethods().contains(Method.GET));
        assertTrue(resource.getResponse().getAllowedMethods().contains(Method.POST));
    }

    @Test
    void setServerInfo_updatesResponseServerInfo() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        org.restlet.data.ServerInfo serverInfo = new org.restlet.data.ServerInfo();
        serverInfo.setAgent("test-agent");

        resource.setServerInfo(serverInfo);

        assertEquals("test-agent", resource.getResponse().getServerInfo().getAgent());
    }

    @Test
    void setCookieSettings_updatesResponseCookieSettings() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        org.restlet.util.Series<org.restlet.data.CookieSetting> cookieSettings =
                new org.restlet.util.Series<>(org.restlet.data.CookieSetting.class);
        cookieSettings.add(new org.restlet.data.CookieSetting(0, "name", "value"));

        resource.setCookieSettings(cookieSettings);

        assertEquals(1, resource.getResponse().getCookieSettings().size());
    }

    @Test
    void setDimensions_updatesResponseDimensions() {
        EchoServerResource resource = createInitializedResource(Method.GET);

        resource.setDimensions(java.util.Set.of(org.restlet.data.Dimension.CHARACTER_SET));

        assertTrue(
                resource.getResponse()
                        .getDimensions()
                        .contains(org.restlet.data.Dimension.CHARACTER_SET));
    }

    @Test
    void setChallengeRequests_updatesResponseChallengeRequests() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        java.util.List<org.restlet.data.ChallengeRequest> requests =
                java.util.List.of(
                        new org.restlet.data.ChallengeRequest(
                                org.restlet.data.ChallengeScheme.HTTP_BASIC));

        resource.setChallengeRequests(requests);

        assertEquals(1, resource.getResponse().getChallengeRequests().size());
    }

    @Test
    void setProxyChallengeRequests_updatesResponseProxyChallengeRequests() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        java.util.List<org.restlet.data.ChallengeRequest> requests =
                java.util.List.of(
                        new org.restlet.data.ChallengeRequest(
                                org.restlet.data.ChallengeScheme.HTTP_BASIC));

        resource.setProxyChallengeRequests(requests);

        assertEquals(1, resource.getResponse().getProxyChallengeRequests().size());
    }

    @Test
    void getOnSentAndSetOnSent_roundTrip() {
        EchoServerResource resource = createInitializedResource(Method.GET);
        org.restlet.Uniform callback = (request, response) -> {};

        resource.setOnSent(callback);

        assertEquals(callback, resource.getOnSent());
    }
}
