/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test case for the {@link AppendableRepresentation} class.
 * 
 * @author Jerome Louvel
 */
public class AppendableRepresentationTestCase {

    @Test
    public void testAppendable() throws Exception {
        AppendableRepresentation ar = new AppendableRepresentation();
        ar.append("abcd");
        ar.append("efgh");
        assertEquals("abcdefgh", ar.getText());
    }

}
