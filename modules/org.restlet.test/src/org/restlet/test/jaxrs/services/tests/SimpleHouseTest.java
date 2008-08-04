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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
        final Response response = get(MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testGetNull() throws Exception {
        final Response response = get("null");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        final Representation entity = response.getEntity();
        if (entity != null) {
            assertEquals(null, entity.getText());
        }
    }

    public void testGetNullWithMediaType() throws Exception {
        final Response response = get("nullWithMediaType");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        final Representation entity = response.getEntity();
        if (entity != null) {
            assertEquals(null, entity.getText());
        }
    }

    public void testGetPlainText() throws Exception {
        final Response response = get(MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEquals(SimpleHouse.RERP_PLAIN_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetTextAll() throws Exception {
        final Response response = get(MediaType.TEXT_ALL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEquals(SimpleHouse.RERP_PLAIN_TEXT, entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }
}