/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import java.io.IOException;
import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/**
 * Unit test for the Gson extension.
 *
 * @author Neal Mi
 */
class GsonTestCase {

    private record User(
            String loginId,
            String password,
            int rate,
            boolean active,
            Date createAt,
            @Since(2.0) Date lastLogin) {}

    private GsonConverter gsonConverter;

    private User user;

    @BeforeEach
    void setUpEach() {
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        Date date = new Date(instant.toEpochMilli());
        user = new User("hello", "secret", 1, true, date, date);
        gsonConverter = new GsonConverter();
    }

    @Test
    final void testCreateMediaTypeT() {
        Representation rep = new GsonRepresentation<>(user);

        assertNotNull(rep);
        assertEquals(MediaType.APPLICATION_JSON, rep.getMediaType());
    }

    @Test
    final void testCreateRepresentationClassOfT() {
        Representation rep = new GsonRepresentation<>(user);
        Representation rep1 = new GsonRepresentation<>(rep, User.class);

        assertEquals(rep.getMediaType(), rep1.getMediaType());
        assertEquals(rep.getClass(), rep1.getClass());
    }

    @Test
    final void testGsonRepresentationRead() throws IOException {
        final String userAsJsonString =
                "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}";
        Representation source =
                new StringRepresentation(userAsJsonString, MediaType.APPLICATION_JSON);

        GsonRepresentation<User> gsonRep = new GsonRepresentation<>(source, User.class);
        User parsedUser = gsonRep.getObject();

        assertNotNull(parsedUser);
        assertEquals("hello", parsedUser.loginId());
        assertEquals("secret", parsedUser.password());
        assertEquals(1, parsedUser.rate());
        assertTrue(parsedUser.active());
        DateTime time = new DateTime("2012-05-20T15:41:01.489+08:00");
        assertEquals(time.getMillis(), parsedUser.createAt().getTime());
        assertEquals(time.getMillis(), parsedUser.lastLogin().getTime());

        GsonRepresentation<User> gsonRep1 = new GsonRepresentation<>(source, User.class);
        gsonRep1.getBuilder().setVersion(1.0);

        User u1 = gsonRep1.getObject();
        assertNull(u1.lastLogin());
        assertEquals("hello", u1.loginId());
        assertEquals("secret", u1.password());
        assertEquals(1, u1.rate());
        assertTrue(u1.active());
        assertEquals(time.getMillis(), u1.createAt().getTime());
    }

    @Test
    final void testGsonRepresentationWrite() throws IOException {
        GsonRepresentation<User> source = new GsonRepresentation<>(user);

        assertEquals(User.class, source.getObjectClass());
        assertEquals(user, source.getObject());
        assertTrue(source.getText().contains("lastLogin"));

        source.getBuilder().setVersion(1.0);
        assertFalse(source.getText().contains("lastLogin"));

        String text = "What's going on?";
        GsonRepresentation<String> source1 = new GsonRepresentation<>(text);

        assertEquals(String.class, source1.getObjectClass());
        assertEquals(text, source1.getObject());
        assertEquals("\"What\\u0027s going on?\"", source1.getText());
    }

    @Test
    final void testScoreObjectVariantResource() {
        Variant v = new Variant(MediaType.APPLICATION_JSON);
        Representation source = new GsonRepresentation<>(user);

        float score = gsonConverter.score(user, v, null);
        assertEquals(0.8F, score);

        float score1 = gsonConverter.score(source, v, null);
        assertEquals(1.0F, score1);
    }

    @Test
    final void testScoreRepresentationClassOfTResource() {
        Representation source = new GsonRepresentation<User>(user);

        float score = gsonConverter.score(source, User.class, null);
        assertEquals(1.0F, score);

        Representation source1 = new EmptyRepresentation();

        float score1 = gsonConverter.score(source1, User.class, null);
        assertEquals(0.8F, score1);
    }

    @Test()
    final void testToObjectRepresentationClassOfTResource() throws IOException {
        Representation source = new GsonRepresentation<>(user);

        User u = gsonConverter.toObject(source, User.class, null);
        assertNotNull(u);
        assertEquals(user, u);

        Representation source1 = new GsonRepresentation<>(user);
        User u1 = gsonConverter.toObject(source1, User.class, null);
        assertNotNull(u1);
        assertEquals(user, u1);

        Representation source2 = new GsonRepresentation<User>(null);
        User u2 = gsonConverter.toObject(source2, User.class, null);
        assertNull(u2);
    }

    @Test
    final void testToRepresentationObjectVariantResource() {
        Variant v = new Variant(MediaType.APPLICATION_JSON);
        Representation rep = gsonConverter.toRepresentation(user, v, null);
        assertNotNull(rep);
        assertEquals(MediaType.APPLICATION_JSON, rep.getMediaType());

        Variant v1 = new Variant(MediaType.APPLICATION_XML);
        Representation rep1 = gsonConverter.toRepresentation(user, v1, null);
        assertNull(rep1);
    }

    @Test
    final void testToRepresentation_withGsonRepresentationSource_returnsSameInstance() {
        Representation gsonRep = new GsonRepresentation<>(user);
        Variant v = new Variant(MediaType.APPLICATION_JSON);

        Representation result = gsonConverter.toRepresentation(gsonRep, v, null);

        assertSame(gsonRep, result);
    }

