/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.RecipientInfoReader;
import org.restlet.engine.header.RecipientInfoWriter;

/**
 * Test {@link org.restlet.data.RecipientInfo}.
 *
 * @author Jerome Louvel
 */
class RecipientInfoTestCase {

    @Test
    void testVia() {
        Header via1a =
                new Header(HeaderConstants.HEADER_VIA, "1.0 fred, 1.1 nowhere.com (Apache/1.1)");
        Header via1b =
                new Header(
                        HeaderConstants.HEADER_VIA,
                        "HTTP/1.0 fred, HTTP/1.1 nowhere.com (Apache/1.1)");
        Header via1c =
                new Header(
                        HeaderConstants.HEADER_VIA,
                        "HTTP/1.0 fred (Apache/1.1), HTTP/1.1 nowhere.com");
        Header via1d =
                new Header(
                        HeaderConstants.HEADER_VIA,
                        "HTTP/1.0 fred (Apache/1.1), HTTP/1.1 nowhere.com:8111");

        List<RecipientInfo> recipients = new ArrayList<>();
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

        recipients = new ArrayList<>();
        RecipientInfoReader.addValues(via1c, recipients);
        recipient1 = recipients.get(0);
        recipient2 = recipients.get(1);

        assertEquals("1.0", recipient1.getProtocol().getVersion());
        assertEquals("1.1", recipient2.getProtocol().getVersion());

        assertEquals("fred", recipient1.getName());
        assertEquals("nowhere.com", recipient2.getName());

        assertEquals("Apache/1.1", recipient1.getComment());
        assertNull(recipient2.getComment());

        recipients = new ArrayList<>();
        RecipientInfoReader.addValues(via1d, recipients);
        recipient1 = recipients.get(0);
        recipient2 = recipients.get(1);

        assertEquals("1.0", recipient1.getProtocol().getVersion());
        assertEquals("1.1", recipient2.getProtocol().getVersion());

        assertEquals("fred", recipient1.getName());
        assertEquals("nowhere.com:8111", recipient2.getName());

        assertEquals("Apache/1.1", recipient1.getComment());
        assertNull(recipient2.getComment());

        header = RecipientInfoWriter.write(recipients);
        assertEquals(via1d.getValue(), header);
    }
}
