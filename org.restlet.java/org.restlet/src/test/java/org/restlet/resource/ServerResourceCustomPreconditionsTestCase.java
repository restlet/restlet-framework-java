/**
 * Copyright 2005-2026 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.Engine;
import org.restlet.representation.RepresentationInfo;
import org.restlet.resource.ServerResourcePreconditionsTestCase.ProbeResource;

/**
 * Tests of the extension of the preconditions evaluated by {@link ServerResource}, through {@link
 * ServerResource#hasPreconditions()} and {@link
 * ServerResource#getPreconditionStatus(RepresentationInfo)}.
 */
class ServerResourceCustomPreconditionsTestCase {

    private static final String IF_VERSION = "If-Version";

    /** Resource whose representation is at version "2", with an extra "If-Version" precondition. */
    static class VersionedResource extends ProbeResource {

        RepresentationInfo infoSeen;

        private String requestedVersion() {
            return getRequest().getHeaders().getFirstValue(IF_VERSION);
        }

        @Override
        protected boolean hasPreconditions() {
            return super.hasPreconditions() || requestedVersion() != null;
        }

        @Override
        protected Status getPreconditionStatus(RepresentationInfo info) {
            infoSeen = info;
            Status status = super.getPreconditionStatus(info);

            if (status == null && requestedVersion() != null && !"2".equals(requestedVersion())) {
                status = Status.CLIENT_ERROR_PRECONDITION_FAILED;
            }

            return status;
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

    private static VersionedResource putWithVersion(String version) {
        VersionedResource resource =
                ServerResourcePreconditionsTestCase.init(new VersionedResource(), Method.PUT);

        if (version != null) {
            resource.getRequest().getHeaders().add(new Header(IF_VERSION, version));
        }

        return resource;
    }

    @Test
    void customPreconditionAlone_whenItFails_answers412AndSkipsPut() {
        VersionedResource resource = putWithVersion("1");

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, resource.getStatus());
        assertEquals(0, resource.putCount);
    }

    @Test
    void customPreconditionAlone_whenItHolds_runsPut() {
        VersionedResource resource = putWithVersion("2");

        resource.handle();

        assertEquals(1, resource.putCount);
        assertEquals(1, resource.getCount);
    }

    @Test
    void customPrecondition_receivesTheInfoOfTheExistingResource() {
        VersionedResource resource = putWithVersion("2");

        resource.handle();

        assertEquals(CURRENT_TAG, resource.infoSeen.getTag());
    }

    @Test
    void customPrecondition_receivesNullForAMissingResource() {
        VersionedResource resource = putWithVersion("2");
        resource.setExisting(false);

        resource.handle();

        assertNull(resource.infoSeen);
        assertEquals(1, resource.putCount);
        assertEquals(0, resource.getCount);
    }

    @Test
    void standardPreconditions_areStillEvaluatedThroughSuper() {
        VersionedResource resource = putWithVersion("2");
        resource.getConditions().setMatch(List.of(new Tag("other", false)));

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, resource.getStatus());
        assertEquals(0, resource.putCount);
    }

    @Test
    void withoutAnyPrecondition_getIsNotCalled() {
        VersionedResource resource = putWithVersion(null);

        resource.handle();

        assertEquals(0, resource.getCount);
        assertEquals(1, resource.putCount);
    }

    private static final Tag CURRENT_TAG = new Tag("current", false);
}
