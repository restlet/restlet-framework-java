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
import java.util.List;
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
        Tag tag = new Tag("abc", false);
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
        Tag tag = new Tag("abc", false);
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

    // ---------------------------------------------------------------------------------------
    // RFC 9110 section 13.1: comparison of the entity tags
    // ---------------------------------------------------------------------------------------

    private static final Tag STRONG_A = new Tag("a", false);

    private static final Tag WEAK_A = new Tag("a", true);

    private static final Date T0 = new Date(1_700_000_000_000L);

    private static Status ifMatch(Tag clientTag, Tag resourceTag) {
        Conditions conditions = new Conditions();
        conditions.setMatch(List.of(clientTag));
        return conditions.getStatus(Method.PUT, true, resourceTag, null);
    }

    private static Status ifNoneMatch(Method method, Tag clientTag, Tag resourceTag) {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(List.of(clientTag));
        return conditions.getStatus(method, true, resourceTag, null);
    }

    /**
     * Pins the 2.7.0 behavior of If-Match, kept in 2.7.1: the tags must have the same name and the
     * same weakness. RFC 9110 requires the strong comparison, applied in Restlet 3.0, where two
     * weak tags do not match.
     */
    @Test
    void getStatus_ifMatch_requiresTheSameNameAndTheSameWeakness() {
        assertNull(ifMatch(STRONG_A, new Tag("a", false)));
        assertNull(ifMatch(WEAK_A, WEAK_A));
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, ifMatch(STRONG_A, WEAK_A));
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, ifMatch(WEAK_A, STRONG_A));
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, ifMatch(STRONG_A, new Tag("b", false)));
    }

    @Test
    void getStatus_ifMatchStarAfterAnotherTag_matchesAnExistingResource() {
        Conditions conditions = new Conditions();
        conditions.setMatch(List.of(new Tag("b", false), Tag.ALL));
        assertNull(conditions.getStatus(Method.PUT, true, STRONG_A, null));
    }

    /** If-None-Match uses the weak comparison, whatever the method. */
    @Test
    void getStatus_ifNoneMatchWeakComparison_onGet_returnsNotModified() {
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, ifNoneMatch(Method.GET, WEAK_A, STRONG_A));
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, ifNoneMatch(Method.GET, STRONG_A, WEAK_A));
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, ifNoneMatch(Method.GET, WEAK_A, WEAK_A));
    }

    @Test
    void getStatus_ifNoneMatchWeakComparison_onPut_returnsPreconditionFailed() {
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, ifNoneMatch(Method.PUT, WEAK_A, STRONG_A));
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, ifNoneMatch(Method.PUT, STRONG_A, WEAK_A));
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, ifNoneMatch(Method.PUT, WEAK_A, WEAK_A));
        assertNull(ifNoneMatch(Method.PUT, new Tag("b", false), STRONG_A));
    }

    // ---------------------------------------------------------------------------------------
    // RFC 9110 section 13.1.2: "If-None-Match: *" is false when a representation exists
    // ---------------------------------------------------------------------------------------

    @Test
    void getStatus_ifNoneMatchStar_onTaggedResource_returnsPreconditionFailedOnPut() {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(List.of(Tag.ALL));
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED,
                conditions.getStatus(Method.PUT, true, STRONG_A, T0));
    }

    @Test
    void getStatus_ifNoneMatchStar_onTaggedResource_returnsNotModifiedOnGet() {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(List.of(Tag.ALL));
        assertEquals(
                Status.REDIRECTION_NOT_MODIFIED,
                conditions.getStatus(Method.GET, true, STRONG_A, T0));
    }

    @Test
    void getStatus_ifNoneMatchStar_onUntaggedResource_returnsPreconditionFailedOnPut() {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(List.of(Tag.ALL));
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED,
                conditions.getStatus(Method.PUT, true, null, T0));
    }

    @Test
    void getStatus_ifNoneMatchStar_withoutResource_lets_thePutCreateIt() {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(List.of(Tag.ALL));
        assertNull(conditions.getStatus(Method.PUT, false, null, null));
    }

    // ---------------------------------------------------------------------------------------
    // RFC 9110 section 13.1.5: If-Range
    // ---------------------------------------------------------------------------------------

    private static Status ifRange(Tag clientTag, Tag resourceTag) {
        Conditions conditions = new Conditions();
        conditions.setRangeTag(clientTag);
        return conditions.getRangeStatus(resourceTag, null);
    }

    /** The strong comparison is required: a weak tag, on either side, is never a match. */
    @Test
    void getRangeStatus_weakTags_returnPreconditionFailed() {
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, ifRange(WEAK_A, WEAK_A));
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, ifRange(STRONG_A, WEAK_A));
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, ifRange(WEAK_A, STRONG_A));
        assertEquals(Status.SUCCESS_OK, ifRange(STRONG_A, new Tag("a", false)));
    }

    @Test
    void getRangeStatus_tagAndResourceWithoutTag_returnsPreconditionFailed() {
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, ifRange(STRONG_A, null));
    }

    /**
     * The date of a header has a precision of one second, whereas the modification date of a file
     * has milliseconds: they must match.
     */
    @Test
    void getRangeStatus_dateWithMilliseconds_matchesTheHeaderDate() {
        Conditions conditions = new Conditions();
        conditions.setRangeDate(T0);
        assertEquals(
                Status.SUCCESS_OK, conditions.getRangeStatus(null, new Date(T0.getTime() + 123)));
    }

    @Test
    void getRangeStatus_anotherSecond_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        conditions.setRangeDate(T0);
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED,
                conditions.getRangeStatus(null, new Date(T0.getTime() + 1000)));
    }

    @Test
    void getRangeStatus_dateAndResourceWithoutDate_returnsPreconditionFailed() {
        Conditions conditions = new Conditions();
        conditions.setRangeDate(T0);
        assertEquals(
                Status.CLIENT_ERROR_PRECONDITION_FAILED, conditions.getRangeStatus(null, null));
    }
}
