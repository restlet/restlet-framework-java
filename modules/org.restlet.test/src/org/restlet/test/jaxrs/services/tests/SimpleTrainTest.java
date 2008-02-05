/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.SimpleTrain;

public class SimpleTrainTest extends JaxRsTestCase {

    private static final boolean ONLY_M2 = false;

    private static final boolean ONLY_TEXT_ALL = false;

    private static final Preference<MediaType> PREF_TEXTPLAIN_QUAL05 = new Preference<MediaType>(
            MediaType.TEXT_PLAIN, 0.5f);

    @Override
    protected Class<?> getRootResourceClass() {
        return SimpleTrain.class;
    }

    public void testGetHtmlText() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL)
            return;
        Response response = get(MediaType.TEXT_HTML);
        assertTrue(response.getStatus().isSuccess());
        Representation entity = response.getEntity();
        assertEquals(SimpleTrain.RERP_HTML_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_HTML, entity.getMediaType());
    }

    public void testGetPlainText() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL)
            return;
        Response response = get(MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(SimpleTrain.RERP_PLAIN_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetTextAll() throws Exception {
        if (ONLY_M2)
            return;
        Response response;
        // TODO This request fails because methods for text/plain and for
        // text/html are available, but it is not specified, which to use.
        // response = JaxRsTestCase.accessServer(SimpleTrain.class,
        // Method.GET, MediaType.TEXT_ALL);
        // assertEquals(Status.SUCCESS_OK, response.getStatus());

        response = get(MediaType.TEXT_PLAIN);
        assertTrue(response.getStatus().isSuccess());
        Representation representation = response.getEntity();
        assertEquals(SimpleTrain.RERP_PLAIN_TEXT, representation.getText());
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
    }

    public void testGetTextMultiple1() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL)
            return;
        Response response = JaxRsTestCase.accessServer(Method.GET,
                SimpleTrain.class, Util.createList(new Object[] {
                        PREF_TEXTPLAIN_QUAL05, MediaType.TEXT_CALENDAR }));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation representation = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_PLAIN, representation
                .getMediaType());
        assertEquals(SimpleTrain.RERP_PLAIN_TEXT, representation.getText());
    }

    public void testGetTextMultiple2() throws Exception {
        if (ONLY_TEXT_ALL)
            return;
        Response response = JaxRsTestCase.accessServer(Method.GET,
                SimpleTrain.class, Util.createList(new Object[] {
                        PREF_TEXTPLAIN_QUAL05, MediaType.TEXT_HTML }));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation representation = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_HTML, representation.getMediaType());
        assertEquals(SimpleTrain.RERP_HTML_TEXT, representation.getText());
    }

    public void testGetWithSlashInUriParam() throws IOException {
        Response response = get("decode/abc/def");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc/def", response.getEntity().getText());
    }

    public void testHead() throws Exception {
        if (ONLY_M2 || ONLY_TEXT_ALL)
            return;
        Response responseHead = JaxRsTestCase.accessServer(Method.HEAD,
                SimpleTrain.class, Util.createList(new Object[] {
                        PREF_TEXTPLAIN_QUAL05, MediaType.TEXT_HTML }));
        Response responseGett = JaxRsTestCase.accessServer(Method.GET,
                SimpleTrain.class, Util.createList(new Object[] {
                        PREF_TEXTPLAIN_QUAL05, MediaType.TEXT_HTML }));
        assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
        assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
        Representation representationHead = responseHead.getEntity();
        Representation representationGett = responseGett.getEntity();
        assertEqualMediaType(MediaType.TEXT_HTML, representationGett
                .getMediaType());
        assertEqualMediaType(MediaType.TEXT_HTML, representationHead
                .getMediaType());
        assertEquals(SimpleTrain.RERP_HTML_TEXT, representationGett.getText());
        assertEquals(0, representationHead.getText().length());
    }

    public void testOptions() throws Exception {
        Response response = accessServer(Method.OPTIONS, SimpleTrain.class);
        assertAllowedMethod(response, Method.GET);
    }

    public void testTemplParamsDecoded() throws Exception {
        String deEncoded = "decode";
        Response response = get(deEncoded + "/66");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation r = response.getEntity();
        assertEquals("66", r.getText());

        response = get(deEncoded + "/a+bc");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        r = response.getEntity();
        assertEquals("a bc", r.getText());

        // TODO wait for issue 435
        // response = accessServer(Method.GET, SimpleTrain.class,
        // deEncoded + "/a%20bc");
        // r = response.getEntity();
        // System.out.println(r.getText());
        // assertEquals(Status.SUCCESS_OK, response.getStatus());
        // assertEquals("a bc", r.getText());
    }

    public void testTemplParamsEncoded() throws Exception {
        String deEncoded = "encode";
        Response response = get(deEncoded + "/66");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation r = response.getEntity();
        assertEquals("66", r.getText());

        response = get(deEncoded + "/a+bc");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        r = response.getEntity();
        assertEquals("a+bc", r.getText());

        // TODO wait for issue 435
        // response = get(deEncoded + "/a%20bc");
        // r = response.getEntity();
        // System.out.println(r.getText());
        // assertEquals(Status.SUCCESS_OK, response.getStatus());
        // assertEquals("a%20bc", r.getText());
    }

    public void testUseAllTests() {
        assertFalse("You should use all tests", ONLY_M2);
        assertFalse("You should use all tests", ONLY_TEXT_ALL);
    }
}