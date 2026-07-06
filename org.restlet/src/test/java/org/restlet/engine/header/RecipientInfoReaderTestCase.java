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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Header;
import org.restlet.data.RecipientInfo;

class RecipientInfoReaderTestCase {

    @Test
    void readValue_implicitHttpProtocol_defaultsProtocolNameToHttp() throws IOException {
        RecipientInfoReader reader = new RecipientInfoReader("1.1 proxy.example.com");
        RecipientInfo info = reader.readValue();
        assertEquals("HTTP", info.getProtocol().getName());
        assertEquals("1.1", info.getProtocol().getVersion());
        assertEquals("proxy.example.com", info.getName());
    }

    @Test
    void readValue_explicitProtocol_parsesNameAndVersion() throws IOException {
        RecipientInfoReader reader = new RecipientInfoReader("FSTR/2.1 proxy.example.com");
        RecipientInfo info = reader.readValue();
        assertEquals("FSTR", info.getProtocol().getName());
        assertEquals("2.1", info.getProtocol().getVersion());
    }

    @Test
    void readValue_withComment_parsesComment() throws IOException {
        RecipientInfoReader reader = new RecipientInfoReader("1.1 proxy.example.com (Apache)");
        RecipientInfo info = reader.readValue();
        assertEquals("proxy.example.com", info.getName());
        assertEquals("Apache", info.getComment());
    }

    @Test
    void readValue_emptyProtocolToken_throws() {
        RecipientInfoReader reader = new RecipientInfoReader("");
        assertThrows(IOException.class, reader::readValue);
    }

    @Test
    void addValuesStatic_fromHeader_populatesCollection() {
        List<RecipientInfo> infos = new ArrayList<>();
        Header header = new Header("Via", "1.1 proxy.example.com");
        RecipientInfoReader.addValues(header, infos);
        assertEquals(1, infos.size());
    }
}