    @Test
    final void testToRepresentation_withNullTargetMediaType_setsJsonMediaType() {
        Variant v = new Variant();
        assertNull(v.getMediaType());

        Representation result = gsonConverter.toRepresentation(user, v, null);

        assertEquals(MediaType.APPLICATION_JSON, v.getMediaType());
        assertNotNull(result);
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());
    }

    @Test
    final void testGetObjectClasses_withJsonVariant_returnsObjectAndGsonRepresentationClasses() {
        Variant jsonVariant = new Variant(MediaType.APPLICATION_JSON);

        List<Class<?>> classes = gsonConverter.getObjectClasses(jsonVariant);

        assertNotNull(classes);
        assertTrue(classes.contains(Object.class));
        assertTrue(classes.contains(GsonRepresentation.class));
    }

    @Test
    final void testGetObjectClasses_withIncompatibleVariant_returnsNull() {
        Variant xmlVariant = new Variant(MediaType.APPLICATION_XML);

        List<Class<?>> classes = gsonConverter.getObjectClasses(xmlVariant);

        assertNull(classes);
    }

    @Test
    final void testGetVariants_withNonNullSource_returnsJsonVariant() {
        List<VariantInfo> variants = gsonConverter.getVariants(Object.class);

        assertNotNull(variants);
        assertEquals(1, variants.size());
        assertTrue(variants.getFirst().isCompatible(new Variant(MediaType.APPLICATION_JSON)));
    }

    @Test
    final void testGetVariants_withNullSource_returnsEmptyList() {
        List<VariantInfo> variants = gsonConverter.getVariants(null);

        assertNotNull(variants);
        assertTrue(variants.isEmpty());
    }

    @Test
    final void testScoreObjectVariantResource_withNullTarget_returnsHalf() {
        float score = gsonConverter.score(user, null, null);

        assertEquals(0.5F, score);
    }

    @Test
    final void testScoreObjectVariantResource_withIncompatibleTarget_returnsHalf() {
        Variant v = new Variant(MediaType.APPLICATION_XML);

        float score = gsonConverter.score(user, v, null);

        assertEquals(0.5F, score);
    }

    @Test
    final void testScoreRepresentationClassOfTResource_withGsonRepresentationTarget_returnsOne() {
        Representation source = new StringRepresentation("plain text", MediaType.TEXT_PLAIN);

        float score = gsonConverter.score(source, GsonRepresentation.class, null);

        assertEquals(1.0F, score);
    }

    @Test
    final void testScoreRepresentationClassOfTResource_withNoMatch_returnsMinusOne() {
        Representation source = new StringRepresentation("plain text", MediaType.TEXT_PLAIN);

        float score = gsonConverter.score(source, User.class, null);

        assertEquals(-1.0F, score);
    }

    @Test
    final void testToObject_withPlainJsonRepresentation_createsGsonRepresentationInternally()
            throws IOException {
        final String userAsJsonString =
                "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}";
        Representation source =
                new StringRepresentation(userAsJsonString, MediaType.APPLICATION_JSON);

        User result = gsonConverter.toObject(source, User.class, null);

        assertNotNull(result);
        assertEquals("hello", result.loginId());
    }

    @Test
    final void testToObject_withTargetGsonRepresentationClass_returnsGsonRepresentation()
            throws IOException {
        final String userAsJsonString =
                "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}";
        Representation source =
                new StringRepresentation(userAsJsonString, MediaType.APPLICATION_JSON);

        Object result = gsonConverter.toObject(source, GsonRepresentation.class, null);

        assertInstanceOf(GsonRepresentation.class, result);
    }

    @Test
    final void testToObject_withIncompatibleRepresentation_returnsNull() throws IOException {
        Representation source = new StringRepresentation("plain text", MediaType.TEXT_PLAIN);

        User result = gsonConverter.toObject(source, User.class, null);

        assertNull(result);
    }

    @Test
    final void testUpdatePreferences_addsJsonPreference() {
        List<Preference<MediaType>> preferences = new ArrayList<>();

        gsonConverter.updatePreferences(preferences, User.class);

        assertFalse(preferences.isEmpty());
        assertEquals(MediaType.APPLICATION_JSON, preferences.getFirst().getMetadata());
    }

    @Test
    final void testSetObject_updatesWrappedObject() throws IOException {
        GsonRepresentation<Object> rep = new GsonRepresentation<>(user);
        Instant instant = Instant.parse("2026-05-07T10:00:00Z");
        User other =
                new User(
                        "other",
                        "pwd",
                        2,
                        false,
                        new Date(instant.toEpochMilli()),
                        new Date(instant.toEpochMilli()));

        rep.setObject(other);

        assertEquals(other, rep.getObject());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    final void testSetObjectClass_updatesObjectClass() {
        GsonRepresentation rep = new GsonRepresentation<>(user);

        rep.setObjectClass(String.class);

        assertEquals(String.class, rep.getObjectClass());
    }

    @Test
    final void testSetBuilder_updatesBuilder() {
        GsonRepresentation<User> rep = new GsonRepresentation<>(user);
        GsonBuilder customBuilder = new GsonBuilder();
        customBuilder.setDateFormat(DateFormat.SHORT, DateFormat.SHORT);

        rep.setBuilder(customBuilder);

        assertSame(customBuilder, rep.getBuilder());
    }

    @Test
    final void testWrite_withSourceRepresentation_delegatesToIt() throws IOException {
        final String userAsJsonString =
                "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}";
        Representation source =
                new StringRepresentation(userAsJsonString, MediaType.APPLICATION_JSON);
        GsonRepresentation<User> gsonRep = new GsonRepresentation<>(source, User.class);

        assertEquals(userAsJsonString, gsonRep.getText());
    }
}
