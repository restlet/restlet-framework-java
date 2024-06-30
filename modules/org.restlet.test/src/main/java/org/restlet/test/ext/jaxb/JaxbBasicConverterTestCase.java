/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.jaxb;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxb.JaxbConverter;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.test.RestletTestCase;

/**
 * Tests basic Conversion using the JaxbConverter
 *
 * @author Sanjay Acharya
 */
public class JaxbBasicConverterTestCase extends RestletTestCase {

    @Test
    public void testObjectionToRepresentation() {
        Representation rep = new JaxbConverter().toRepresentation(new Sample(), new Variant(
                MediaType.APPLICATION_XML), null);
        assertInstanceOf(JaxbRepresentation.class, rep);
    }

    @Test
    public void testRepresentationToObject() throws IOException {
        JaxbRepresentation<Sample> sampleRep = new JaxbRepresentation<>(
                MediaType.APPLICATION_XML, new Sample());
        Object rep = new JaxbConverter().toObject(sampleRep, Sample.class, null);
        assertInstanceOf(Sample.class, rep);
    }
}
