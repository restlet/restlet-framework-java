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
import java.util.Set;

import org.restlet.Client;
import org.restlet.data.Conditions;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.test.jaxrs.services.EvaluatePreconditionService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 */
public class EvaluatePreconditionTest extends JaxRsTestCase {
    @Override
    @SuppressWarnings("unchecked")
    protected Collection createRootResourceColl() {
        return Collections.singleton(EvaluatePreconditionService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @SuppressWarnings("deprecation")
    public static final Date OLDER = new Date(2007-1900, 11, 31); // 2007-12-31
    
    @SuppressWarnings("deprecation")
    public static final Date NEWER = new Date(2008-1900, 0, 9); // 2008-01-09
    
    /**
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     *      The Date of the last modification of this resource is 2008-01-01,
     *      12h
     * @throws Exception
     */
    public void testEvalPrecDateOk() throws Exception {
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, OLDER, null);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(200, response.getStatus().getCode());
        assertEquals(EvaluatePreconditionService.getLastModificationDateFromDatastore(), response.getEntity().getModificationDate());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);
    }

    @SuppressWarnings("deprecation")
    public void testEvalPrecDateNotModified() throws Exception {
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, NEWER, null);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertEquals(304, response.getStatus().getCode());
        assertEquals(0, response.getEntity().getText().length());
        // from RFC 2616, Section 10.3.5
        // The 304 response MUST include the following header fields:
        // - ETag and/or Content-Location, if the header would have been sent
        //   in a 200 response to the same request
        // - Expires, Cache-Control, and/or Vary, if the field-value might
        //   differ from that sent in any previous response for the same
        //   variant
        // TODO JSR311: Wie das vorige einhalten?

        // wenn GET, dann 304, bei anderen Methoden andere Ergebnisse
        // (Precondition failed)
        // 304:
        // * muss das Datum der letzten Änderung enthalten
    }

    public void testEvalPrecEntityTag() throws Exception {
    }

    public void testEvalPrecDateAndEntityTag() throws Exception {
    }

    public void testOptions()
    {
        Response response = accessServer(EvaluatePreconditionService.class,
                Method.OPTIONS);
        Set<Method> allowedMethods = response.getAllowedMethods();
        assertTrue("allowedOptions must contain ABC", allowedMethods.contains(Method.valueOf("ABC")));
        assertTrue("allowedOptions must contain DEF", allowedMethods.contains(Method.valueOf("DEF")));
        assertEquals(2, allowedMethods.size());
    }
    
    private static Response accessServer(Class<?> klasse, String subPath,
            Method httpMethod, Date modifiedSince, Tag entityTag) {
        Reference reference = createReference(klasse, subPath);
        Client client = new Client(PROTOCOL);
        Request request = new Request(httpMethod, reference);
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(modifiedSince);
        if (entityTag != null)
            conditions.setMatch(Collections.singletonList(entityTag));
        request.setConditions(conditions);
        Response response = client.handle(request);
        return response;
    }
}