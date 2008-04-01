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

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.SimpleHouse;

/**
 * One of the first Test classes.
 * 
 * @author Stephan Koops
 * @see SimpleHouse
 */
public class SimpleHouseTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return SimpleHouse.class;
    }

    public void testGetHtmlText() throws Exception {
        Response response = get(MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testGetPlainText() throws Exception {
        Response response = get(MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(SimpleHouse.RERP_PLAIN_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetTextAll() throws Exception {
        Response response = get(MediaType.TEXT_ALL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(SimpleHouse.RERP_PLAIN_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetNull() throws Exception {
        Response response = get("null");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        Representation entity = response.getEntity();
        if(entity != null)
            assertEquals(null, entity.getText());
    }

    public void testGetNullWithMediaType() throws Exception {
        Response response = get("nullWithMediaType");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        Representation entity = response.getEntity();
        if(entity != null)
            assertEquals(null, entity.getText());
    }
}