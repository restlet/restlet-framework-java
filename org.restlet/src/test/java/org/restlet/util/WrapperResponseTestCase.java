/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.AuthenticationInfo;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Dimension;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link WrapperResponse}. */
class WrapperResponseTestCase {

    private Request request;

    private Response wrapped;

    private WrapperResponse wrapper;

    @BeforeEach
    void setUp() {
        request = new Request();
        wrapped = new Response(request);
        wrapper = new WrapperResponse(wrapped);
    }

    @Test
    void abortAndCommit_delegateToWrappedResponse() {
        wrapper.abort();
        wrapper.commit();
    }

    @Test
    void gettersAndSetters_delegateToWrappedResponse() throws Exception {
        wrapper.setAge(42);
        assertEquals(42, wrapper.getAge());

        wrapper.setAllowedMethods(Set.of(Method.GET));
        assertEquals(Set.of(Method.GET), wrapper.getAllowedMethods());

        wrapped.getAttributes().put("key", "value");
        assertSame(wrapped.getAttributes(), wrapper.getAttributes());

        AuthenticationInfo authInfo =
                new AuthenticationInfo("nextNonce", 1, "cnonce", "auth", "digest");
        wrapper.setAuthenticationInfo(authInfo);
        assertSame(authInfo, wrapper.getAuthenticationInfo());

        wrapper.setAutoCommitting(false);
        assertFalse(wrapper.isAutoCommitting());

        ChallengeRequest challengeRequest = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        wrapper.setChallengeRequests(List.of(challengeRequest));
        assertEquals(1, wrapper.getChallengeRequests().size());

        wrapper.getCookieSettings().add(new org.restlet.data.CookieSetting("foo", "bar"));
        assertSame(wrapped.getCookieSettings(), wrapper.getCookieSettings());

        wrapper.setCommitted(true);
        assertTrue(wrapper.isCommitted());

        wrapper.setDimensions(Set.of(Dimension.MEDIA_TYPE));
        assertEquals(Set.of(Dimension.MEDIA_TYPE), wrapper.getDimensions());

        wrapper.setEntity(new StringRepresentation("text"));
        assertEquals("text", wrapper.getEntity().getText());
        wrapper.setEntity("value", MediaType.TEXT_PLAIN);
        assertEquals("value", wrapped.getEntity().getText());

        wrapper.setLocationRef("http://localhost/loc");
        assertEquals("http://localhost/loc", wrapper.getLocationRef().toString());
        wrapper.setLocationRef(new Reference("http://localhost/loc2"));
        assertEquals("http://localhost/loc2", wrapper.getLocationRef().toString());

        wrapper.setProxyChallengeRequests(List.of(challengeRequest));
        assertEquals(1, wrapper.getProxyChallengeRequests().size());

        assertSame(request, wrapper.getRequest());
        wrapper.setRequest(request);
        assertSame(request, wrapped.getRequest());
        wrapper.setRequest(new WrapperRequest(request));
        assertTrue(wrapped.getRequest() instanceof WrapperRequest);

        Date retryAfter = new Date();
        wrapper.setRetryAfter(retryAfter);
        assertEquals(retryAfter, wrapper.getRetryAfter());

        ServerInfo serverInfo = new ServerInfo();
        wrapper.setServerInfo(serverInfo);
        assertSame(serverInfo, wrapper.getServerInfo());

        wrapper.setStatus(Status.SUCCESS_ACCEPTED);
        assertEquals(Status.SUCCESS_ACCEPTED, wrapper.getStatus());

        wrapper.setStatus(Status.SUCCESS_OK, "custom message");
        assertEquals(Status.SUCCESS_OK, wrapper.getStatus());

        wrapper.setStatus(Status.SERVER_ERROR_INTERNAL, new RuntimeException("boom"));
        assertEquals(Status.SERVER_ERROR_INTERNAL, wrapper.getStatus());

        wrapper.setStatus(Status.SERVER_ERROR_INTERNAL, new RuntimeException("boom"), "message");
        assertEquals(Status.SERVER_ERROR_INTERNAL, wrapper.getStatus());

        assertFalse(wrapper.isConfidential());
        assertTrue(wrapper.isEntityAvailable());

        assertEquals(wrapped.toString(), wrapper.toString());
    }

    @Test
    void redirectMethods_delegateToWrappedResponse() {
        wrapper.redirectPermanent("http://localhost/permanent");
        assertEquals(Status.REDIRECTION_PERMANENT, wrapped.getStatus());

        wrapper.redirectPermanent(new Reference("http://localhost/permanent2"));
        assertEquals(Status.REDIRECTION_PERMANENT, wrapped.getStatus());

        wrapper.redirectSeeOther("http://localhost/other");
        assertEquals(Status.REDIRECTION_SEE_OTHER, wrapped.getStatus());

        wrapper.redirectSeeOther(new Reference("http://localhost/other2"));
        assertEquals(Status.REDIRECTION_SEE_OTHER, wrapped.getStatus());

        wrapper.redirectTemporary("http://localhost/temp");
        assertEquals(Status.REDIRECTION_TEMPORARY, wrapped.getStatus());

        wrapper.redirectTemporary(new Reference("http://localhost/temp2"));
        assertEquals(Status.REDIRECTION_TEMPORARY, wrapped.getStatus());
    }
}
