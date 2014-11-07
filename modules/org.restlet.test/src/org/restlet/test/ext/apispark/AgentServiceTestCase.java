package org.restlet.test.ext.apispark;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.agent.AgentConfig;
import org.restlet.ext.apispark.AgentService;
import org.restlet.ext.apispark.internal.agent.bean.Credentials;
import org.restlet.ext.apispark.internal.agent.bean.FirewallIpFilter;
import org.restlet.ext.apispark.internal.agent.bean.FirewallRateLimit;
import org.restlet.ext.apispark.internal.agent.bean.FirewallSettings;
import org.restlet.ext.apispark.internal.agent.bean.OperationAuthorization;
import org.restlet.ext.apispark.internal.agent.bean.OperationsAuthorization;
import org.restlet.ext.apispark.internal.agent.bean.User;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.module.AuthenticationModule;
import org.restlet.ext.apispark.internal.agent.module.AuthorizationModule;
import org.restlet.ext.apispark.internal.agent.module.FirewallModule;
import org.restlet.ext.apispark.internal.agent.module.ModulesSettingsModule;
import org.restlet.ext.apispark.internal.agent.resource.AuthenticationAuthenticateResource;
import org.restlet.ext.apispark.internal.agent.resource.AuthorizationOperationsResource;
import org.restlet.ext.apispark.internal.agent.resource.FirewallSettingsResource;
import org.restlet.ext.apispark.internal.agent.resource.ModulesSettingsResource;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;
import org.restlet.test.RestletTestCase;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Manuel Boillod
 */
public class AgentServiceTestCase extends RestletTestCase  {

    private static Logger logger = Context.getCurrentLogger();

    public static final int DEFAULT_TEST_PORT = 1337;

    private static final String PROPERTY_TEST_PORT = "org.restlet.test.port";

    public static int AGNET_SERVICE_PORT = getTestPort();
    public static int AGENT_PORT = getTestPort() + 1;

    private static int getTestPort() {
        if (System.getProperties().containsKey(PROPERTY_TEST_PORT)) {
            return Integer.parseInt(System.getProperty(PROPERTY_TEST_PORT));
        }

        return DEFAULT_TEST_PORT;
    }

    private static final String AGENT_SERVICE_URL = "http://localhost:" + AGNET_SERVICE_PORT;
    private static final String AGENT_URL = "http://localhost:" + AGENT_PORT;

    public static final String VALID_USERNAME = "user13";
    public static final String VALID_PASSWORD = "pw15";
    public static final String BAD_PASSWORD = "dont remember my password";

    public static final String SERVER_ERROR_USERNAME = "userFail";

    private static final int CELL_ID = 123;
    private static final int CELL_VERSION = 2;
    private static final String ROOT_PATH = "/agent/cells/" + CELL_ID + "/versions/" + CELL_VERSION;


    private Component agentServiceComponent;

