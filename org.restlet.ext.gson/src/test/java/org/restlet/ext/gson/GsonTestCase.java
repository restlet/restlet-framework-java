/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.gson;


import com.google.gson.annotations.Since;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the Gson extension.
 *
 * @author Neal Mi
 */
public class GsonTestCase {

    private static class User {
        private final boolean active;

        private final Date createAt;

        @Since(2.0)
        private Date lastLogin;

        private final String loginId;

        private final String password;

        private final int rate;

        public User(String loginId, String password, int rate, boolean active,
                Date createAt, Date lastLogin) {
            super();
            this.loginId = loginId;
            this.password = password;
            this.rate = rate;
            this.active = active;
            this.createAt = createAt;
            this.lastLogin = lastLogin;
        }

        public Date getCreateAt() {
            return createAt;
        }

        public Date getLastLogin() {
            return lastLogin;
        }

        public String getLoginId() {
            return loginId;
        }

        public String getPassword() {
            return password;
        }

        public int getRate() {
            return rate;
        }

        public boolean isActive() {
            return active;
        }
    }

    private GsonConverter gsonConverter;

    private User user;

    @BeforeEach
    public void setUpEach() throws Exception {
        user = new User("hello", "secret", 1, true, new Date(), new Date());
        gsonConverter = new GsonConverter();
    }

    @Test
    public final void testCreateMediaTypeT() {
        Representation rep = new GsonRepresentation<>(user);

        assertNotNull(rep);
        assertEquals(rep.getMediaType(), MediaType.APPLICATION_JSON);
    }

    @Test
    public final void testCreateRepresentationClassOfT() {
        Representation rep = new GsonRepresentation<>(user);
        Representation rep1 = new GsonRepresentation<>(rep, User.class);

        assertEquals(rep.getMediaType(), rep1.getMediaType());
        assertEquals(rep.getClass(), rep1.getClass());
    }

    @Test
    public final void testGsonRepresentationRead() throws IOException {
        final String userAsJsonString = "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}";
        Representation source = new StringRepresentation(userAsJsonString, MediaType.APPLICATION_JSON);

        GsonRepresentation<User> gsonRep = new GsonRepresentation<>(source, User.class);
        User user = gsonRep.getObject();

        assertNotNull(user);
        assertEquals("hello", user.getLoginId());
        assertEquals("secret", user.getPassword());
        assertEquals(1, user.getRate());
        assertTrue(user.isActive());
        DateTime time = new DateTime("2012-05-20T15:41:01.489+08:00");
        assertEquals(time.getMillis(), user.getCreateAt().getTime());
        assertEquals(time.getMillis(), user.getLastLogin().getTime());

        GsonRepresentation<User> gsonRep1 = new GsonRepresentation<>(source, User.class);
        gsonRep1.getBuilder().setVersion(1.0);

        User u1 = gsonRep1.getObject();
        assertNull(u1.getLastLogin());
        assertEquals("hello", u1.getLoginId());
        assertEquals("secret", u1.getPassword());
        assertEquals(1, u1.getRate());
        assertTrue(u1.isActive());
        assertEquals(time.getMillis(), u1.getCreateAt().getTime());
    }

    @Test
    public final void testGsonRepresentationWrite() throws IOException {
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
    public final void testScoreObjectVariantResource() {
        Variant v = new Variant(MediaType.APPLICATION_JSON);
        Representation source = new GsonRepresentation<>(user);

        float score = gsonConverter.score(user, v, null);
        assertEquals(0.8F, score);

        float score1 = gsonConverter.score(source, v, null);
        assertEquals(1.0F , score1);
    }

    @Test
    public final void testScoreRepresentationClassOfTResource() {
        Representation source = new GsonRepresentation<User>(user);

        float score = gsonConverter.score(source, User.class, null);
        assertEquals(1.0F, score);

        Representation source1 = new EmptyRepresentation();

        float score1 = gsonConverter.score(source1, User.class, null);
        assertEquals(0.8F, score1);
    }

    @Test()
    public final void testToObjectRepresentationClassOfTResource()
            throws IOException {
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
    public final void testToRepresentationObjectVariantResource()
            throws IOException {
        Variant v = new Variant(MediaType.APPLICATION_JSON);
        Representation rep = gsonConverter.toRepresentation(user, v, null);
        assertNotNull(rep);
        assertEquals(rep.getMediaType(), MediaType.APPLICATION_JSON);

        Variant v1 = new Variant(MediaType.APPLICATION_XML);
        Representation rep1 = gsonConverter.toRepresentation(user, v1, null);
        assertNull(rep1);
    }

}
