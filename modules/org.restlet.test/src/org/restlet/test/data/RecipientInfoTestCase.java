/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.test.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Parameter;
import org.restlet.data.RecipientInfo;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.RecipientInfoReader;
import org.restlet.engine.header.RecipientInfoWriter;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.data.RecipientInfo}.
 * 
 * @author Jerome Louvel
 */
public class RecipientInfoTestCase extends RestletTestCase {

    public void testVia() {
        Parameter via1a = new Parameter(HeaderConstants.HEADER_VIA,
                "1.0 fred, 1.1 nowhere.com (Apache/1.1)");
        Parameter via1b = new Parameter(HeaderConstants.HEADER_VIA,
                "HTTP/1.0 fred, HTTP/1.1 nowhere.com (Apache/1.1)");
        Parameter via1c = new Parameter(HeaderConstants.HEADER_VIA,
                "HTTP/1.0 fred (Apache/1.1), HTTP/1.1 nowhere.com");

        List<RecipientInfo> recipients = new ArrayList<RecipientInfo>();
        RecipientInfoReader.addValues(via1a, recipients);

        assertEquals(2, recipients.size());

        RecipientInfo recipient1 = recipients.get(0);
        RecipientInfo recipient2 = recipients.get(1);

        assertEquals("1.0", recipient1.getProtocol().getVersion());
        assertEquals("1.1", recipient2.getProtocol().getVersion());

        assertEquals("fred", recipient1.getName());
        assertEquals("nowhere.com", recipient2.getName());

        assertNull(recipient1.getComment());
        assertEquals("Apache/1.1", recipient2.getComment());

        String header = RecipientInfoWriter.write(recipients);
        assertEquals(via1b.getValue(), header);

        recipients = new ArrayList<RecipientInfo>();
        RecipientInfoReader.addValues(via1c, recipients);
        recipient1 = recipients.get(0);
        recipient2 = recipients.get(1);

        assertEquals("1.0", recipient1.getProtocol().getVersion());
        assertEquals("1.1", recipient2.getProtocol().getVersion());

        assertEquals("fred", recipient1.getName());
        assertEquals("nowhere.com", recipient2.getName());

        assertEquals("Apache/1.1", recipient1.getComment());
        assertNull(recipient2.getComment());

    }
}
