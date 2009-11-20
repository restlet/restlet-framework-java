/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.test.ext.jaxb;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.ConverterService;

/**
 * Tests basic Conversion using the JaxbConverter
 * 
 * @author Sanjay Acharya
 */
public class JaxbBasicConverterTest extends TestCase {

    public void testObjectionToRepresentation() {
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