    private Component agentComponent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startAgentService();
        MockModulesSettingsServerResource.MODULES_SETTINGS = new ModulesSettings();
        MockModulesSettingsServerResource.GET_SETTINGS_COUNT = 0;
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS = new FirewallSettings();
        MockFirewallSettingsServerResource.GET_SETTINGS_COUNT = 0;
        MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        stopAgentService();
        stopAgent();
    }

    public void startAgentService() throws Exception {
        this.agentServiceComponent = new Component();
        this.agentServiceComponent.setName("agent service");
        this.agentServiceComponent.getServers().add(Protocol.HTTP, AGNET_SERVICE_PORT);

        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                MapVerifier mapVerifier = new MapVerifier();
                mapVerifier.getLocalSecrets().put(VALID_USERNAME, VALID_PASSWORD.toCharArray());

                ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
                        getContext(), ChallengeScheme.HTTP_BASIC, "realm");
                authenticator.setVerifier(mapVerifier);

                Router router = new Router();
                router.attach(ROOT_PATH + ModulesSettingsModule.MODULE_PATH, MockModulesSettingsServerResource.class);
                router.attach(ROOT_PATH + AuthenticationModule.AUTHENTICATE_PATH, MockAuthenticationAuthenticateServerResource.class);
                router.attach(ROOT_PATH + AuthorizationModule.OPERATIONS_AUTHORIZATIONS_PATH, MockAuthorizationOperationsServerResource.class);
                router.attach(ROOT_PATH + FirewallModule.SETTINGS_PATH, MockFirewallSettingsServerResource.class);
                authenticator.setNext(router);

                return authenticator;
            }
        };

        this.agentServiceComponent.getDefaultHost().attach(application);
        this.agentServiceComponent.start();
    }

    public void stopAgentService() throws Exception {
        if (this.agentServiceComponent != null) {
            this.agentServiceComponent.stop();
        }
        this.agentServiceComponent = null;
    }

    public void stopAgent() throws Exception {
        if (this.agentComponent != null) {
            this.agentComponent.stop();
        }
        this.agentComponent = null;
    }

    public AgentConfig getConnectorAgentConfig() {
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setAgentServicePath(AGENT_SERVICE_URL);
        agentConfig.setAgentUsername(VALID_USERNAME);
        agentConfig.setAgentSecretKey(VALID_PASSWORD);
        agentConfig.setCellId(CELL_ID);
        agentConfig.setCellVersion(CELL_VERSION);
        return agentConfig;
    }

    public void testConfiguration_Null() throws Exception {
        AgentConfig agentConfig = new AgentConfig();
        try {
            startAgent(agentConfig);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }


    public void testConfiguration_AllModulesDisabled() throws Exception {
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);
        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
    }

    public void testConfiguration_AuthorizationWithoutAuthentication() throws Exception {
        // configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthorizationModuleEnabled(true);

        AgentConfig agentConfig = new AgentConfig();
        try {
            startAgent(agentConfig);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAuthentication_userRequestWithoutCredentials() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test");

        //verify
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    public void testAuthentication_userRequestWithCredentials_butServiceError() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", SERVER_ERROR_USERNAME, VALID_PASSWORD);

        //verify
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
        assertEquals(1, MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);
    }

    public void testAuthentication_userRequestWithCredentials() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(1, MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);

        //call again (should use cache)
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(1, MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);

        //call again with bad password (should use cache)
        response = callAgent("/test", VALID_USERNAME, BAD_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertEquals(1, MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);
    }

    public void testAuthorization_unknownResource() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthorizationModuleEnabled(true);

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockAuthorizationOperationsServerResource.AUTHORIZATIONS_COUNT);

        //call api
        Response response = callAgent("/fromMyMind", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        assertEquals(1, MockAuthenticationAuthenticateServerResource.AUTHENTICATE_COUNT);
    }

    public void testFirewall_noConfig() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setFirewallModuleEnabled(true);

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
    }

    public void testFirewall_ipFilter_blocking() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setFirewallModuleEnabled(true);

        FirewallIpFilter firewallIpFilter = new FirewallIpFilter();
        firewallIpFilter.setWhiteList(true);
        firewallIpFilter.setIps(Arrays.asList("1.1.1.1"));
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS.setIpFilters(
                Arrays.asList(firewallIpFilter));

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    public void testFirewall_rateLimitation_blocking_anonymous_user_second_call() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS.setFirewallModuleEnabled(true);

        FirewallRateLimit firewallRateLimit = new FirewallRateLimit();
        firewallRateLimit.setName("max 1 call per minute");
        firewallRateLimit.setPeriod((int)TimeUnit.MINUTES.toSeconds(1));
        firewallRateLimit.setRateLimit(1);
        firewallRateLimit.setDefaultRateLimit(true);
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS.setRateLimits(
                Arrays.asList(firewallRateLimit));

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        //second api call
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_TOO_MANY_REQUESTS, response.getStatus());
    }

    public void testFirewall_rateLimitation_blocking_userrole_second_call() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS.setFirewallModuleEnabled(true);

        FirewallRateLimit firewallRateLimit = new FirewallRateLimit();
        firewallRateLimit.setName("max 1 call per minute");
        firewallRateLimit.setPeriod((int)TimeUnit.MINUTES.toSeconds(1));
        firewallRateLimit.setGroup("user");
        firewallRateLimit.setRateLimit(1);
        firewallRateLimit.setDefaultRateLimit(false);
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS.setRateLimits(
                Arrays.asList(firewallRateLimit));

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        //second api call
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.CLIENT_ERROR_TOO_MANY_REQUESTS, response.getStatus());
    }

    public void testFirewall_rateLimitation_noblocking_userrole_because_another_group() throws Exception {
        //configure
        MockModulesSettingsServerResource.MODULES_SETTINGS.setAuthenticationModuleEnabled(true);
        MockModulesSettingsServerResource.MODULES_SETTINGS.setFirewallModuleEnabled(true);

        FirewallRateLimit firewallRateLimit = new FirewallRateLimit();
        firewallRateLimit.setName("max 1 call per minute");
        firewallRateLimit.setPeriod((int)TimeUnit.MINUTES.toSeconds(1));
        firewallRateLimit.setGroup("admin");
        firewallRateLimit.setRateLimit(1);
        firewallRateLimit.setDefaultRateLimit(false);
        MockFirewallSettingsServerResource.FIREWALL_SETTINGS.setRateLimits(
                Arrays.asList(firewallRateLimit));

        //run
        AgentConfig agentConfig = getConnectorAgentConfig();
        startAgent(agentConfig);

        //verify
        assertEquals(1, MockModulesSettingsServerResource.GET_SETTINGS_COUNT);
        assertEquals(1, MockFirewallSettingsServerResource.GET_SETTINGS_COUNT);

        //call api
        Response response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        //second api call
        response = callAgent("/test", VALID_USERNAME, VALID_PASSWORD);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
    }

    private void startAgent(AgentConfig agentConfig) throws Exception {
        this.agentComponent = new Component();
        this.agentComponent.setName("agent");
        this.agentComponent.getServers().add(Protocol.HTTP, AGENT_PORT);
        AgentService agentService = new AgentService();
        agentService.setCellId(agentConfig.getCellId());
        agentService.setCellVersion(agentConfig.getCellVersion());
        agentService.setAgentServicePath(agentConfig.getAgentServicePath());
        agentService.setAgentUsername(agentConfig.getAgentUsername());
        agentService.setAgentSecretKey(agentConfig.getAgentSecretKey());
        agentComponent.getServices().add(agentService);

        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                Router router = new Router();
                router.attach("/test", AgentServerResource.class);
                router.attach("/admin/test", AgentServerResource.class);
                return router;
            }
        };

        this.agentComponent.getDefaultHost().attach(application);
        this.agentComponent.start();
    }

    private Response callAgent(String path) throws Exception {
        return callAgent(path, null, null);
    }

    private Response callAgent(String path, String username, String password) throws Exception {
        Request request = new Request(Method.GET, AGENT_URL + path);

        if (username != null) {
            //add authentication scheme
            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password));
        }

        Client c = new Client(Protocol.HTTP);
        try {
            return c.handle(request);
        } finally {
            c.stop();
        }
    }

    public static class AgentServerResource extends ServerResource {

        public Request getRequest;

        @Get
        public void getCalled() {
            logger.info("agent get method called " + getRequest());
            getRequest = getRequest();
        }
    }


    public static class MockModulesSettingsServerResource extends ServerResource implements ModulesSettingsResource {
        public static int GET_SETTINGS_COUNT = 0;
        public static ModulesSettings MODULES_SETTINGS;

        @Override
        public ModulesSettings getSettings() {
            GET_SETTINGS_COUNT++;
            return MODULES_SETTINGS;
        }
    }

    public static class MockAuthenticationAuthenticateServerResource extends ServerResource implements AuthenticationAuthenticateResource {
        public static int AUTHENTICATE_COUNT = 0;

        @Override
        public User authenticate(Credentials credentials) {
            AUTHENTICATE_COUNT++;
            if (VALID_USERNAME.equals(credentials.getUsername()) && VALID_PASSWORD.equals(new String(credentials.getPassword()))) {
                User user = new User();
                user.setUsername(VALID_USERNAME);
                user.setGroups(Arrays.asList("user", "dev"));
                return user;
            }
            if (SERVER_ERROR_USERNAME.equals(credentials.getUsername())) {
                throw new RuntimeException("Error username cause an exception");
            }
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
    }

    public static class MockAuthorizationOperationsServerResource extends ServerResource implements AuthorizationOperationsResource {
        public static int AUTHORIZATIONS_COUNT = 0;

        @Override
        public OperationsAuthorization getAuthorizations() {
            AUTHORIZATIONS_COUNT++;
            return new OperationsAuthorization
                    (Arrays.asList(
                    new OperationAuthorization(Method.GET.getName(), "/test", Arrays.asList("user", "admin")),
                    new OperationAuthorization(Method.GET.getName(), "/admin/test", Arrays.asList("admin"))
                    )
            );
        }
    }

    public static class MockFirewallSettingsServerResource extends ServerResource implements FirewallSettingsResource {
        public static int GET_SETTINGS_COUNT = 0;
        public static FirewallSettings FIREWALL_SETTINGS;


        @Override
        public FirewallSettings getSettings() {
            GET_SETTINGS_COUNT++;
            return FIREWALL_SETTINGS;
        }
    }
}
