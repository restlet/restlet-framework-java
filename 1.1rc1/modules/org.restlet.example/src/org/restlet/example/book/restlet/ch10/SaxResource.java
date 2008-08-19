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

package org.restlet.example.book.restlet.ch10;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.SaxRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 *
 */
public class SaxResource extends Resource {

    public SaxResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;

        final XmlWriter w = new XmlWriter();
        w.setDataFormat(true);
        w.setIndentStep(2);
        try {
            w.startDocument();
            w.startElement("Person");
            w.dataElement("name", "Jane Smith");
            w.dataElement("date-of-birth", "1965-05-23");
            w.dataElement("citizenship", "US");
            w.endElement("Person");
            w.endDocument();
            rep = new SaxRepresentation(MediaType.TEXT_XML);
        } catch (final SAXException e) {
            e.printStackTrace();
        }

        return rep;
    }
}
