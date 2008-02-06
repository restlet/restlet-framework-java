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
    public void testParsingBasic() {
        String authenticate1 = "Basic realm=\"Restlet tutorial\"";
        String authorization1 = "Basic c2NvdHQ6dGlnZXI=";

        assertEquals(authorization1, SecurityUtils.format(SecurityUtils
                .parseResponse(null, null, authorization1), null, null));
        assertEquals(authenticate1, SecurityUtils.format(SecurityUtils
                .parseRequest(authenticate1)));
    }

    /**
     * Tests the cookies parsing with Digest authentication.
     */
    public void testParsingDigest() {
        String authenticate1 = "Digest realm=\"realm\", domain=\"/protected/ /alsoProtected/\", qop=\"auth\", algorithm=MD5, nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\"";
        String authorization1 = "Digest cnonce=\"MTE3NzEwMzIwMjkwMDoxNmMzODFiYzRjNWRjMmMyOTVkMWFhNDdkMTQ4OGFlMw==\",qop=auth,uri=\"/protected/asdass\",username=\"admin\",nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\",response=\"a891ebedebb2046b83a9b7540f4e9554\",nc=00000001";

        assertEquals(authorization1, SecurityUtils.format(SecurityUtils
                .parseResponse(null, null, authorization1), null, null));
        assertEquals(authenticate1, SecurityUtils.format(SecurityUtils
                .parseRequest(authenticate1)));
    }
}
