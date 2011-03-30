package org.restlet.test.ext.oauth.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;
import org.restlet.test.ext.oauth.provider.TimedTokenTest;
import org.restlet.test.ext.oauth.provider.data.TokenGeneratorTest;
import org.restlet.test.ext.oauth.webclient.FacebookTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuthorizationServerTest.class, TimedTokenTest.class,
        FacebookTest.class, ProviderTest.class, TokenGeneratorTest.class })
public class OAuthTestSuite {

}
