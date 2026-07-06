/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Conditions}. */
class ConditionsTestCase {

    @Test
    void newInstance_hasNoConditions() {
        Conditions conditions = new Conditions();
        assertFalse(conditions.hasSome());
        assertFalse(conditions.hasSomeRange());
        assertTrue(conditions.getMatch().isEmpty());
        assertTrue(conditions.getNoneMatch().isEmpty());
        assertNull(conditions.getModifiedSince());
        assertNull(conditions.getUnmodifiedSince());
        assertNull(conditions.getRangeDate());
        assertNull(conditions.getRangeTag());
    }

    @Test
    void setMatch_marksConditionsAsPresent() {
        Conditions conditions = new Conditions();
        conditions.setMatch(Collections.singletonList(Tag.ALL));
        assertTrue(conditions.hasSome());
        assertEquals(1, conditions.getMatch().size());
    }

    @Test
    void setNoneMatch_marksConditionsAsPresent() {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(Collections.singletonList(new Tag("abc")));
        assertTrue(conditions.hasSome());
    }

    @Test
    void setModifiedSince_marksConditionsAsPresent() {
        Conditions conditions = new Conditions();
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        conditions.setModifiedSince(new Date(instant.toEpochMilli()));
        assertTrue(conditions.hasSome());
        assertNotNull(conditions.getModifiedSince());
    }

    @Test
    void setUnmodifiedSince_marksConditionsAsPresent() {
        Conditions conditions = new Conditions();
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        conditions.setUnmodifiedSince(new Date(instant.toEpochMilli()));
        assertTrue(conditions.hasSome());
        assertNotNull(conditions.getUnmodifiedSince());
    }

    @Test
    void setRangeTag_marksRangeConditionsAsPresent() {
        Conditions conditions = new Conditions();
        conditions.setRangeTag(new Tag("abc"));
        assertTrue(conditions.hasSomeRange());
    }

    @Test
    void setRangeDate_marksRangeConditionsAsPresent() {
        Conditions conditions = new Conditions();
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        conditions.setRangeDate(new Date(instant.toEpochMilli()));
        assertTrue(conditions.hasSomeRange());
    }

    @Test
    void getRangeStatus_matchingTag_returnsOk() {
        Conditions conditions = new Conditions();
        Tag tag = new Tag("abc");
        conditions.setRangeTag(tag);
        assertEquals(Status.SUCCESS_OK, conditions.getRangeStatus(tag, null));
    }

    @Test
    void getRangeStatus_allTag_returnsOk() {
        Conditions conditions = new Conditions();
        conditions.setRangeTag(Tag.ALL);
        assertEquals(Status.SUCCESS_OK, conditions.getRangeStatus(new Tag("anything"), null));
    }

    @Test
    void getRangeStatus_nonMatchingTag_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        conditions.setRangeTag(new Tag("abc"));
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED,
                conditions.getRangeStatus(new Tag("other"), null));
    }

    @Test
    void getRangeStatus_matchingDate_returnsOk() {
        Conditions conditions = new Conditions();
        Date date = new Date(1000);
        conditions.setRangeDate(date);
        assertEquals(Status.SUCCESS_OK, conditions.getRangeStatus(null, date));
    }

    @Test
    void getRangeStatus_noConditionsSet_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, conditions.getRangeStatus(null, null));
    }

    @Test
    void getStatus_ifMatchNotSatisfied_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        conditions.setMatch(Collections.singletonList(new Tag("expected")));
        Status status = conditions.getStatus(Method.GET, true, new Tag("actual"), null);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, status);
    }

    @Test
    void getStatus_ifMatchSatisfied_returnsNull() {
        Conditions conditions = new Conditions();
        Tag tag = new Tag("abc");
        conditions.setMatch(Collections.singletonList(tag));
        Status status = conditions.getStatus(Method.GET, true, tag, null);
        assertNull(status);
    }

    @Test
    void getStatus_ifMatchStarWithNoEntity_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        conditions.setMatch(Collections.singletonList(Tag.ALL));
        Status status = conditions.getStatus(Method.GET, false, null, null);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED.getCode(), status.getCode());
    }

    @Test
    void getStatus_ifNoneMatchSatisfiedOnGet_returnsNotModified() {
        Conditions conditions = new Conditions();
        Tag tag = new Tag("abc");
        conditions.setNoneMatch(Collections.singletonList(tag));
        Status status = conditions.getStatus(Method.GET, true, tag, null);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, status);
    }

    @Test
    void getStatus_ifNoneMatchSatisfiedOnPut_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        Tag tag = new Tag("abc");
        conditions.setNoneMatch(Collections.singletonList(tag));
        Status status = conditions.getStatus(Method.PUT, true, tag, null);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, status);
    }

    @Test
    void getStatus_ifModifiedSinceInFuture_returnsNull() {
        Conditions conditions = new Conditions();
        Date future = new Date(System.currentTimeMillis() + 1_000_000_000L);
        conditions.setModifiedSince(future);
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        Status status =
                conditions.getStatus(Method.GET, true, null, new Date(instant.toEpochMilli()));
        assertNull(status);
    }

    @Test
    void getStatus_ifUnmodifiedSinceViolated_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        Date past = new Date(1000);
        conditions.setUnmodifiedSince(past);
        Date modificationDate = new Date(2000);
        Status status = conditions.getStatus(Method.GET, true, null, modificationDate);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, status);
    }

    @Test
    void getStatus_noConditions_returnsNull() {
        Conditions conditions = new Conditions();
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        assertNull(
                conditions.getStatus(
                        Method.GET, true, new Tag("abc"), new Date(instant.toEpochMilli())));
    }
}
