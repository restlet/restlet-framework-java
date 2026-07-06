/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

class MetadataExtensionTestCase {

    @Test
    void getName_returnsConstructedName() {
        MetadataExtension extension = new MetadataExtension("json", MediaType.APPLICATION_JSON);
        assertEquals("json", extension.getName());
    }

    @Test
    void getMetadata_returnsConstructedMetadata() {
        MetadataExtension extension = new MetadataExtension("json", MediaType.APPLICATION_JSON);
        assertEquals(MediaType.APPLICATION_JSON, extension.getMetadata());
    }

    @Test
    void getMediaType_castsMetadataToMediaType() {
        MetadataExtension extension = new MetadataExtension("json", MediaType.APPLICATION_JSON);
        assertEquals(MediaType.APPLICATION_JSON, extension.getMediaType());
    }

    @Test
    void getCharacterSet_castsMetadataToCharacterSet() {
        MetadataExtension extension = new MetadataExtension("utf8", CharacterSet.UTF_8);
        assertEquals(CharacterSet.UTF_8, extension.getCharacterSet());
    }

    @Test
    void getEncoding_castsMetadataToEncoding() {
        MetadataExtension extension = new MetadataExtension("gzip", Encoding.GZIP);
        assertEquals(Encoding.GZIP, extension.getEncoding());
    }

    @Test
    void getLanguage_castsMetadataToLanguage() {
        MetadataExtension extension = new MetadataExtension("en", Language.ENGLISH);
        assertEquals(Language.ENGLISH, extension.getLanguage());
    }
}
