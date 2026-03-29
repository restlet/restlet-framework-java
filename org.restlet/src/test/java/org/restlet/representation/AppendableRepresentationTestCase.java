/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test case for the {@link AppendableRepresentation} class.
 *
 * @author Jerome Louvel
 */
class AppendableRepresentationTestCase {

    @Test
    void testAppendable() throws Exception {
        AppendableRepresentation ar = new AppendableRepresentation();
        ar.append("abcd");
        ar.append("efgh");
        assertEquals("abcdefgh", ar.getText());
    }
}
