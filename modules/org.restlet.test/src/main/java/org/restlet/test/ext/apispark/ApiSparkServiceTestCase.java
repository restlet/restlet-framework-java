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

package org.restlet.test.ext.apispark;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.DefaultConverter;
import org.restlet.ext.apispark.ApiSparkService;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.bean.CallLogs;
import org.restlet.ext.apispark.internal.agent.bean.Credentials;
import org.restlet.ext.apispark.internal.agent.bean.FirewallIpFilter;
import org.restlet.ext.apispark.internal.agent.bean.FirewallRateLimit;
import org.restlet.ext.apispark.internal.agent.bean.FirewallSettings;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.bean.OperationAuthorization;
import org.restlet.ext.apispark.internal.agent.bean.OperationsAuthorization;
import org.restlet.ext.apispark.internal.agent.bean.User;
import org.restlet.ext.apispark.internal.agent.module.AnalyticsModule;
import org.restlet.ext.apispark.internal.agent.module.AuthenticationModule;
import org.restlet.ext.apispark.internal.agent.module.AuthorizationModule;
import org.restlet.ext.apispark.internal.agent.module.FirewallModule;
import org.restlet.ext.apispark.internal.agent.module.ModulesSettingsModule;
import org.restlet.ext.apispark.internal.agent.resource.AnalyticsResource;
import org.restlet.ext.apispark.internal.agent.resource.AuthenticationAuthenticateResource;
import org.restlet.ext.apispark.internal.agent.resource.AuthorizationOperationsResource;
import org.restlet.ext.apispark.internal.agent.resource.FirewallSettingsResource;
import org.restlet.ext.apispark.internal.agent.resource.ModulesSettingsResource;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;
import org.restlet.test.RestletTestCase;

/**
 * @author Manuel Boillod
 */
public class ApiSparkServiceTestCase extends RestletTestCase {

    public static class AgentServerResource extends ServerResource {
        public static int CALL_COUNT = 0;

        public static Request LAST_REQUEST;

        @Get
        public void getCalled() {
            CALL_COUNT++;
            LAST_REQUEST = getRequest();
        }
    }

    /**
     * Mock user Web API with two resources on paths "/test" and "/admin/test".
     * 
     * @author Manuel Boillod
     * 
     */
    public static class UserApiApplication extends Application {
        @Override
        public Restlet createInboundRoot() {
            Router router = new Router();
            router.attach("/test", AgentServerResource.class);
            router.attach("/admin/test", AgentServerResource.class);
            return router;
        }
    }

