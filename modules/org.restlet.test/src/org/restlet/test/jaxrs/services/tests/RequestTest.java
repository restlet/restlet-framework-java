/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Request;

import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Dimension;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.test.jaxrs.services.resources.RequestService;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 * @see RequestService
 * @see Request
 * @see CallContext
 */
public class RequestTest extends JaxRsTestCase {
    /**
     * After than 2008-01-08, 12h
     * 
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     */
    @SuppressWarnings("deprecation")
    public static final Date AFTER = new Date(2008 - 1900, 0, 9); // 2008-01-09

    /**
     * Before 2008-01-08, 12h
     * 
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     */
    @SuppressWarnings("deprecation")
    public static final Date BEFORE = new Date(2007 - 1900, 11, 31); // 2007-12-31

    private static final Status PREC_FAILED = Status.CLIENT_ERROR_PRECONDITION_FAILED;

    /**
     * @param modifiedSince
     * @param entityTag
     * @return
     */
    private static Conditions createConditions(Date modifiedSince, Tag entityTag) {
        final Conditions conditions = new Conditions();
        conditions.setModifiedSince(modifiedSince);
        conditions.setMatch(TestUtils.createList(entityTag));
        return conditions;
    }

    public static void main(String[] args) throws Exception {
        new RequestTest().runServerUntilKeyPressed();
    }

    /**
     * @return
     */
    @SuppressWarnings("all")
    private Tag getDatastoreETag() {
        return org.restlet.ext.jaxrs.internal.util.Converter
                .toRestletTag(RequestService.getEntityTagFromDatastore());
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return RequestService.class;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testDateAndEntityTag1Get() throws Exception {
        final Conditions cond = createConditions(BEFORE, getDatastoreETag());
        final Response response = get("date", cond);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /**
     * @see RequestService#put(Request)
     */
    public void testDateAndEntityTag1Put() throws Exception {
        final Conditions cond = createConditions(BEFORE, getDatastoreETag());
        final Response response = put("date", null, cond);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    public void testDateAndEntityTag2Get() throws Exception {
        final Conditions conditions = createConditions(AFTER,
                getDatastoreETag());
        final Response response = get("date", conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
    }

    public void testDateAndEntityTag2Put() throws Exception {
        final Conditions conditions = createConditions(AFTER,
                getDatastoreETag());
        final Response response = put("date", null, conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testDateAndEntityTag3Get() throws Exception {
        final Conditions conditions = createConditions(BEFORE, new Tag(
                "shkhsdk"));
        final Response response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testDateAndEntityTag3Put() throws Exception {
        final Conditions conditions = createConditions(BEFORE, new Tag(
                "shkhsdk"));
        final Response response = put("date", null, conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testDateAndEntityTag4Get() throws Exception {
        final Conditions conditions = createConditions(AFTER,
                new Tag("shkhsdk"));
        final Response response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testDateAndEntityTag4Put() throws Exception {
        final Conditions conditions = createConditions(AFTER,
                new Tag("shkhsdk"));
        final Response response = put("date", null, conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testGetDateNotModified() throws Exception {
        final Conditions conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        final Response response = get("date", conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertFalse(response.isEntityAvailable());
        // from RFC 2616, Section 10.3.5
        // The 304 response MUST include the following header fields:
        // - ETag and/or Content-Location, if the header would have been sent
        // in a 200 response to the same request
        // - Expires, Cache-Control, and/or Vary, if the field-value might
        // differ from that sent in any previous response for the same
        // variant
    }

    public void testGetEntityTagMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setMatch(TestUtils.createList(getDatastoreETag()));
        Response response = get("date", conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getDatastoreETag(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setMatch(TestUtils.createList(new Tag("affer")));
        response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testGetEntityTagNoneMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(TestUtils.createList(getDatastoreETag()));
        Response response = get("date", conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());

        conditions = new Conditions();
        conditions.setNoneMatch(TestUtils.createList(new Tag("affer")));
        response = get("date", conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /**
     * @see RequestService#getLastModificationDateFromDatastore()
     * @throws Exception
     */
    public void testGetModifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        Response response = get("date", conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getDatastoreETag(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = get("date", conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getDatastoreETag(), response.getEntity().getTag());
        assertEquals(0, response.getEntity().getSize());
    }

    public void testGetUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = get("date", conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getDatastoreETag(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());

        // NICE testen, was bei ungultigem Datum passiert:
        // If-Unmodified-Since-Header ignorieren.
    }

    public void testOptions() {
        final Response response = options();
        final Set<Method> allowedMethods = response.getAllowedMethods();
        assertEquals(3, allowedMethods.size());
        assertTrue("allowedOptions must contain ABC", allowedMethods
                .contains(Method.valueOf("ABC")));
        assertTrue("allowedOptions must contain DEF", allowedMethods
                .contains(Method.valueOf("DEF")));
        assertTrue("allowedOptions must contain GHI", allowedMethods
                .contains(Method.valueOf("GHI")));
        assertEquals(3, allowedMethods.size());
    }

    /**
     * @see RequestService#getLastModificationDateFromDatastore()
     * @throws Exception
     */
    public void testPutModifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        Response response = put("date", null, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = put("date", null, conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testPutUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = put("date", null, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = put("date", null, conditions);
        assertEquals(PREC_FAILED, response.getStatus());
    }

    public void testSelectVariant() {
        final ClientInfo clientInfo = new ClientInfo();
        final List<Preference<Language>> accLangs = clientInfo
                .getAcceptedLanguages();
        accLangs.add(new Preference<Language>(Language.SPANISH, 1f));
        accLangs.add(new Preference<Language>(new Language("de"), 0.8f));
        clientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_HTML, 0.5f));

        Response response = get("selectVariants", clientInfo);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response.getEntity()
                .getMediaType());
        assertEquals(new Language("de"), TestUtils.getOnlyElement(response
                .getEntity().getLanguages()));
        assertTrue("dimensions must contain " + Dimension.MEDIA_TYPE, response
                .getDimensions().contains(Dimension.MEDIA_TYPE));
        assertTrue("dimensions must contain " + Dimension.LANGUAGE, response
                .getDimensions().contains(Dimension.LANGUAGE));

        clientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_PLAIN, 1f));
        response = get("selectVariants", clientInfo);
        assertEqualMediaType(MediaType.TEXT_PLAIN, response.getEntity()
                .getMediaType());
        assertEquals(new Language("de"), TestUtils.getOnlyElement(response
                .getEntity().getLanguages()));

        accLangs.add(new Preference<Language>(Language.ENGLISH, 0.9f));
        response = get("selectVariants", clientInfo);
        assertEqualMediaType(MediaType.TEXT_PLAIN, response.getEntity()
                .getMediaType());
        assertEquals(Language.ENGLISH, TestUtils.getOnlyElement(response
                .getEntity().getLanguages()));
    }
}