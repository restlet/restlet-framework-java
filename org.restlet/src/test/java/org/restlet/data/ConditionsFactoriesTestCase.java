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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.StringRepresentation;

/** Tests the client-side factories of {@link Conditions} (#100). */
class ConditionsFactoriesTestCase {

    private static final Date LAST_MODIFIED = new Date(1_700_000_000_000L);

    private static Response read(Tag tag, Date modified, Date responseDate) {
        Response response = new Response(new Request());
        StringRepresentation entity = new StringRepresentation("state");
        entity.setTag(tag);
        entity.setModificationDate(modified);
        response.setEntity(entity);
        response.setDate(responseDate);
        return response;
    }

    @Test
    void unchangedSince_strongTag_usesIfMatchOnly() {
        Tag tag = new Tag("v1", false);
        Conditions c =
                Conditions.unchangedSince(
                        read(tag, LAST_MODIFIED, new Date(LAST_MODIFIED.getTime() + 60_000)));

        assertEquals(1, c.getMatch().size());
        assertSame(tag, c.getMatch().get(0));
        assertNull(c.getUnmodifiedSince());
    }

    @Test
    void unchangedSince_weakTagAndDateOneSecondOlderThanResponseDate_usesIfUnmodifiedSince() {
        Conditions c =
                Conditions.unchangedSince(
                        read(
                                new Tag("v1", true),
                                LAST_MODIFIED,
                                new Date(LAST_MODIFIED.getTime() + 1000)));

        assertTrue(c.getMatch().isEmpty());
        assertEquals(LAST_MODIFIED, c.getUnmodifiedSince());
    }

    @Test
    void unchangedSince_noTagAndDateOlderThanResponseDate_usesIfUnmodifiedSince() {
        Conditions c =
                Conditions.unchangedSince(
                        read(null, LAST_MODIFIED, new Date(LAST_MODIFIED.getTime() + 5000)));

        assertTrue(c.getMatch().isEmpty());
        assertEquals(LAST_MODIFIED, c.getUnmodifiedSince());
    }

    @Test
    void unchangedSince_weakTagAndDateWithinOneSecondOfResponseDate_isRejected() {
        Response read =
                read(new Tag("v1", true), LAST_MODIFIED, new Date(LAST_MODIFIED.getTime() + 999));

        assertThrows(IllegalArgumentException.class, () -> Conditions.unchangedSince(read));
    }

    @Test
    void unchangedSince_weakTagAndNoResponseDate_isRejected() {
        Response read = read(new Tag("v1", true), LAST_MODIFIED, null);

        assertThrows(IllegalArgumentException.class, () -> Conditions.unchangedSince(read));
    }

    @Test
    void unchangedSince_noValidator_isRejected() {
        Response read = read(null, null, new Date());

        assertThrows(IllegalArgumentException.class, () -> Conditions.unchangedSince(read));
    }

    @Test
    void unchangedSince_noEntity_isRejected() {
        Response read = new Response(new Request());

        assertThrows(IllegalArgumentException.class, () -> Conditions.unchangedSince(read));
    }

    @Test
    void unchangedSince_representationWithStrongTag_usesIfMatch() {
        StringRepresentation entity = new StringRepresentation("state");
        entity.setTag(new Tag("v1", false));

        Conditions c = Conditions.unchangedSince(entity);

        assertEquals(1, c.getMatch().size());
        assertFalse(c.getMatch().get(0).isWeak());
    }

    @Test
    void unchangedSince_representationWithWeakTag_isRejected() {
        StringRepresentation entity = new StringRepresentation("state");
        entity.setTag(new Tag("v1", true));

        assertThrows(IllegalArgumentException.class, () -> Conditions.unchangedSince(entity));
    }

    @Test
    void ifAbsent_setsIfNoneMatchStarOnly() {
        Conditions c = Conditions.ifAbsent();

        assertEquals(1, c.getNoneMatch().size());
        assertEquals(Tag.ALL, c.getNoneMatch().get(0));
        assertTrue(c.getMatch().isEmpty());
        assertNull(c.getUnmodifiedSince());
    }
}
