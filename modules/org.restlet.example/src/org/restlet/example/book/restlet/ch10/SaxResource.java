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
