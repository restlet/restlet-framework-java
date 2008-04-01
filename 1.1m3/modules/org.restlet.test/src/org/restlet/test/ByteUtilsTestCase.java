/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.restlet.util.ByteUtils;

/**
 * Test case for the ByteUtils class.
 * 
 * @author Kevin Conaway
 */
public class ByteUtilsTestCase extends TestCase {

    public void testGetStream() throws IOException {
        StringWriter writer = new StringWriter();

        OutputStream out = ByteUtils.getStream(writer);

        out.write("test".getBytes());
        out.flush();
        out.close();
        assertEquals("test", writer.toString());
    }

}
