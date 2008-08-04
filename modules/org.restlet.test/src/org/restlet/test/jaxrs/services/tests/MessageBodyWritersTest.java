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

import java.io.IOException;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.providers.CrazyTypeProvider;
import org.restlet.test.jaxrs.services.providers.ProidersTestProvider;
import org.restlet.test.jaxrs.services.resources.MessageBodyWriterTestResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see ProidersTestProvider
 * @see MessageBodyWriterTestResource
 */
@SuppressWarnings("all")
public class MessageBodyWritersTest extends JaxRsTestCase {

    @Override
    protected Set<Class<?>> getProvClasses() {
        return (Set) TestUtils.createSet(ProidersTestProvider.class,
                CrazyTypeProvider.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return MessageBodyWriterTestResource.class;
    }

    public void test1() throws IOException {
        final Response response = get();
        final Representation entity = response.getEntity();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(new MediaType("text/crazy-person"), entity);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstname>Angela</firstname><lastname>Merkel</lastname></person>Angela Merkel is crazy.\nHeader value for name h1 is h1v",
                entity.getText());
    }
}