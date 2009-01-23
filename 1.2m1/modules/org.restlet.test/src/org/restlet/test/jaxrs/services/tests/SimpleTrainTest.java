/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * one of the first test case classes.
 * 
 * @author Stephan Koops
 * @see SimpleTrain
 */
public class SimpleTrainTest extends JaxRsTestCase {

    private static final boolean ONLY_M2 = false;

    private static final boolean ONLY_TEXT_ALL = false;

    private static final Preference<MediaType> PREF_TEXTPLAIN_QUAL05 = new Preference<MediaType>(
            MediaType.TEXT_PLAIN, 0.5f);

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(SimpleTrain.class);
            }
        };
    }

    public void testGetHtmlText() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL) {
            return;
        }
        final Response response = get(MediaType.TEXT_HTML);
        sysOutEntityIfError(response);
        assertTrue(response.getStatus().isSuccess());
        final Representation entity = response.getEntity();
        assertEquals(SimpleTrain.RERP_HTML_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_HTML, entity.getMediaType());
    }

    public void testGetPlainText() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL) {
            return;
        }
        final Response response = get(MediaType.TEXT_PLAIN);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEquals(SimpleTrain.RERP_PLAIN_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetTextAll() throws Exception {
        if (ONLY_M2) {
            return;
        }
        Response response = get(MediaType.TEXT_ALL);
        sysOutEntityIfError(response);
        Representation representation = response.getEntity();
        final MediaType mediaType = representation.getMediaType();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(mediaType.equals(MediaType.TEXT_PLAIN, true)
                || mediaType.equals(MediaType.TEXT_HTML, true));

        response = get(MediaType.TEXT_PLAIN);
        sysOutEntityIfError(response);
        assertTrue(response.getStatus().isSuccess());
        representation = response.getEntity();
        assertEquals(SimpleTrain.RERP_PLAIN_TEXT, representation.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, representation);
    }

    public void testGetTextMultiple1() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL) {
            return;
        }
        final Response response = accessServer(Method.GET, SimpleTrain.class,
                TestUtils.createList(new Object[] { PREF_TEXTPLAIN_QUAL05,
                        MediaType.TEXT_CALENDAR }));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals(SimpleTrain.RERP_PLAIN_TEXT, entity.getText());
    }

    public void testGetTextMultiple2() throws Exception {
        if (ONLY_TEXT_ALL) {
            return;
        }
        final Response response = accessServer(Method.GET, SimpleTrain.class,
                TestUtils.createList(new Object[] { PREF_TEXTPLAIN_QUAL05,
                        MediaType.TEXT_HTML }));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation representation = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_HTML, representation.getMediaType());
        assertEquals(SimpleTrain.RERP_HTML_TEXT, representation.getText());
    }

    public void testHead() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL) {
            return;
        }
        final Response responseHead = accessServer(Method.HEAD,
                SimpleTrain.class, TestUtils.createList(new Object[] {
                        PREF_TEXTPLAIN_QUAL05, MediaType.TEXT_HTML }));
        final Response responseGett = accessServer(Method.GET,
                SimpleTrain.class, TestUtils.createList(new Object[] {
                        PREF_TEXTPLAIN_QUAL05, MediaType.TEXT_HTML }));
        assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
        assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
        final Representation entityHead = responseHead.getEntity();
        final Representation entityGett = responseGett.getEntity();
        assertNotNull(entityHead);
        assertNotNull(entityGett);
        assertEqualMediaType(MediaType.TEXT_HTML, entityGett.getMediaType());
        assertEquals(SimpleTrain.RERP_HTML_TEXT, entityGett.getText());
    }

    public void testOptions() throws Exception {
        final Response response = options();
        sysOutEntityIfError(response);
        assertAllowedMethod(response, Method.GET);
    }

    public void testTemplParamsDecoded() throws Exception {
        final String deEncoded = "decode";
        Response response = get(deEncoded + "/66");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("66", response.getEntity().getText());

        response = get(deEncoded + "/a+bc");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a bc", response.getEntity().getText());

        response = get(deEncoded + "/a%20bc");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a bc", response.getEntity().getText());
    }

    public void testTemplParamsEncoded() throws Exception {
        final String deEncoded = "encode";
        Response response = get(deEncoded + "/66");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("66", response.getEntity().getText());

        response = get(deEncoded + "/a+bc");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a+bc", response.getEntity().getText());

        response = get(deEncoded + "/a%20bc");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a%20bc", response.getEntity().getText());
    }

    public void testUseAllTests() {
        assertFalse("You should use all tests", ONLY_M2);
        assertFalse("You should use all tests", ONLY_TEXT_ALL);
    }

    protected Class<?> getRootResourceClass() {
        throw new UnsupportedOperationException(
                "You must implement the methods getRootResourceClass() or getAppConfig(). If you only implemented getAppConfig(), you can't use this method");
    }
}