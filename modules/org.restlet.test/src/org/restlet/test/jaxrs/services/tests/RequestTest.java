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
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(modifiedSince);
        conditions.setMatch(TestUtils.createList(entityTag));
        return conditions;
    }

    public static void main(String[] args) throws Exception {
        runServerUntilKeyPressed(new RequestTest());
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
        Conditions cond = createConditions(BEFORE, getDatastoreETag());
        Response response = get("date", cond);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    public void testDateAndEntityTag1Put() throws Exception {
        Conditions cond = createConditions(BEFORE, getDatastoreETag());
        Response response = put("date", cond);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    public void testDateAndEntityTag2Get() throws Exception {
        Conditions conditions = createConditions(AFTER, getDatastoreETag());
        Response response = get("date", conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
    }

    public void testDateAndEntityTag2Put() throws Exception {
        Conditions conditions = createConditions(AFTER, getDatastoreETag());
        Response response = put("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"was not modified\"", response
                .getEntity().getText().contains(
                        "The entity was not modified since"));
    }

    public void testDateAndEntityTag3Get() throws Exception {
        Conditions conditions = createConditions(BEFORE, new Tag("shkhsdk"));
        Response response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        String entityText = response.getEntity().getText();
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));
    }

    public void testDateAndEntityTag3Put() throws Exception {
        Conditions conditions = createConditions(BEFORE, new Tag("shkhsdk"));
        Response response = put("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        String entityText = response.getEntity().getText();
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));
    }

    public void testDateAndEntityTag4Get() throws Exception {
        Conditions conditions = createConditions(AFTER, new Tag("shkhsdk"));
        Response response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        String entityText = response.getEntity().getText();
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));
    }

    public void testDateAndEntityTag4Put() throws Exception {
        Conditions conditions = createConditions(AFTER, new Tag("shkhsdk"));
        Response response = put("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        String entityText = response.getEntity().getText();
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));
    }

    @SuppressWarnings("deprecation")
    public void testGetDateNotModified() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        Response response = get("date", conditions);
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
        assertTrue("Entity must contain \"does not match Entity Tag\"",
                response.getEntity().getText().contains(
                        "The entity does not match Entity Tag"));
    }

    public void testGetEntityTagNoneMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(TestUtils.createList(getDatastoreETag()));
        Response response = get("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"matches Entity Tag\"", response
                .getEntity().getText()
                .contains("The entity matches Entity Tag"));

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

        // LATER test, what happens, because of Range-Header
        // see RFC2616, top of page 131
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
        assertTrue("Entity must contain \"was modified\"", response.getEntity()
                .getText().contains("The entity was modified since"));

        // LATER testen, was bei ungultigem Datum passiert:
        // If-Unmodified-Since-Header ignorieren.
    }

    public void testOptions() {
        Response response = options();
        Set<Method> allowedMethods = response.getAllowedMethods();
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
        Response response = put("date", conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = put("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"was not modified\"", response
                .getEntity().getText().contains(
                        "The entity was not modified since"));
    }

    public void testPutUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = put("date", conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = put("date", conditions);
        assertEquals(PREC_FAILED, response.getStatus());
        String respEntity = response.getEntity().getText();
        assertTrue("Entity must contain \"was not modified\"", respEntity
                .contains("The entity was modified since"));
    }

    public void testSelectVariant() {
        ClientInfo clientInfo = new ClientInfo();
        List<Preference<Language>> accLangs = clientInfo.getAcceptedLanguages();
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