    /**
     * Mock analytics service. Accepts a CallLogs object and returns a 204 or
     * throws a 500 error.
     * 
     * The variable GET_ANALYTICS_COUNT counts the number of calls to the
     * service.
     * 
     * The variable GET_CALLLOG_COUNT counts the number of CallLog received by
     * the service.
     * 
     * The variable BROKEN allows to simulate a broken analytics service to test
     * retries. When BROKEN is true, the mock analytics module always throws a
     * ResourceException with status code 500.
     * 
     * @author Cyprien Quilici
     * 
     */
    public static class MockAnalyticsServerResource extends ServerResource
            implements AnalyticsResource {
        public static int GET_ANALYTICS_COUNT;

        public static int GET_CALLLOG_COUNT;

        public static boolean BROKEN = false;

        @Override
        public void postLogs(CallLogs callLogs) {
            GET_ANALYTICS_COUNT++;
            if (BROKEN) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
            }
            GET_CALLLOG_COUNT += callLogs.size();
        }
    }

    /**
     * Mock authentication service. Returns a Restlet User if authentication
     * succeeds, throws a 403 if authentication fails and a 500 error if
     * username does not exist.
     * 
     * Valid authentication is defined by login = VALID_USERNAME and password =
     * VALID_PASSWORD. If authentication succeeds, the returned user is given
     * its roles: "user" and "dev".
     * 
     * The variable AUTHENTICATE_COUNT counts the number of authentication calls
     * to the service.
     * 
     * @author Manuel Boillod
     * 
     */
    public static class MockAuthenticationAuthenticateServerResource extends
            ServerResource implements AuthenticationAuthenticateResource {
        public static int AUTHENTICATE_COUNT = 0;

        @Override
        public User authenticate(Credentials credentials) {
            AUTHENTICATE_COUNT++;
            if (VALID_USERNAME.equals(credentials.getUsername())
                    && VALID_PASSWORD.equals(new String(credentials
                            .getPassword()))) {
                User user = new User();
                user.setUsername(VALID_USERNAME);
                user.setGroups(Arrays.asList("user", "dev"));
                return user;
            }
            if (SERVER_ERROR_USERNAME.equals(credentials.getUsername())) {
                throw new RuntimeException("Error username causes an exception");
            }
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
    }

    /**
     * Mock authorization service. Returns an OperationsAuthorization.
     * 
     * Users with role user can access only resource on path "/test" with HTTP
     * method GET.
     * 
     * Users with role admin can access resources on paths "/test" and
     * "/admin/test" with HTTP method GET.
     * 
     * The variable AUTHORIZATIONS_COUNT counts the number of calls to the
     * service.
     * 
     * @author Manuel Boillod
     * 
     */
    public static class MockAuthorizationOperationsServerResource extends
            ServerResource implements AuthorizationOperationsResource {
        public static int AUTHORIZATIONS_COUNT = 0;

        @Override
        public OperationsAuthorization getAuthorizations() {
            AUTHORIZATIONS_COUNT++;
            return new OperationsAuthorization(Arrays.asList(
                    new OperationAuthorization(Method.GET.getName(), "/test",
                            Arrays.asList("user", "admin")),
                    new OperationAuthorization(Method.GET.getName(),
                            "/admin/test", Arrays.asList("admin"))));
        }
    }

    /**
     * Mock firewall service. Returns a FirewallSettings object stored in
     * variable FIREWALL_SETTINGS.
     * 
     * The variable GET_SETTINGS_COUNT counts the number of calls to the
     * service.
     * 
     * @author Manuel Boillod
     * 
     */
    public static class MockFirewallSettingsServerResource extends
            ServerResource implements FirewallSettingsResource {
        public static FirewallSettings FIREWALL_SETTINGS;

        public static int GET_SETTINGS_COUNT = 0;

        @Override
        public FirewallSettings getSettings() {
            GET_SETTINGS_COUNT++;
            return FIREWALL_SETTINGS;
        }
    }

    /**
     * Mock modules settings service. Returns a ModulesSettings object stored in
     * variable MODULES_SETTINGS.
     * 
     * The variable GET_SETTINGS_COUNT counts the number of calls to the
     * service.
     * 
     * @author Manuel Boillod
     * 
     */
    public static class MockModulesSettingsServerResource extends
            ServerResource implements ModulesSettingsResource {
        public static int GET_SETTINGS_COUNT = 0;

        public static ModulesSettings MODULES_SETTINGS;

        @Override
        public ModulesSettings getSettings() {
            GET_SETTINGS_COUNT++;
            return MODULES_SETTINGS;
        }
    }

    /**
     * Port on which the agent service runs, set to DEFAULT_TEST_PORT if no JVM
     * property org.restlet.test.port is set.
     */
    public static int AGENT_SERVICE_PORT = getTestPort();

    /**
     * Port on which the agent runs, set to DEFAULT_TEST_PORT if no JVM property
     * org.restlet.test.port is set.
     */
    public static int AGENT_PORT = AGENT_SERVICE_PORT + 1;

    public static int USER_WEBAPI_PORT = AGENT_SERVICE_PORT + 2;

    private static final String AGENT_SERVICE_URL = "http://localhost:"
            + AGENT_SERVICE_PORT;

    private static final String AGENT_URL = "http://localhost:" + AGENT_PORT;

    private static final String USER_WEBAPI_URL = "http://localhost:"
            + USER_WEBAPI_PORT;

    public static final String BAD_PASSWORD = "dont remember my password";

    private static final int CELL_ID = 123;

    private static final int CELL_VERSION = 2;

    public static final int DEFAULT_TEST_PORT = 1337;

    private static final String PROPERTY_TEST_PORT = "org.restlet.test.port";

    private static final String ROOT_PATH = "/agent/cells/" + CELL_ID
            + "/versions/" + CELL_VERSION;

    public static final String SERVER_ERROR_USERNAME = "userFail";

    public static final String VALID_PASSWORD = "pw15";

    public static final String VALID_USERNAME = "user13";

    private static int getTestPort() {
        return Integer.getInteger(PROPERTY_TEST_PORT, DEFAULT_TEST_PORT);
    }

    private Component agentComponent;

    private Component agentServiceComponent;

    private Component userApiComponent;

    private Response callAgent(String path) throws Exception {
        return callAgent(path, null, null);
    }

    private Response callAgent(String path, String username, String password)
            throws Exception {
        Request request = new Request(Method.GET, AGENT_URL + path);
        request.getClientInfo().accept(MediaType.APPLICATION_JAVA_OBJECT);
        if (username != null) {
            // add authentication scheme
            request.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, username, password));
        }

        Client c = new Client(Protocol.HTTP);
        try {
            return c.handle(request);
        } finally {
            c.stop();
        }
    }

    public ApiSparkService getAgentService() {
        ApiSparkService apiSparkService = new ApiSparkService();
        apiSparkService.setAgentEnabled(true);
        apiSparkService.setAgentServiceUrl(AGENT_SERVICE_URL);
        apiSparkService.setAgentRefreshPeriodInSecond(0);
        apiSparkService.setAgentLogin(VALID_USERNAME);
        apiSparkService.setAgentPassword(VALID_PASSWORD);
        apiSparkService.setAgentCellId(CELL_ID);
        apiSparkService.setAgentCellVersion(CELL_VERSION);
        return apiSparkService;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startAgentApiSparkService();
        AgentServerResource.CALL_COUNT = 0;
        AgentServerResource.LAST_REQUEST = null;
        MockModulesSettingsServerResource.MODULES_SETTINGS = new ModulesSettings();
        MockModulesSettingsServerResource.GET_SETTINGS_COUNT = 0;
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS = new FirewallSettings();
        MockFirewallSettingsServerResource.GET_SETTINGS_COUNT = 0;
        MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT = 0;
        MockAnalyticsServerResource.GET_ANALYTICS_COUNT = 0;
        MockAnalyticsServerResource.GET_CALLLOG_COUNT = 0;
    }

    protected void setUpEngine() {
        super.setUpEngine();
        // we control the available converters.
        Engine.getInstance().getRegisteredConverters().clear();
        Engine.getInstance().getRegisteredConverters()
                .add(new JacksonConverter());
        Engine.getInstance().getRegisteredConverters()
                .add(new DefaultConverter());
    }

    private void startApiSparkService(final ApiSparkService apiSparkService,
            boolean embedded) throws Exception {
        this.agentComponent = new Component();
        this.agentComponent.setName("agent");
        this.agentComponent.getServers().add(Protocol.HTTP, AGENT_PORT);
        this.agentComponent.getClients().add(Protocol.HTTP);

        Application application;
        if (embedded) {
            application = new UserApiApplication();
        } else {
            application = new Application();
        }
        application.getServices().add(apiSparkService);
        this.agentComponent.getDefaultHost().attach(application);
        this.agentComponent.start();
    }

    public void startAgentApiSparkService() throws Exception {
        this.agentServiceComponent = new Component();
        this.agentServiceComponent.setName("agent service");
        this.agentServiceComponent.getServers().add(Protocol.HTTP,
                AGENT_SERVICE_PORT);

        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                MapVerifier mapVerifier = new MapVerifier();
                mapVerifier.getLocalSecrets().put(VALID_USERNAME,
                        VALID_PASSWORD.toCharArray());

                ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
                        getContext(), ChallengeScheme.HTTP_BASIC, "realm");
                authenticator.setVerifier(mapVerifier);

                Router router = new Router();
                router.attach(ROOT_PATH + ModulesSettingsModule.MODULE_PATH,
                        MockModulesSettingsServerResource.class);
                router.attach(ROOT_PATH
                        + AuthenticationModule.AUTHENTICATE_PATH,
                        MockAuthenticationAuthenticateServerResource.class);
                router.attach(ROOT_PATH
                        + AuthorizationModule.OPERATIONS_AUTHORIZATIONS_PATH,
                        MockAuthorizationOperationsServerResource.class);
                router.attach(ROOT_PATH + FirewallModule.SETTINGS_PATH,
                        MockFirewallSettingsServerResource.class);
                router.attach(ROOT_PATH + AnalyticsModule.ANALYTICS_PATH,
                        MockAnalyticsServerResource.class);
                authenticator.setNext(router);

                return authenticator;
            }
        };

        this.agentServiceComponent.getDefaultHost().attach(application);
        this.agentServiceComponent.start();
    }

    public void startUserApi() throws Exception {
        this.userApiComponent = new Component();
        this.userApiComponent.setName("userapi");
        this.userApiComponent.getServers().add(Protocol.HTTP, USER_WEBAPI_PORT);
        this.userApiComponent.getDefaultHost().attach(new UserApiApplication());
        this.userApiComponent.start();
    }

    public void stopAgent() throws Exception {
        if (this.agentComponent != null) {
            this.agentComponent.stop();
        }
        this.agentComponent = null;
    }

    public void stopAgentService() throws Exception {
        if (this.agentServiceComponent != null) {
            this.agentServiceComponent.stop();
        }
        this.agentServiceComponent = null;
    }

    public void stopUserApi() throws Exception {
        if (this.userApiComponent != null) {
            this.userApiComponent.stop();
        }
        this.userApiComponent = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        stopAgentService();
        stopAgent();
        stopUserApi();
    }

    public void testAgent_Authentication_userRequestWithCredentials()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthenticationModuleEnabled(true);

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(1,
                MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);

        // call again (should use cache)
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(1,
                MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);

        // call again with bad password (should use cache)
        response = callAgent("/test", VALID_USERNAME, BAD_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertEquals(1,
                MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);
    }

    public void testAgent_Authentication_userRequestWithCredentials_butServiceError()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthenticationModuleEnabled(true);

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", SERVER_ERROR_USERNAME,
                VALID_PASSWORD);

        // verify
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
        assertEquals(1,
                MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);
    }

    public void testAgent_Authentication_userRequestWithoutCredentials()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthenticationModuleEnabled(true);

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test");

        // verify
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    public void testAgent_Authorization_unknownResource() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthorizationModuleEnabled(true);

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1,
                MockAuthorizationOperationsServerResource.AUTHORIZATIONS_COUNT);

        // call api
        Response response = callAgent("/fromMyMind", VALID_USERNAME,
                VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        assertEquals(1,
                MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);
    }

    public void testAgent_Configuration_AllModulesDisabled() throws Exception {
        startApiSparkService(getAgentService(), true);
        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
    }

    public void testAgent_Configuration_AuthorizationWithoutAuthentication()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthorizationModuleEnabled(true);

        try {
            ApiSparkService apiSparkService = getAgentService();
            startApiSparkService(apiSparkService, true);
            fail("AgentConfigurationException expected");
        } catch (AgentConfigurationException e) {
            assertEquals(
                    "The authorization module requires the authentication module which is not enabled",
                    e.getMessage());
        }
    }

    /**
     * Tests that the timer calls the service for re-configuration
     * 
     * @throws Exception
     */
    public void testAgent_Timer() throws Exception {
        // configure
        ApiSparkService apiSparkService = getAgentService();
        apiSparkService.setAgentRefreshPeriodInSecond(2);
        startApiSparkService(apiSparkService, true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        // Re-configure
        Thread.sleep(TimeUnit.SECONDS.toMillis(3));

        // verify
        assertEquals(2, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

    }

    /**
     * Tests that the timer calls the analytics service
     * 
     * @throws Exception
     */
    public void testAgent_Analytics_Timer() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAnalyticsModuleEnabled(true);

        ApiSparkService apiSparkService = getAgentService();
        apiSparkService.setAgentAnalyticsPostPeriodInSecond(2);
        apiSparkService.createInboundFilter(null);
        startApiSparkService(apiSparkService, true);

        // verify
        assertEquals(0, MockAnalyticsServerResource.GET_ANALYTICS_COUNT);
        assertEquals(0, MockAnalyticsServerResource.GET_CALLLOG_COUNT);

        // Call user's Web API
        callAgent("/test", VALID_USERNAME, VALID_PASSWORD);

        // Re-configure
        Thread.sleep(TimeUnit.SECONDS.toMillis(3));

        // verify
        assertEquals(1, MockAnalyticsServerResource.GET_ANALYTICS_COUNT);
        assertEquals(1, MockAnalyticsServerResource.GET_CALLLOG_COUNT);
    }

    /**
     * Tests that the agent calls the analytics service when buffer reaches max
     * size
     * 
     * @throws Exception
     */
    public void testAgent_Analytics_Broken() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAnalyticsModuleEnabled(true);
        MockAnalyticsServerResource.BROKEN = true;

        ApiSparkService apiSparkService = getAgentService();
        apiSparkService.setAgentAnalyticsBufferSize(1);
        startApiSparkService(apiSparkService, true);

        // verify
        assertEquals(0, MockAnalyticsServerResource.GET_ANALYTICS_COUNT);
        assertEquals(0, MockAnalyticsServerResource.GET_CALLLOG_COUNT);

        // Call user's Web API
        callAgent("/test", VALID_USERNAME, VALID_PASSWORD);

        // The analytics module tries to send the call logs 3 times in 1.5
        // second
        Thread.sleep(1500);

        // verify
        assertTrue("expected count >= 2, current count: " + MockAnalyticsServerResource.GET_ANALYTICS_COUNT,
                MockAnalyticsServerResource.GET_ANALYTICS_COUNT >= 2);
        assertEquals(0, MockAnalyticsServerResource.GET_CALLLOG_COUNT);
    }

    /**
     * Tests that the agent calls the analytics service when buffer reaches max
     * size
     * 
     * @throws Exception
     */

    public void testAgent_Analytics_Buffer() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAnalyticsModuleEnabled(true);
        MockAnalyticsServerResource.BROKEN = false;

        ApiSparkService apiSparkService = getAgentService();
        apiSparkService.setAgentAnalyticsBufferSize(1);

        // verify
        assertEquals(0, MockAnalyticsServerResource.GET_ANALYTICS_COUNT);
        assertEquals(0, MockAnalyticsServerResource.GET_CALLLOG_COUNT);

        startApiSparkService(apiSparkService, true);

        // Call user's Web API
        callAgent("/test", VALID_USERNAME, VALID_PASSWORD);

        // Re-configure
        Thread.sleep(100);

        // verify
        assertTrue("expected count >= 1, current count: " + MockAnalyticsServerResource.GET_ANALYTICS_COUNT,
                MockAnalyticsServerResource.GET_ANALYTICS_COUNT >= 1);
        assertTrue("expected count >= 1, current count: " + MockAnalyticsServerResource.GET_CALLLOG_COUNT,
                MockAnalyticsServerResource.GET_CALLLOG_COUNT >= 1);
    }

    public void testAgent_Configuration_Null() throws Exception {
        try {
            ApiSparkService apiSparkService = new ApiSparkService();
            apiSparkService.setAgentEnabled(true);
            startApiSparkService(apiSparkService, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("The cell identifier is mandatory", e.getMessage());
        }
    }

    public void testAgent_Firewall_ipFilter_blocking() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setFirewallModuleEnabled(true);

        FirewallIpFilter firewallIpFilter = new FirewallIpFilter();
        firewallIpFilter.setWhiteList(true);
        firewallIpFilter.setIps(Arrays.asList("1.1.1.1"));
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS
                .setIpFilters(Arrays.asList(firewallIpFilter));

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    public void testAgent_Firewall_noConfig() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setFirewallModuleEnabled(true);

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
    }

    public void testAgent_Firewall_rateLimitation_blocking_anonymous_user_second_call()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setFirewallModuleEnabled(true);

        FirewallRateLimit firewallRateLimit = new FirewallRateLimit();
        firewallRateLimit.setName("max 1 call per minute");
        firewallRateLimit.setPeriod((int) TimeUnit.MINUTES.toSeconds(1));
        firewallRateLimit.setRateLimit(1);
        firewallRateLimit.setDefaultRateLimit(true);
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS
                .setRateLimits(Arrays.asList(firewallRateLimit));

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", null, null);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // second api call
        response = callAgent("/test", null, null);
        assertEquals(Status.CLIENT_ERROR_TOO_MANY_REQUESTS,
                response.getStatus());
    }

    public void testAgent_Firewall_rateLimitation_blocking_userrole_second_call()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setFirewallModuleEnabled(true);

        FirewallRateLimit firewallRateLimit = new FirewallRateLimit();
        firewallRateLimit.setName("max 1 call per minute");
        firewallRateLimit.setPeriod((int) TimeUnit.MINUTES.toSeconds(1));
        firewallRateLimit.setGroup("user");
        firewallRateLimit.setRateLimit(1);
        firewallRateLimit.setDefaultRateLimit(false);
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS
                .setRateLimits(Arrays.asList(firewallRateLimit));

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // second api call
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_TOO_MANY_REQUESTS,
                response.getStatus());
    }

    public void testAgent_Firewall_rateLimitation_noblocking_userrole_because_another_group()
            throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS
                .setFirewallModuleEnabled(true);

        FirewallRateLimit firewallRateLimit = new FirewallRateLimit();
        firewallRateLimit.setName("max 1 call per minute");
        firewallRateLimit.setPeriod((int) TimeUnit.MINUTES.toSeconds(1));
        firewallRateLimit.setGroup("admin");
        firewallRateLimit.setRateLimit(1);
        firewallRateLimit.setDefaultRateLimit(false);
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS
                .setRateLimits(Arrays.asList(firewallRateLimit));

        // run
        startApiSparkService(getAgentService(), true);

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // second api call
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
    }

    public void testAgent_Redirection_noUrl() throws Exception {
        // run
        ApiSparkService apiSparkService = getAgentService();
        apiSparkService.setReverseProxyEnabled(true);
        try {
            startApiSparkService(apiSparkService, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testAgent_ReverseProxy() throws Exception {
        Context.getCurrentLogger().setLevel(Level.FINE);
        // run
        ApiSparkService apiSparkService = getAgentService();
        apiSparkService.setReverseProxyEnabled(true);
        apiSparkService.setReverseProxyTargetUrl(USER_WEBAPI_URL);
        startApiSparkService(apiSparkService, false);
        startUserApi();

        // verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(1, AgentServerResource.CALL_COUNT);
        assertNotNull(AgentServerResource.LAST_REQUEST);
        assertEquals(USER_WEBAPI_URL + "/test",
                AgentServerResource.LAST_REQUEST.getResourceRef().toString());

        // call api
        response = callAgent("/test?val1=a&val2=b", VALID_USERNAME,
                VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(2, AgentServerResource.CALL_COUNT);
        assertNotNull(AgentServerResource.LAST_REQUEST);
        assertEquals(USER_WEBAPI_URL + "/test?val1=a&val2=b",
                AgentServerResource.LAST_REQUEST.getResourceRef().toString());
    }

    public void testReverseProxy() throws Exception {
        Context.getCurrentLogger().setLevel(Level.FINE);
        // run
        ApiSparkService apiSparkService = new ApiSparkService();
        apiSparkService.setReverseProxyEnabled(true);
        apiSparkService.setReverseProxyTargetUrl(USER_WEBAPI_URL);
        startApiSparkService(apiSparkService, false);
        startUserApi();

        // verify
        assertEquals(0, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(1, AgentServerResource.CALL_COUNT);
        assertNotNull(AgentServerResource.LAST_REQUEST);
        assertEquals(USER_WEBAPI_URL + "/test",
                AgentServerResource.LAST_REQUEST.getResourceRef().toString());

        // call api
        response = callAgent("/test?val1=a&val2=b", VALID_USERNAME,
                VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(2, AgentServerResource.CALL_COUNT);
        assertNotNull(AgentServerResource.LAST_REQUEST);
        assertEquals(USER_WEBAPI_URL + "/test?val1=a&val2=b",
                AgentServerResource.LAST_REQUEST.getResourceRef().toString());
    }

    public void testFirewall() throws Exception {
        Context.getCurrentLogger().setLevel(Level.FINE);
        // run
        ApiSparkService apiSparkService = new ApiSparkService();
        apiSparkService.setFirewallEnabled(true);
        apiSparkService.getFirewallConfig().addRolesPeriodicCounter(1,
                TimeUnit.MINUTES, null, 1);

        // run
        startApiSparkService(apiSparkService, true);

        // verify
        assertEquals(0, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(0, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        // call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // second api call
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_TOO_MANY_REQUESTS,
                response.getStatus());
    }

    public void testLoadConfiguration() throws Exception {
        System.setProperty(
                ApiSparkService.CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY,
                getClass().getResource("agent-configuration.properties")
                        .getPath());
        ApiSparkService apiSparkService = new ApiSparkService();
        apiSparkService.setAgentEnabled(true);
        apiSparkService.loadConfiguration();

        assertEquals(VALID_USERNAME, apiSparkService.getAgentLogin());
        assertEquals(VALID_PASSWORD, apiSparkService.getAgentPassword());
        assertEquals(Integer.valueOf(CELL_ID), apiSparkService.getAgentCellId());
        assertEquals(Integer.valueOf(CELL_VERSION),
                apiSparkService.getAgentCellVersion());
        assertTrue(apiSparkService.isReverseProxyEnabled());
        assertEquals("http://myrealapi.com/",
                apiSparkService.getReverseProxyTargetUrl());
    }
}
