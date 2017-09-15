/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxb;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.ConverterService;
import org.restlet.test.RestletTestCase;

/**
 * Tests basic Conversion using the JaxbConverter
 * 
 * @author Sanjay Acharya
 */
public class JaxbBasicConverterTestCase extends RestletTestCase {

    public void testObjectionToRepresentation() throws IOException {
        ConverterService cs = new ConverterService();
        Representation rep = cs.toRepresentation(new Sample(), new Variant(
                MediaType.APPLICATION_XML), null);
        assertTrue(rep instanceof JaxbRepresentation<?>);
    }

    public void testRepresentationToObject() throws IOException, JAXBException {
        ConverterService cs = new ConverterService();
        JaxbRepresentation<Sample> sampleRep = new JaxbRepresentation<Sample>(
                MediaType.APPLICATION_XML, new Sample());
        Object rep = cs.toObject(sampleRep, Sample.class, null);
        assertTrue(rep instanceof Sample);
    }
}
