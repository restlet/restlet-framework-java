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

package org.restlet.test.ext.sip;

import org.junit.Test;
import org.restlet.ext.sip.EventType;
import org.restlet.ext.sip.internal.EventTypeReader;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the Event type like headers.
 * 
 * @author Thierry Boileau
 */
@Deprecated
public class EventTypeTestCase extends RestletTestCase {

    @Test
    public void testParsing() throws Exception {
        String str = "presence";

        EventTypeReader r = new EventTypeReader(str);
        EventType e = r.readValue();
        assertEquals("presence", e.getPackage());
        assertTrue(e.getEventTemplates().isEmpty());

        str = "presence.template1.template2 ";
        r = new EventTypeReader(str);
        e = r.readValue();
        assertEquals("presence", e.getPackage());
        assertEquals(2, e.getEventTemplates().size());
        assertEquals("template1", e.getEventTemplates().get(0));
        assertEquals("template2", e.getEventTemplates().get(1));
    }

    @Test
    public void testWriting() {
    }

}
