/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.service;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the metadata service.
 *
 * @author Jerome Louvel
 */
public class MetadataServiceTestCase {

    @Test
    public void testStrict() {
        MetadataService ms = new MetadataService();
        MediaType ma = ms.getMediaType("ma");
        assertNull(ma);
    }
}
