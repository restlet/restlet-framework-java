/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.ext.oauth.provider;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.internal.AuthenticatedUserImpl;
import org.restlet.ext.oauth.internal.ExpireToken;
import org.restlet.ext.oauth.internal.MemTokenGenerator;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenGenerator;

public class TokenGeneratorTest {
    private static ScheduledThreadPoolExecutor pool;

    @BeforeClass
    public static void initGenerator() throws Exception {
        pool = new ScheduledThreadPoolExecutor(5);
    }

    @Test
    public void testCodeGeneratorSequence() throws Exception {
        TokenGenerator generator = new MemTokenGenerator(pool);
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890", null);
        for (int i = 0; i < 10; i++) {
            String token = generator.generateCode(user);
            System.out.println(i + ":" + token);
        }
    }

    @Test
    public void testTokenGeneratorSequence() throws Exception {
        TokenGenerator generator = new MemTokenGenerator(pool);
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890", null);
        for (int i = 0; i < 10; i++) {
            String token = generator.generateToken(user, Token.UNLIMITED)
                    .getToken();
            System.out.println(i + ":" + token);
        }
    }

    @Test
    public void testTimeTokenGeneratorSequence() throws Exception {
        TokenGenerator generator = new MemTokenGenerator(pool);
        generator.setMaxTokenTime(1); // allow for timed tokens of max 1
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890", null);
        for (int i = 0; i < 10; i++) {
            ExpireToken et = (ExpireToken) generator.generateToken(user, 1);
            System.out.println(i + " token   :" + et.getToken());
            System.out.println(i + " refresh :" + et.getRefreshToken());
        }
    }

}
