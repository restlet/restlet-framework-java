/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.agent.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.AgentException;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.AuthenticationSettings;
import org.restlet.ext.apispark.internal.agent.bean.Credentials;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.bean.User;
import org.restlet.ext.apispark.internal.agent.resource.AuthenticationAuthenticateResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.Verifier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * Authentication module for the agent. This class extends {@link ChallengeAuthenticator} and is responsible to fill
 * {@link org.restlet.data.ClientInfo} on the request.
 * 
 * @author Manuel Boillod
 */
public class AuthenticationModule extends ChallengeAuthenticator {

    private class AgentVerifier implements Verifier {

        @Override
        public int verify(Request request, Response response) {
            int result;

            if (request.getChallengeResponse() == null) {
                result = RESULT_MISSING;
            } else {
                String identifier = request.getChallengeResponse()
                        .getIdentifier();
                char[] secret = request.getChallengeResponse().getSecret();
                UserIdentifier userIdentifier = new UserIdentifier(identifier,
                        secret);

                try {
                    // we have to add secret in cache key because cache loader
                    // needs the secret.
                    UserInfo userInfo = userLoadingCache
                            .getUnchecked(userIdentifier);
                    if (userInfo == null) {
                        throw new AgentException("User could not be null");
                    }
                    // verify password (only after getting user from cache).
                    // See UserIdenfifier javadoc for more details.
                    if (!Arrays.equals(secret, userInfo.getSecret())) {
                        result = RESULT_INVALID;
                    } else {
                        // set user on request client info
                        User user = userInfo.getUser();
                        org.restlet.security.User securityUser = new org.restlet.security.User(
                                identifier, (char[]) null, user.getFirstName(),
                                user.getLastName(), user.getEmail());
                        request.getClientInfo().setUser(securityUser);

                        // set roles on request client info
                        List<Role> securityRoles = new ArrayList<>();
                        Application application = Application.getCurrent();
                        if (user.getGroups() != null) {
                            for (String role : user.getGroups()) {
                                securityRoles.add(new Role(application, role));
                            }
                        }
                        request.getClientInfo().setRoles(securityRoles);

                        result = RESULT_VALID;
                    }
                } catch (UncheckedExecutionException e) {
                    if (e.getCause() instanceof ResourceException) {
                        // client resource exception (status 401 is normal)
                        ResourceException rex = (ResourceException) e
                                .getCause();
                        if (Status.CLIENT_ERROR_UNAUTHORIZED.equals(rex
                                .getStatus())) {
                            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            result = RESULT_INVALID;
                        } else {
                            throw new ResourceException(
                                    Status.SERVER_ERROR_INTERNAL,
                                    "Agent service error during user authentication of user: "
                                            + identifier, rex);
                        }
                    } else {
                        throw new AgentException(
                                "Unexpected error during user authentication error of user: "
                                        + identifier, e);
                    }
                }
            }

            return result;
        }
    }

    /**
     * This class is used as Cache Key. The {@link #secret} is not used in the
     * key, but the {@link CacheLoader} need it.
     * 
     * Warning: The {@link #hashCode()} and {@link #equals(Object)} methods only
     * use the {@link #identifier} attribute. The secret should be compared
     * separately.
     */
    public static class UserIdentifier {

        private String identifier;

        private char[] secret;

        public UserIdentifier(String identifier, char[] secret) {
            this.identifier = identifier;
            this.secret = secret;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof UserIdentifier) {
                UserIdentifier userIdentifier = (UserIdentifier) obj;
                return Objects.equals(identifier, userIdentifier.identifier);
            }
            return false;
        }

        public String getIdentifier() {
            return identifier;
        }

        public char[] getSecret() {
            return secret;
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public void setSecret(char[] secret) {
            this.secret = secret;
        }
    }

    private static class UserInfo {
        private char[] secret;

        private User user;

        private UserInfo(User user, char[] secret) {
            this.user = user;
            this.secret = secret;
        }

        public char[] getSecret() {
            return secret;
        }

        public User getUser() {
            return user;
        }
    }

    public static final String AUTHENTICATE_PATH = "/authentication/authenticate";

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(AuthenticationModule.class.getName());

    private AuthenticationAuthenticateResource authenticateClientResource;

    private AuthenticationSettings authenticationSettings;

    private LoadingCache<UserIdentifier, UserInfo> userLoadingCache;

    /**
     * Create a new Authentication module with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     */
    public AuthenticationModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings) {
        this(apiSparkConfig, modulesSettings, null);
    }

    /**
     * Create a new Authentication module with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     * @param context
     *            The context
     */
    public AuthenticationModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings, Context context) {
        super(context, ChallengeScheme.HTTP_BASIC, "realm");

        authenticationSettings = new AuthenticationSettings();
        authenticationSettings.setOptional(modulesSettings.isAuthorizationModuleEnabled());

        authenticateClientResource = AgentUtils.getClientResource(
                apiSparkConfig, modulesSettings,
                AuthenticationAuthenticateResource.class, AUTHENTICATE_PATH);

        // config ChallengeAuthenticator
        setOptional(authenticationSettings.isOptional());
        setVerifier(new AgentVerifier());

        // Initialize the cache
        initializeCache();
    }

    /**
     * Initializes the user cache and the cache loader instance.
     */
    private void initializeCache() {
        // Cache loader get user from apispark. Never returns null
        CacheLoader<UserIdentifier, UserInfo> userLoader = new CacheLoader<UserIdentifier, UserInfo>() {
            public UserInfo load(UserIdentifier userIdentifier) {
                Credentials credentials = new Credentials(
                        userIdentifier.getIdentifier(),
                        userIdentifier.getSecret());
                User user = authenticateClientResource
                        .authenticate(credentials);
                if (user == null) {
                    // Authentication should throw an error instead of
                    // returning
                    // null
                    throw new AgentException(
                            "Authentication should not return null");
                }
                return new UserInfo(user, userIdentifier.getSecret());
            }
        };

        userLoadingCache = CacheBuilder
                .newBuilder()
                .maximumSize(authenticationSettings.getCacheSize())
                .expireAfterWrite(
                        authenticationSettings.getCacheTimeToLiveSeconds(),
                        TimeUnit.SECONDS).build(userLoader);
    }
}
