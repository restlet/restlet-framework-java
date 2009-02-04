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

package org.restlet.test.resource;

import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Resource test case.
 * 
 * @author Kevin Conaway
 * @author Konstantin Laufer (laufer@cs.luc.edu)
 * @author Jerome Louvel
 */
public class ResourceTestCase extends RestletTestCase {

    public class AutoDetectResource extends Resource {

        public Representation representXml() {
            return new StringRepresentation("<root>test</root>",
                    MediaType.TEXT_XML);
        }

        public Representation representHtmlEn() {
            return new StringRepresentation("<html>test EN</html>",
                    MediaType.TEXT_HTML);
        }

        public Representation representHtmlFr() {
            return new StringRepresentation("<html>test FR</html>",
                    MediaType.TEXT_HTML);
        }

    }

//    public void testAutoDetect() throws IOException {
//        Application.setCurrent(new Application());
//        Request request = new Request(Method.GET, "http://www.test.com/root");
//        request.getClientInfo().getAcceptedMediaTypes().add(
//                new Preference<MediaType>(MediaType.TEXT_XML));
//        Response response = new Response(request);
//
//        Resource target = new AutoDetectResource();
//        target.init(new Context(), request, response);
//        target.handleGet();
//        Representation rep = response.getEntity();
//
//        assertNotNull(rep);
//        assertEquals(MediaType.TEXT_XML, rep.getMediaType());
//
//        request.getClientInfo().getAcceptedMediaTypes().add(0,
//                new Preference<MediaType>(MediaType.TEXT_HTML));
//        target.handleGet();
//        rep = response.getEntity();
//
//        assertNotNull(rep);
//        assertEquals(MediaType.TEXT_HTML, rep.getMediaType());
//        assertEquals("<html>test EN</html>", rep.getText());
//
//        request.getClientInfo().getAcceptedLanguages().add(
//                new Preference<Language>(Language.FRENCH_FRANCE));
//        target.handleGet();
//        rep = response.getEntity();
//
//        assertNotNull(rep);
//        assertEquals(MediaType.TEXT_HTML, rep.getMediaType());
//        assertEquals("<html>test FR</html>", rep.getText());
//    }

    public void testIsAvailable() {
        Resource r = new Resource();
        assertTrue(r.isAvailable());
        r.init(null, null, null);
        assertTrue(r.isAvailable());

        r = new Resource(null, null, null);
        assertTrue(r.isAvailable());
    }

    public void testIsModifiable() {
        Resource r = new Resource();
        assertFalse(r.isModifiable());
        r.setModifiable(true);
        assertTrue(r.isModifiable());
        r.init(null, null, null);
        assertTrue(r.isModifiable());

        r = new Resource(null, null, null);
        assertFalse(r.isModifiable());
    }

}
