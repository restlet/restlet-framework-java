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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link Conditions#getStatus(Method, boolean, Tag, Date)} against an independent
 * implementation of the evaluation of the preconditions of RFC 9110 section 13.2.2, for every
 * combination of method, state of the resource, validators and conditional headers.
 *
 * <p>The only rule of {@link Conditions} that is not in RFC 9110 is the one of RFC 2616 section
 * 14.25: an "If-Modified-Since" date later than the current time is ignored. The combinations do
 * not use such a date; {@link ConditionsTestCase} covers it.
 */
class ConditionsRfc9110TestCase {

    /** Dates have a precision of one second, as the dates of the headers. */
    private static final long T0 = 1_700_000_000_000L;

    private static final Tag STRONG_A = new Tag("a", false);

    private static final Tag WEAK_A = new Tag("a", true);

    private static final Tag STRONG_B = new Tag("b", false);

    private record Case(
            Method method,
            boolean exists,
            Tag tag,
            Date modificationDate,
            List<Tag> ifMatch,
            List<Tag> ifNoneMatch,
            Date ifUnmodifiedSince,
            Date ifModifiedSince) {

        @Override
        public String toString() {
            return method
                    + (exists ? "" : " (no resource)")
                    + " tag="
                    + format(tag)
                    + " modified="
                    + (modificationDate == null ? "-" : "t0")
                    + " If-Match="
                    + format(ifMatch)
                    + " If-None-Match="
                    + format(ifNoneMatch)
                    + " If-Unmodified-Since="
                    + format(ifUnmodifiedSince)
                    + " If-Modified-Since="
                    + format(ifModifiedSince);
        }
    }

    private static String format(Tag tag) {
        return tag == null ? "-" : tag.format();
    }

    private static String format(List<Tag> tags) {
        return tags == null ? "-" : tags.stream().map(Tag::format).toList().toString();
    }

    private static String format(Date date) {
        return date == null ? "-" : "t0" + String.format("%+ds", (date.getTime() - T0) / 1000);
    }

    private static boolean containsStar(List<Tag> tags) {
        return tags.stream().anyMatch(t -> "*".equals(t.getName()));
    }

    private static boolean strongMatch(List<Tag> tags, Tag tag) {
        return tags.stream().anyMatch(t -> !"*".equals(t.getName()) && t.strongMatch(tag));
    }

    private static boolean weakMatch(List<Tag> tags, Tag tag) {
        return tags.stream().anyMatch(t -> !"*".equals(t.getName()) && t.weakMatch(tag));
    }

    private static long seconds(Date date) {
        return date.getTime() / 1000;
    }

    /** RFC 9110 section 13.2.2, for an origin server: 0 to proceed, otherwise the status code. */
    private static int expected(Case c) {
        boolean getOrHead = Method.GET.equals(c.method) || Method.HEAD.equals(c.method);

        // 1. If-Match, strong comparison
        if (c.ifMatch != null) {
            boolean holds = c.exists && (containsStar(c.ifMatch) || strongMatch(c.ifMatch, c.tag));

            if (!holds) {
                return 412;
            }
        } else if (c.ifUnmodifiedSince != null && c.modificationDate != null) {
            // 2. If-Unmodified-Since, ignored if there is an If-Match
            if (seconds(c.modificationDate) > seconds(c.ifUnmodifiedSince)) {
                return 412;
            }
        }

        // 3. If-None-Match, weak comparison
        if (c.ifNoneMatch != null) {
            boolean holds =
                    !(c.exists && (containsStar(c.ifNoneMatch) || weakMatch(c.ifNoneMatch, c.tag)));

            if (!holds) {
                return getOrHead ? 304 : 412;
            }
        } else if (getOrHead && c.ifModifiedSince != null && c.modificationDate != null) {
            // 4. If-Modified-Since, GET and HEAD only, ignored if there is an If-None-Match
            if (seconds(c.modificationDate) <= seconds(c.ifModifiedSince)) {
                return 304;
            }
        }

        return 0;
    }

    private static int actual(Case c) {
        Conditions conditions = new Conditions();

        if (c.ifMatch != null) {
            conditions.setMatch(new ArrayList<>(c.ifMatch));
        }

        if (c.ifNoneMatch != null) {
            conditions.setNoneMatch(new ArrayList<>(c.ifNoneMatch));
        }

        conditions.setUnmodifiedSince(c.ifUnmodifiedSince);
        conditions.setModifiedSince(c.ifModifiedSince);
        Status status = conditions.getStatus(c.method, c.exists, c.tag, c.modificationDate);
        return status == null ? 0 : status.getCode();
    }

    @Test
    void getStatus_forEveryCombination_followsRfc9110() {
        List<List<Tag>> tagLists =
                java.util.Arrays.asList(
                        null,
                        List.of(Tag.ALL),
                        List.of(STRONG_A),
                        List.of(WEAK_A),
                        List.of(STRONG_B));
        Date[] unmodifiedSince = {null, new Date(T0 - 10_000), new Date(T0), new Date(T0 + 10_000)};
        // All in the past: a date later than the current time is ignored (RFC 2616)
        Date[] modifiedSince = {null, new Date(T0 - 10_000), new Date(T0), new Date(T0 + 10_000)};
        Method[] methods = {Method.GET, Method.HEAD, Method.PUT, Method.POST, Method.DELETE};

        List<String> differences = new ArrayList<>();
        int total = 0;

        for (Method method : methods) {
            for (boolean exists : new boolean[] {true, false}) {
                for (Tag tag : exists ? new Tag[] {null, STRONG_A, WEAK_A} : new Tag[] {null}) {
                    for (Date date : exists ? new Date[] {null, new Date(T0)} : new Date[] {null}) {
                        for (List<Tag> ifMatch : tagLists) {
                            for (List<Tag> ifNoneMatch : tagLists) {
                                for (Date ius : unmodifiedSince) {
                                    for (Date ims : modifiedSince) {
                                        Case c =
                                                new Case(
                                                        method,
                                                        exists,
                                                        tag,
                                                        date,
                                                        ifMatch,
                                                        ifNoneMatch,
                                                        ius,
                                                        ims);
                                        total++;
                                        int expected = expected(c);
                                        int actual = actual(c);

                                        if (expected != actual) {
                                            differences.add(
                                                    "expected "
                                                            + expected
                                                            + " but was "
                                                            + actual
                                                            + ": "
                                                            + c);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        assertEquals(
                List.of(),
                differences.subList(0, Math.min(differences.size(), 10)),
                differences.size() + " of " + total + " combinations differ from RFC 9110");
    }
}
