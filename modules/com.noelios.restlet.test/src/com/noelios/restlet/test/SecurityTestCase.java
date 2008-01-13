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

package com.noelios.restlet.test;

import junit.framework.TestCase;

import com.noelios.restlet.util.SecurityUtils;

/**
 * Unit tests for the SecurityData related classes.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SecurityTestCase extends TestCase {
    /**
     * Tests the cookies parsing.
     */
    public void testParsing() {
        String authenticate1 = "Basic realm=\"Restlet tutorial\"";
        String authorization1 = "Basic c2NvdHQ6dGlnZXI=";

        assertEquals(authorization1, SecurityUtils.format(SecurityUtils
                .parseResponse(null, null, authorization1), null, null));
        assertEquals(authenticate1, SecurityUtils.format(SecurityUtils
                .parseRequest(authenticate1)));
    }

}
