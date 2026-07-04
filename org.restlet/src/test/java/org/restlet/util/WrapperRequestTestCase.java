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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link WrapperRequest}. */
class WrapperRequestTestCase {

    private Request wrapped;

    private WrapperRequest wrapper;

    @BeforeEach
    void setUp() {
        wrapped = new Request();
        wrapper = new WrapperRequest(wrapped);
    }

    @Test
    void abort_delegatesToWrappedRequest() {
        assertFalse(wrapper.abort());
    }

    @Test
    void commit_delegatesToWrappedRequest() {
        wrapper.commit(new Response(wrapped));
    }

    @Test
    void attributesAndSimpleGetters_delegateToWrappedRequest() throws Exception {
        wrapped.getAttributes().put("key", "value");
        assertSame(wrapped.getAttributes(), wrapper.getAttributes());

        ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        wrapper.setChallengeResponse(challengeResponse);
        assertSame(challengeResponse, wrapper.getChallengeResponse());
        assertSame(wrapped.getChallengeResponse(), wrapper.getChallengeResponse());

        assertSame(wrapped.getClientInfo(), wrapper.getClientInfo());
        assertSame(wrapped.getConditions(), wrapper.getConditions());

        wrapper.setCookies(wrapper.getCookies());
        wrapper.getCookies().add(new Cookie("foo", "bar"));
        assertSame(wrapped.getCookies(), wrapper.getCookies());

        wrapper.setEntity(new StringRepresentation("text"));
        assertEquals("text", wrapper.getEntity().getText());

        wrapper.setEntity("value", MediaType.TEXT_PLAIN);
        assertEquals("value", wrapped.getEntity().getText());

        wrapper.setHostRef("http://localhost");
        assertEquals("http://localhost", wrapper.getHostRef().toString());
        wrapper.setHostRef(new Reference("http://example.com"));
        assertEquals("http://example.com", wrapped.getHostRef().toString());

        wrapper.setMaxForwards(3);
        assertEquals(3, wrapper.getMaxForwards());

        wrapper.setMethod(Method.PUT);
        assertEquals(Method.PUT, wrapper.getMethod());

        wrapper.setOnResponse(null);
        assertEquals(wrapped.getOnResponse(), wrapper.getOnResponse());

        wrapper.setOriginalRef(new Reference("http://original"));
        assertEquals("http://original", wrapper.getOriginalRef().toString());

        wrapper.setProtocol(Protocol.HTTP);
        assertEquals(Protocol.HTTP, wrapper.getProtocol());

        ChallengeResponse proxyChallenge = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        wrapper.setProxyChallengeResponse(proxyChallenge);
        assertSame(proxyChallenge, wrapper.getProxyChallengeResponse());

        wrapper.setRanges(List.of(new Range(0, 10)));
        assertEquals(1, wrapper.getRanges().size());

        wrapper.setReferrerRef("http://referrer");
        assertEquals("http://referrer", wrapper.getReferrerRef().toString());
        wrapper.setReferrerRef(new Reference("http://referrer2"));
        assertEquals("http://referrer2", wrapper.getReferrerRef().toString());

        wrapper.setResourceRef("http://resource");
        assertEquals("http://resource", wrapper.getResourceRef().toString());
        wrapper.setResourceRef(new Reference("http://resource2"));
        assertEquals("http://resource2", wrapper.getResourceRef().toString());

        wrapper.setRootRef(new Reference("http://root"));
        assertEquals("http://root", wrapper.getRootRef().toString());

        wrapper.setAccessControlRequestHeaders(Set.of("X-Test"));
        assertEquals(Set.of("X-Test"), wrapper.getAccessControlRequestHeaders());

        wrapper.setAccessControlRequestMethod(Method.DELETE);
        assertEquals(Method.DELETE, wrapper.getAccessControlRequestMethod());

        assertFalse(wrapper.isAsynchronous());
        assertFalse(wrapper.isConfidential());
        assertTrue(wrapper.isEntityAvailable());
        assertTrue(wrapper.isExpectingResponse());
        assertTrue(wrapper.isSynchronous());

        assertEquals(wrapped.toString(), wrapper.toString());
    }

    @Test
    void setClientInfoConditionsCookies_delegateToWrappedRequest() {
        var clientInfo = new org.restlet.data.ClientInfo();
        wrapper.setClientInfo(clientInfo);
        assertSame(clientInfo, wrapped.getClientInfo());

        var conditions = new org.restlet.data.Conditions();
        wrapper.setConditions(conditions);
        assertSame(conditions, wrapped.getConditions());
    }
}
