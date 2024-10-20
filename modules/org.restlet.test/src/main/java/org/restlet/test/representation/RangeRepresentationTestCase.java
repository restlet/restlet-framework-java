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

package org.restlet.test.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.data.Range;
import org.restlet.engine.application.RangeRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the {@link RangeRepresentation} class.
 * 
 * @author Jerome Louvel
 */
public class RangeRepresentationTestCase extends RestletTestCase {

    @Test
    public void testAppendable() throws Exception {
        StringRepresentation sr = new StringRepresentation("1234567890");
        RangeRepresentation rr = new RangeRepresentation(sr);
        rr.setRange(new Range(2, 5));
        assertNull(sr.getRange());
        assertEquals(10, sr.getAvailableSize());
        assertEquals(5, rr.getAvailableSize());
        assertEquals("1234567890", sr.getText());
        assertEquals("34567", rr.getText());
    }

    @Test
    public void testSize() throws Exception {
        StringRepresentation sr = new StringRepresentation("1234567890");
        RangeRepresentation rr = new RangeRepresentation(sr);
        rr.setRange(new Range(5, 10000));
        assertNull(sr.getRange());
        assertEquals(10, sr.getAvailableSize());
        assertEquals(5, rr.getAvailableSize());
        assertEquals("1234567890", sr.getText());
        assertEquals("67890", rr.getText());
    }

}
