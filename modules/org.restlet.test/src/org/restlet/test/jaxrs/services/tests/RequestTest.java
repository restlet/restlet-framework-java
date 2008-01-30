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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import org.restlet.ext.jaxrs.util.Converter;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.test.jaxrs.services.RequestService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
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

    @Override
    @SuppressWarnings("unchecked")
    protected Collection createRootResourceColl() {
        return Collections.singleton(RequestService.class);
    }

    /**
     * @return
     */
    private Tag getEntityTagFromDatastore() {
        return Converter
                .toRestletTag(RequestService.getEntityTagFromDatastore());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetDateAndEntityTag() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        conditions.setMatch(Util.createList(getEntityTagFromDatastore()));
        Response response = accessServer(RequestService.class, "date",
                Method.GET, conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        response = accessServer(RequestService.class, "date", Method.PUT,
                conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        conditions.setMatch(Util.createList(getEntityTagFromDatastore()));
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());

        response = accessServer(RequestService.class, "date", Method.PUT,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"was not modified\"", response
                .getEntity().getText().contains(
                        "The entity was not modified since"));

        conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        conditions.setMatch(Util.createList(new Tag("shkhsdk")));
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        String entityText = response.getEntity().getText();
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));

        response = accessServer(RequestService.class, "date", Method.PUT,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        conditions.setMatch(Util.createList(new Tag("shkhsdk")));
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        entityText = response.getEntity().getText();
        assertTrue(
                "Entity must contain \"was not modified\" or \"does not match Entity Tag\", but is \""
                        + entityText + "\"",
                entityText.contains("The entity was not modified since")
                        || entityText
                                .contains("The entity does not match Entity Tag"));

        response = accessServer(RequestService.class, "date", Method.PUT,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
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
        Response response = accessServer(RequestService.class, "date",
                Method.GET, conditions, null);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertEquals(0, response.getEntity().getText().length());
        // from RFC 2616, Section 10.3.5
        // The 304 response MUST include the following header fields:
        // - ETag and/or Content-Location, if the header would have been sent
        // in a 200 response to the same request
        // - Expires, Cache-Control, and/or Vary, if the field-value might
        // differ from that sent in any previous response for the same
        // variant
        // gefragt: JSR311 Wie das vorige einhalten?
    }

    public void testGetEntityTagMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setMatch(Util.createList(getEntityTagFromDatastore()));
        Response response = accessServer(RequestService.class, "date",
                Method.GET, conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setMatch(Util.createList(new Tag("affer")));
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"does not match Entity Tag\"",
                response.getEntity().getText().contains(
                        "The entity does not match Entity Tag"));
    }

    public void testGetEntityTagNoneMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(Util.createList(getEntityTagFromDatastore()));
        Response response = accessServer(RequestService.class, "date",
                Method.GET, conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"matches Entity Tag\"", response
                .getEntity().getText()
                .contains("The entity matches Entity Tag"));

        conditions = new Conditions();
        conditions.setNoneMatch(Util.createList(new Tag("affer")));
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /**
     * @see RequestService#getLastModificationDateFromDatastore()
     * @throws Exception
     */
    public void testGetModifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        Response response = accessServer(RequestService.class, "date",
                Method.GET, conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertEquals(0, response.getEntity().getSize());

        // LATER test, what happens, because of Range-Header
        // see RFC2616, top of page 131
    }

    public void testGetUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = accessServer(RequestService.class, "date",
                Method.GET, conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(RequestService.getLastModificationDateFromDatastore(),
                response.getEntity().getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = accessServer(RequestService.class, "date", Method.GET,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"was modified\"", response.getEntity()
                .getText().contains("The entity was modified since"));

        // LATER testen, was bei ungültigem Datum passiert:
        // If-Unmodified-Since-Header ignorieren.
    }

    public void testOptions() {
        Response response = accessServer(RequestService.class, Method.OPTIONS);
        Set<Method> allowedMethods = response.getAllowedMethods();
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
        Response response = accessServer(RequestService.class, "date",
                Method.PUT, conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = accessServer(RequestService.class, "date", Method.PUT,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"was not modified\"", response
                .getEntity().getText().contains(
                        "The entity was not modified since"));
    }

    public void testPutUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = accessServer(RequestService.class, "date",
                Method.PUT, conditions, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = accessServer(RequestService.class, "date", Method.PUT,
                conditions, null);
        assertEquals(PREC_FAILED, response.getStatus());
        assertTrue("Entity must contain \"was not modified\"", response
                .getEntity().getText()
                .contains("The entity was modified since"));

        // LATER testen, was bei ungültigem Datum passiert:
        // If-Unmodified-Since-Header ignorieren.
    }
    
    public void testSelectVariant()
    {
        ClientInfo clientInfo = new ClientInfo();
        List<Preference<Language>> accLangs = clientInfo.getAcceptedLanguages();
        accLangs.add(new Preference<Language>(Language.SPANISH, 1f));
        accLangs.add(new Preference<Language>(new Language("de"), 0.8f));
        clientInfo.getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.5f));
        
        Response response = accessServer(RequestService.class, "selectVariants", Method.GET, null, clientInfo);
        assertEqualMediaType(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals(new Language("de"), Util.getOnlyElement(response.getEntity().getLanguages()));
        assertTrue("dimensions must contain "+Dimension.MEDIA_TYPE, response.getDimensions().contains(Dimension.MEDIA_TYPE));
        assertTrue("dimensions must contain "+Dimension.LANGUAGE, response.getDimensions().contains(Dimension.LANGUAGE));

        clientInfo.getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.TEXT_PLAIN, 1f));
        response = accessServer(RequestService.class, "selectVariants", Method.GET, null, clientInfo);
        assertEqualMediaType(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals(new Language("de"), Util.getOnlyElement(response.getEntity().getLanguages()));

        accLangs.add(new Preference<Language>(Language.ENGLISH, 0.9f));
        response = accessServer(RequestService.class, "selectVariants", Method.GET, null, clientInfo);
        assertEqualMediaType(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals(Language.ENGLISH, Util.getOnlyElement(response.getEntity().getLanguages()));
    }
}