/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.test.ext.sip;

import junit.framework.TestCase;

import org.junit.Test;
import org.restlet.data.Parameter;
import org.restlet.ext.sip.Availability;
import org.restlet.ext.sip.internal.AvailabilityReader;

/**
 * Test case for the Availability reader and writer.
 * 
 * @author Thierry Boileau
 * 
 */
public class AvailabilityTestCase extends TestCase {

    @Test
    public void testParsing() throws Exception {
        String str = "18000;duration=3600;tag=hyh8";
        AvailabilityReader r = new AvailabilityReader(str);
        Availability a = r.readValue();

        assertEquals(18000, a.getDelay());
        assertEquals(3600, a.getDuration());
        assertNull(a.getComment());
        assertEquals(1, a.getParameters().size());

        Parameter parameter = a.getParameters().get(0);

        assertEquals("tag", parameter.getName());
        assertEquals("hyh8", parameter.getValue());

        str = "120 (I'm in a meeting)";
        r = new AvailabilityReader(str);
        a = r.readValue();
        assertEquals(120, a.getDelay());
        assertEquals(0, a.getDuration());
        assertEquals("I'm in a meeting", a.getComment());
        assertEquals(0, a.getParameters().size());
    }

    @Test
    public void testWriting() {
    }

}
