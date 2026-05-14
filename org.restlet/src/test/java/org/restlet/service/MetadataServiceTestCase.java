/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/**
 * Unit tests for the metadata service.
 *
 * @author Jerome Louvel
 */
class MetadataServiceTestCase {

    @Test
    void testStrict() {
        MetadataService ms = new MetadataService();
        MediaType ma = ms.getMediaType("ma");
        assertNull(ma);
    }
}
