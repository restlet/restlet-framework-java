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

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;

/**
 * Tests of the evaluation of the request preconditions (RFC 9110 section 13) by {@link
 * ServerResource}: the order of the calls to the resource, and the status that the handler may have
 * set before the preconditions are evaluated (RFC 9110 section 13.2.1).
 */
class ServerResourcePreconditionsTestCase {

    private static final Tag CURRENT = new Tag("current", false);

    private static final Tag OTHER = new Tag("other", false);

    /** Resource counting its calls, whose {@code get()} may leave a status like a handler would. */
    static class ProbeResource extends ServerResource {

        int getCount;

        int putCount;

        /** Status that {@code get()} sets before returning its representation. */
        Status statusSetByGet;

        /** Location that {@code get()} sets, for a redirection. */
        String locationSetByGet;

        @Override
        protected Representation get() {
            getCount++;

            if (statusSetByGet != null) {
                setStatus(statusSetByGet);
            }

            if (locationSetByGet != null) {
                setLocationRef(locationSetByGet);
            }

            StringRepresentation result = new StringRepresentation("body");
            result.setTag(CURRENT);
            return result;
        }

        @Override
        protected Representation put(Representation entity) {
            putCount++;
            return new StringRepresentation("put-result");
        }
    }

    /** Resource whose {@code getInfo()} is lighter than {@code get()}, as the API advises. */
    static class LightInfoResource extends ProbeResource {

        int getInfoCount;

        @Override
        protected RepresentationInfo getInfo() {
            getInfoCount++;
            RepresentationInfo info = new RepresentationInfo();
            info.setTag(CURRENT);
            return info;
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

    static <T extends ServerResource> T init(T resource, Method method) {
        Request request = new Request(method, "http://localhost/test");
        resource.init(new Context(), request, new Response(request));
        return resource;
    }

    // ---------------------------------------------------------------------------------------
    // The flow as it is (must keep passing)
    // ---------------------------------------------------------------------------------------

    @Test
    void put_withoutPreconditions_doesNotCallGet() {
        ProbeResource resource = init(new ProbeResource(), Method.PUT);

        resource.handle();

        assertEquals(0, resource.getCount);
        assertEquals(1, resource.putCount);
    }

    @Test
    void put_withFailingIfMatch_answers412AndSkipsPut() {
        ProbeResource resource = init(new ProbeResource(), Method.PUT);
        resource.getConditions().setMatch(List.of(OTHER));

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, resource.getStatus());
        assertEquals(0, resource.putCount);
    }

    @Test
    void get_withMatchingIfNoneMatch_answers304() {
        ProbeResource resource = init(new ProbeResource(), Method.GET);
        resource.getConditions().setNoneMatch(List.of(CURRENT));

        resource.handle();

        assertEquals(Status.REDIRECTION_NOT_MODIFIED, resource.getStatus());
    }

    @Test
    void put_withIfNoneMatchAll_onMissingResource_runsPut() {
        ProbeResource resource = init(new ProbeResource(), Method.PUT);
        resource.setExisting(false);
        resource.getConditions().setNoneMatch(List.of(Tag.ALL));

        resource.handle();

        assertEquals(0, resource.getCount);
        assertEquals(1, resource.putCount);
    }

    // ---------------------------------------------------------------------------------------
    // #262: how many times the resource is called
    // ---------------------------------------------------------------------------------------

    @Test
    void conditionalPut_callsGetOnce_toReadTheValidators() {
        ProbeResource resource = init(new ProbeResource(), Method.PUT);
        resource.getConditions().setMatch(List.of(CURRENT));

        resource.handle();

        assertEquals(1, resource.getCount);
        assertEquals(1, resource.putCount);
    }

    @Test
    void conditionalGet_reusesTheRepresentationObtainedForTheValidators() {
        ProbeResource resource = init(new ProbeResource(), Method.GET);
        resource.getConditions().setNoneMatch(List.of(OTHER));

        resource.handle();

        assertEquals(1, resource.getCount);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    void conditionalGet_withLightGetInfo_neverBuildsTheRepresentationOnA304() {
        LightInfoResource resource = init(new LightInfoResource(), Method.GET);
        resource.getConditions().setNoneMatch(List.of(CURRENT));

        resource.handle();

        assertEquals(Status.REDIRECTION_NOT_MODIFIED, resource.getStatus());
        assertEquals(1, resource.getInfoCount);
        assertEquals(0, resource.getCount);
    }

    /**
     * By design: a lighter {@code getInfo()} is followed by {@code get()} when the call goes on.
     */
    @Test
    void conditionalGet_withLightGetInfo_callsGetOnceMoreWhenTheConditionsPass() {
        LightInfoResource resource = init(new LightInfoResource(), Method.GET);
        resource.getConditions().setNoneMatch(List.of(OTHER));

        resource.handle();

        assertEquals(Status.SUCCESS_OK, resource.getStatus());
        assertEquals(1, resource.getInfoCount);
        assertEquals(1, resource.getCount);
    }

    // ---------------------------------------------------------------------------------------
    // #129: the status set while retrieving the validators (RFC 9110 section 13.2.1)
    // ---------------------------------------------------------------------------------------

    @Test
    void get_whenHandlerSetAnError_keepsItInsteadOfA304() {
        ProbeResource resource = init(new ProbeResource(), Method.GET);
        resource.statusSetByGet = Status.CLIENT_ERROR_CONFLICT;
        resource.getConditions().setNoneMatch(List.of(CURRENT));

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_CONFLICT, resource.getStatus());
    }

    @Test
    void put_whenHandlerSetAnError_keepsItInsteadOfA412_andSkipsPut() {
        ProbeResource resource = init(new ProbeResource(), Method.PUT);
        resource.statusSetByGet = Status.CLIENT_ERROR_FORBIDDEN;
        resource.getConditions().setMatch(List.of(OTHER));

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, resource.getStatus());
        assertEquals(0, resource.putCount);
    }

    @Test
    void get_whenHandlerSetARedirection_keepsItInsteadOfA304() {
        ProbeResource resource = init(new ProbeResource(), Method.GET);
        resource.statusSetByGet = Status.REDIRECTION_FOUND;
        resource.locationSetByGet = "http://localhost/elsewhere";
        resource.getConditions().setNoneMatch(List.of(CURRENT));

        resource.handle();

        assertEquals(Status.REDIRECTION_FOUND, resource.getStatus());
    }

    @Test
    void get_whenHandlerSetAnotherSuccess_stillEvaluatesThePreconditions() {
        ProbeResource resource = init(new ProbeResource(), Method.GET);
        resource.statusSetByGet = Status.SUCCESS_ACCEPTED;
        resource.getConditions().setNoneMatch(List.of(CURRENT));

        resource.handle();

        assertEquals(Status.REDIRECTION_NOT_MODIFIED, resource.getStatus());
    }
}
