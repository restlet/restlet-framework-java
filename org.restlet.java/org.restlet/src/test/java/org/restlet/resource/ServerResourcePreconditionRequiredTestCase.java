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

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.Engine;
import org.restlet.representation.RepresentationInfo;
import org.restlet.resource.ServerResourcePreconditionsTestCase.ProbeResource;

/**
 * Tests the recipe of the Javadoc of {@link ServerResource#getPreconditionStatus(
 * RepresentationInfo)}: a resource requiring conditional writes answers 428 (Precondition Required,
 * RFC 6585) to a write that has no precondition.
 */
class ServerResourcePreconditionRequiredTestCase {

    /** The recipe of the Javadoc. */
    static class ConditionalWritesResource extends ProbeResource {

        private boolean isWrite() {
            return Method.PUT.equals(getMethod())
                    || Method.PATCH.equals(getMethod())
                    || Method.DELETE.equals(getMethod());
        }

        @Override
        protected boolean hasPreconditions() {
            return super.hasPreconditions() || isWrite();
        }

        @Override
        protected Status getPreconditionStatus(RepresentationInfo info) {
            if (isWrite() && !getConditions().hasSome()) {
                return Status.CLIENT_ERROR_PRECONDITION_REQUIRED;
            }

            return super.getPreconditionStatus(info);
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

    private static ConditionalWritesResource resource(Method method) {
        return ServerResourcePreconditionsTestCase.init(new ConditionalWritesResource(), method);
    }

    @Test
    void writeWithoutPrecondition_answers428AndIsNotPerformed() {
        ConditionalWritesResource resource = resource(Method.PUT);

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_PRECONDITION_REQUIRED, resource.getStatus());
        assertEquals(0, resource.putCount);
    }

    @Test
    void deleteWithoutPrecondition_answers428() {
        ConditionalWritesResource resource = resource(Method.DELETE);

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_PRECONDITION_REQUIRED, resource.getStatus());
    }

    /** The cost of the recipe: the validators are retrieved before the 428 is decided. */
    @Test
    void writeWithoutPrecondition_callsGetOnceBeforeAnswering() {
        ConditionalWritesResource resource = resource(Method.PUT);

        resource.handle();

        assertEquals(1, resource.getCount);
    }

    @Test
    void writeWithAMatchingIfMatch_isPerformed() {
        ConditionalWritesResource resource = resource(Method.PUT);
        resource.getConditions().setMatch(List.of(new Tag("current", false)));

        resource.handle();

        assertEquals(1, resource.putCount);
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
    }

    @Test
    void writeWithAFailingIfMatch_answers412_asWithoutTheRecipe() {
        ConditionalWritesResource resource = resource(Method.PUT);
        resource.getConditions().setMatch(List.of(new Tag("other", false)));

        resource.handle();

        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, resource.getStatus());
        assertEquals(0, resource.putCount);
    }

    @Test
    void readWithoutPrecondition_isNotAffected() {
        ConditionalWritesResource resource = resource(Method.GET);

        resource.handle();

        assertEquals(Status.SUCCESS_OK, resource.getStatus());
        assertEquals(1, resource.getCount);
    }
}
