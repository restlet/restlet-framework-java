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

import org.junit.jupiter.api.Test;
import org.restlet.data.Language;

class MetadataWriterTestCase {

    @Test
    void append_metadata_writesItsName() {
        MetadataWriter<Language> writer = new MetadataWriter<>();
        writer.append(Language.ENGLISH);
        assertEquals(Language.ENGLISH.getName(), writer.toString());
    }
}
