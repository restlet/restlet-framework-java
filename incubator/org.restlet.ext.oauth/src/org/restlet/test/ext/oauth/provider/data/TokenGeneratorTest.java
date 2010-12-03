package org.restlet.test.ext.oauth.provider.data;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.ext.oauth.provider.data.AuthenticatedUser;
import org.restlet.ext.oauth.provider.data.ExpireToken;
import org.restlet.ext.oauth.provider.data.Token;
import org.restlet.ext.oauth.provider.data.TokenGenerator;
import org.restlet.ext.oauth.provider.data.impl.AuthenticatedUserImpl;
import org.restlet.ext.oauth.provider.data.impl.MemTokenGenerator;

public class TokenGeneratorTest {
    private static ScheduledThreadPoolExecutor pool;

    @BeforeClass
    public static void initGenerator() throws Exception {
        pool = new ScheduledThreadPoolExecutor(5);
    }

    @Test
    public void testCodeGeneratorSequence() throws Exception {
        TokenGenerator generator = new MemTokenGenerator(pool);
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890");
        for (int i = 0; i < 10; i++) {
            String token = generator.generateCode(user);
            System.out.println(i + ":" + token);
        }
    }

    @Test
    public void testTokenGeneratorSequence() throws Exception {
        TokenGenerator generator = new MemTokenGenerator(pool);
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890");
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
        AuthenticatedUser user = new AuthenticatedUserImpl("1234567890");
        for (int i = 0; i < 10; i++) {
            ExpireToken et = (ExpireToken) generator.generateToken(user, 1);
            System.out.println(i + " token   :" + et.getToken());
            System.out.println(i + " refresh :" + et.getRefreshToken());
        }
    }

}
