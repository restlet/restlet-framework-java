/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jaas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.spi.LoginModule;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/** Unit tests for {@link JaasVerifier}. */
class JaasVerifierTestCase {

    /** Simple principal implementation used to exercise the JAAS login flow. */
    public static class TestPrincipal implements Principal {
        private final String name;

        public TestPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    /** Login module that always succeeds and adds a new principal to the subject. */
    public static class AddingPrincipalLoginModule implements LoginModule {
        private Subject subject;

        @Override
        public void initialize(
                Subject subject,
                CallbackHandler callbackHandler,
                Map<String, ?> sharedState,
                Map<String, ?> options) {
            this.subject = subject;
        }

        @Override
        public boolean login() {
            return true;
        }

        @Override
        public boolean commit() {
            subject.getPrincipals().add(new TestPrincipal("newUser"));
            return true;
        }

        @Override
        public boolean abort() {
            return false;
        }

        @Override
        public boolean logout() {
            return true;
        }
    }

    /**
     * Creates a JAAS {@link Configuration} backed by a single login module entry for the given
     * login module class.
     */
    private static Configuration createConfiguration(
            Class<? extends LoginModule> loginModuleClass) {
        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[] {
                    new AppConfigurationEntry(
                            loginModuleClass.getName(),
                            LoginModuleControlFlag.REQUIRED,
                            Collections.emptyMap())
                };
            }
        };
    }

    @Test
    void testConstructor() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        assertEquals("myApp", verifier.getName());
        assertNull(verifier.getConfiguration());
        assertNull(verifier.getUserPrincipalClassName());
    }

    @Test
    void testSetName() {
        JaasVerifier verifier = new JaasVerifier("initial");
        verifier.setName("updated");
        assertEquals("updated", verifier.getName());
    }

    @Test
    void testSetUserPrincipalClassName() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        verifier.setUserPrincipalClassName("com.example.MyPrincipal");
        assertEquals("com.example.MyPrincipal", verifier.getUserPrincipalClassName());
    }

    @Test
    void testCreateCallbackHandler() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        var handler = verifier.createCallbackHandler(request, response);

        assertNotNull(handler);
        assertInstanceOf(ChallengeCallbackHandler.class, handler);
    }

    @Test
    void testVerifyReturnsInvalidWhenLoginFails() {
        JaasVerifier verifier = new JaasVerifier("nonExistentLoginModule");
        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_INVALID, result);
    }

    @Test
    void testSetConfiguration() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        Configuration configuration = createConfiguration(AddingPrincipalLoginModule.class);

        verifier.setConfiguration(configuration);

        assertEquals(configuration, verifier.getConfiguration());
    }

    @Test
    void testVerifySucceedsAndMergesPrincipalsWithoutDuplicates() {
        JaasVerifier verifier = new JaasVerifier("testRealm");
        verifier.setConfiguration(createConfiguration(AddingPrincipalLoginModule.class));

        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        User user = new User("alice");
        Role role = new Role(null, "admin");
        Principal extraPrincipal = new TestPrincipal("extra");

        request.getClientInfo().setUser(user);
        request.getClientInfo().getRoles().add(role);
        request.getClientInfo().getPrincipals().add(extraPrincipal);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_VALID, result);
        // The user and existing principals must not be duplicated in the principals list.
        assertEquals(
                1, Collections.frequency(request.getClientInfo().getPrincipals(), extraPrincipal));
        assertTrue(
                request.getClientInfo().getPrincipals().stream()
                        .noneMatch(principal -> principal.equals(user)));
        // The principal added by the login module must be merged into the principals list.
        assertTrue(
                request.getClientInfo().getPrincipals().stream()
                        .anyMatch(
                                principal ->
                                        principal instanceof TestPrincipal
                                                && "newUser".equals(principal.getName())));
        // No new user was created since one was already set.
        assertEquals(user, request.getClientInfo().getUser());
    }

    @Test
    void testVerifyCreatesUserWhenPrincipalMatchesConfiguredClassName() {
        JaasVerifier verifier = new JaasVerifier("testRealm");
        verifier.setConfiguration(createConfiguration(AddingPrincipalLoginModule.class));
        verifier.setUserPrincipalClassName(TestPrincipal.class.getName());

        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        assertNull(request.getClientInfo().getUser());

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_VALID, result);
        assertNotNull(request.getClientInfo().getUser());
        assertEquals("newUser", request.getClientInfo().getUser().getIdentifier());
    }

    @Test
    void testVerifyDoesNotOverwriteExistingUserEvenWhenPrincipalClassMatches() {
        JaasVerifier verifier = new JaasVerifier("testRealm");
        verifier.setConfiguration(createConfiguration(AddingPrincipalLoginModule.class));
        verifier.setUserPrincipalClassName(TestPrincipal.class.getName());

        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        User existingUser = new User("bob");
        request.getClientInfo().setUser(existingUser);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_VALID, result);
        assertEquals(existingUser, request.getClientInfo().getUser());
    }
}
