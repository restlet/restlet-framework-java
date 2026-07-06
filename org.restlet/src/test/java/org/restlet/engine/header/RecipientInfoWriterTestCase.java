/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Protocol;
import org.restlet.data.RecipientInfo;

class RecipientInfoWriterTestCase {

    @Test
    void write_nameOnly_writesProtocolAndName() {
        RecipientInfo info =
                new RecipientInfo(new Protocol("HTTP", "HTTP", null, -1, "1.1"), "proxy", null);
        assertEquals("HTTP/1.1 proxy", RecipientInfoWriter.write(List.of(info)));
    }

    @Test
    void write_withComment_appendsCommentInParentheses() {
        RecipientInfo info =
                new RecipientInfo(new Protocol("HTTP", "HTTP", null, -1, "1.1"), "proxy", null);
        info.setComment("Apache");
        assertEquals("HTTP/1.1 proxy (Apache)", RecipientInfoWriter.write(List.of(info)));
    }

    @Test
    void append_nullProtocol_throwsIllegalArgumentException() {
        RecipientInfo info = new RecipientInfo();
        RecipientInfoWriter recipientInfoWriter = new RecipientInfoWriter();
        assertThrows(IllegalArgumentException.class, () -> recipientInfoWriter.append(info));
    }

    @Test
    void append_nullName_throwsIllegalArgumentException() {
        RecipientInfo info =
                new RecipientInfo(new Protocol("HTTP", "HTTP", null, -1, "1.1"), null, null);
        RecipientInfoWriter recipientInfoWriter = new RecipientInfoWriter();
        assertThrows(IllegalArgumentException.class, () -> recipientInfoWriter.append(info));
    }
}
