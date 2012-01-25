/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.oauth;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.internal.AuthenticatedUserImpl;
import org.restlet.ext.oauth.internal.ExpireToken;
import org.restlet.ext.oauth.internal.MemTokenGenerator;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenGenerator;
import org.restlet.ext.oauth.internal.UnlimitedToken;
import org.restlet.test.RestletTestCase;

public class TokenGeneratorTestCase extends RestletTestCase{
    
    /**
     * Test that the generator creates unique codes
     * @throws Exception
     */
    public void testCodeGeneratorSequence() throws Exception {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(5);
        TokenGenerator generator = new MemTokenGenerator(pool);
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890", null);
        Set <String> codes = new HashSet <String> ();
        for (int i = 0; i < 100; i++) {
            codes.add(generator.generateCode(user));
        }
        assertEquals(100, codes.size());
    }

    /**
     * Test that a token is properly generated
     * @throws Exception
     */
    public void testTokenGenerator() throws Exception {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(5);
        TokenGenerator generator = new MemTokenGenerator(pool);
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890", null);
        Token token = generator.generateToken(user, Token.UNLIMITED);
        assertEquals("1234567890", token.getUser().getId());
        assertTrue(token instanceof UnlimitedToken);
    }

    /**
     * test that token expire time is properly set
     * @throws Exception
     */
    public void testTimeTokenGeneratorSequence() throws Exception {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(5);
        TokenGenerator generator = new MemTokenGenerator(pool);
        generator.setMaxTokenTime(10);
        Random r = new Random();
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890", null);
        for (int i = 0; i < 10; i++) {
            int exp = r.nextInt(15) + 1;
            ExpireToken et = (ExpireToken) generator.generateToken(user, exp);
            if(exp > 10)
                assertEquals(10, et.getExpirePeriod());
            else
                assertEquals(exp, et.getExpirePeriod());
        }
    }

}
