/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.ext.gson.GsonConverter;
import org.restlet.ext.gson.GsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ReaderRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.google.gson.annotations.Since;

/**
 * Unit test for the Gson extension.
 * 
 * @author Neal Mi
 */
public class GsonTestCase {

    private class User {
        private boolean active;

        private Date createAt;

        @Since(2.0)
        private Date lastLogin;

        private String loginId;

        private String password;

        private int rate;

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

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    private GsonConverter c;

    private User user;

    @Before
    public void setUp() throws Exception {
        user = new User("hello", "secret", 1, true, new Date(), new Date());
        c = new GsonConverter();
    }

    @Test
    public final void testCreateMediaTypeT() {
        Representation rep = new GsonRepresentation<User>(user);

        assertNotNull(rep);
        assertEquals(rep.getMediaType(), MediaType.APPLICATION_JSON);
    }

    @Test
    public final void testCreateRepresentationClassOfT() {
        Representation rep = new GsonRepresentation<User>(user);
        Representation rep1 = new GsonRepresentation<User>(rep, User.class);

        assertEquals(rep1.getMediaType(), rep1.getMediaType());
        assertEquals(rep1.getClass(), rep1.getClass());
    }

    @Test
    public final void testGsonRepresentationRead() throws IOException {
        Reader reader = new StringReader(
                "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}");
        Representation source = new ReaderRepresentation(reader);
        source.setMediaType(MediaType.APPLICATION_JSON);

        GsonRepresentation<User> gsonRep = new GsonRepresentation<User>(source,
                User.class);
        User u = gsonRep.getObject();

        assertNotNull(u);
        assertEquals("hello", u.getLoginId());
        assertEquals("secret", u.getPassword());
        assertEquals(1, u.getRate());
        assertTrue(u.isActive());
        DateTime time = new DateTime("2012-05-20T15:41:01.489+08:00");
        assertEquals(time.getMillis(), u.getCreateAt().getTime());

        Reader reader1 = new StringReader(
                "{\"loginId\":\"hello\",\"password\":\"secret\",\"rate\":1,\"active\":true,\"createAt\":\"2012-05-20T15:41:01.489+08:00\",\"lastLogin\":\"2012-05-20T15:41:01.489+08:00\"}");
        Representation source1 = new ReaderRepresentation(reader1);

        GsonRepresentation<User> gsonRep1 = new GsonRepresentation<GsonTestCase.User>(
                source1, User.class);

        gsonRep1.getBuilder().setVersion(1.0);
        User u1 = gsonRep1.getObject();
        assertNull(u1.getLastLogin());
    }

    @Test
    public final void testGsonRepresentationWrite() throws IOException {
        GsonRepresentation<User> source = new GsonRepresentation<User>(user);

        assertEquals(User.class, source.getObjectClass());
        assertEquals(user, source.getObject());
        source.write(System.out);
        System.out.println();

        source.getBuilder().setVersion(1.0);
        source.write(System.out);
        System.out.println();

        String text = "What's going on?";
        GsonRepresentation<String> source1 = new GsonRepresentation<String>(
                text);

        assertEquals(String.class, source1.getObjectClass());
        assertEquals(text, source1.getObject());
        source1.write(System.out);
        System.out.println();

    }

    @Test
    public final void testScoreObjectVariantResource() {
        Variant v = new Variant(MediaType.APPLICATION_JSON);
        Representation source = new GsonRepresentation<User>(user);

        float score = c.score(user, v, null);
        assertTrue(score == 0.8F);

        float score1 = c.score(source, v, null);
        assertTrue(score1 == 1.0F);
    }

    @Test
    public final void testScoreRepresentationClassOfTResource() {
        Representation source = new GsonRepresentation<User>(user);

        float score = c.score(source, User.class, null);
        assertTrue(score == 1.0F);

        Representation source1 = new EmptyRepresentation();

        float score1 = c.score(source1, User.class, null);
        assertTrue(score1 == 0.8F);
    }

    @Test()
    public final void testToObjectRepresentationClassOfTResource()
            throws IOException {
        Representation source = new GsonRepresentation<User>(user);

        User u = c.toObject(source, User.class, null);
        assertNotNull(u);
        assertEquals(user, u);

        Representation source1 = new GsonRepresentation<User>(user);
        User u1 = c.toObject(source1, User.class, null);
        assertNotNull(u1);
        assertEquals(user, u1);

        Representation source2 = new GsonRepresentation<User>(null);
        User u2 = c.toObject(source2, User.class, null);
        assertNull(u2);
    }

    @Test
    public final void testToRepresentationObjectVariantResource()
            throws IOException {
        Variant v = new Variant(MediaType.APPLICATION_JSON);
        Representation rep = c.toRepresentation(user, v, null);
        assertNotNull(rep);
        assertEquals(rep.getMediaType(), MediaType.APPLICATION_JSON);

        Variant v1 = new Variant(MediaType.APPLICATION_XML);
        Representation rep1 = c.toRepresentation(user, v1, null);
        assertNull(rep1);
    }

}
