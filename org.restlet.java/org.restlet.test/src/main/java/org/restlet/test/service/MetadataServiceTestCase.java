/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.service;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.service.MetadataService;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the metadata service.
 *
 * @author Jerome Louvel
 */
public class MetadataServiceTestCase extends RestletTestCase {

    @Test
    public void testStrict() {
        MetadataService ms = new MetadataService();
        MediaType ma = ms.getMediaType("ma");
        assertNull(ma);
    }
}
