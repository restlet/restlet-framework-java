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

package org.restlet.test.component;

import java.util.List;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.VirtualHost;
import org.restlet.service.LogService;
import org.restlet.test.RestletTestCase;
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;

/**
 * JUnit test case for the newly added (Component.xsd version 2.0) parameter
 * elements in a restlet.xml configuration file.
 * <p>
 * Note that the XML data validation IS active but DOES NOT affect the outcome
 * of the configuration. In other words you can write non Schema compliant XML
 * data without risking an exception being thrown. Note also that although this
 * is possible, it is strongly discouraged. The schema validation error message
 * which is printed in the logs will look like the following --line breaks were
 * added for clarity:
 * 
 * <pre>
 * Jan 23, 2009 6:11:48 PM org.restlet.util.SaxDefaultHandler error
 * CONFIG: [ERROR] - Unexpected exception while parsing an instance of PUBLIC \
 *     [null], SYSTEM [null] - line #7, column #75: cvc-complex-type.2.4.a: \
 *     Invalid content was found starting with element 'illegal-element'. \
 *     One of '{&quot;http://www.restlet.org/schemas/2.0/Component&quot;:client, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:server, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:parameter, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:defaultHost, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:host, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:internalRouter, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:logService, \
 *     &quot;http://www.restlet.org/schemas/2.0/Component&quot;:statusService}' is expected.
 * </pre>
 */
public class ComponentXmlConfigTestCase extends RestletTestCase {
    private static final String A_PARAM_NAME = "attachParamName";

    private static final String A_PARAM_VALUE = "attachParamValue";

    private static final String ATTACH = "ATTACH";

    private static final String C_NAME = "componentParamName";

    private static final String C_VALUE = "componentParamValue";

    private static final String CLIENT = "CLIENT";

    // strings used in output to better identify the context of the check
    private static final String COMPONENT = "COMPONENT";

    private static final String CON_PARAM_NAME = "connectorParamName";

    private static final String CON_PARAM_VALUE = "connectorParamValue";

    private static final String HOST = "HOST";

    private static final boolean LOG_ENABLED = false;

    private static final String LOG_FORMAT = "logFormat";

    private static final boolean LOG_IDENTITY_CHECKED = true;

    private static final String LOGGER_NAME = "loggerName";

    private static final int PORT1 = TEST_PORT;

    private static final int PORT2 = PORT1 + 1;

    private static final String R_NAME = "routerParamName";

    private static final String R_VALUE = "routerParamValue";

    private static final String RESTLET_DESCRIPTION = "restletDescription";

    private static final String RESTLET_NAME = "restletName";

    /** Correct restlet.xml test instances. */
    private static final String RESTLET_XML = "<?xml version=\"1.0\"?>\n"
            + "<component xmlns=\"http://www.restlet.org/schemas/2.0/Component\">\n"
            + "<client protocol=\"HTTP\"" + " name=\""
            + RESTLET_NAME
            + "\""
            + " description=\""
            + RESTLET_DESCRIPTION
            + "\">\n"
            + "<parameter name=\""
            + CON_PARAM_NAME
            + "3\" value=\""
            + CON_PARAM_VALUE
            + "3\"/>\n"
            + "<parameter name=\""
            + CON_PARAM_NAME
            + "4\" value=\""
            + CON_PARAM_VALUE
            + "4\"/>\n"
            + "</client>\n"
            + "<illegal-element name=\""
            + C_NAME
            + "1\" value=\""
            + C_VALUE
            + "1\"/>\n"
            + "<server "
            + " name=\""
            + RESTLET_NAME
            + "\""
            + " description=\""
            + RESTLET_DESCRIPTION
            + "\" protocol=\"HTTP\" port=\""
            + PORT1
            + "\">\n"
            + "<parameter name=\""
            + CON_PARAM_NAME
            + "1\" value=\""
            + CON_PARAM_VALUE
            + "1\"/>\n"
            + "<parameter name=\""
            + CON_PARAM_NAME
            + "2\" value=\""
            + CON_PARAM_VALUE
            + "2\"/>\n"
            + "</server>\n"
            + "<parameter name=\""
            + C_NAME
            + "1\" value=\""
            + C_VALUE
            + "1\"/>\n"
            + "<parameter name=\""
            + C_NAME
            + "2\" value=\""
            + C_VALUE
            + "2\"/>\n"
            + "<defaultHost "
            + " name=\""
            + RESTLET_NAME
            + "\""
            + " description=\""
            + RESTLET_DESCRIPTION
            + "\" hostPort=\""
            + PORT1
            + "\">\n"
            + "<parameter name=\""
            + R_NAME
            + "1\" value=\""
            + R_VALUE
            + "1\"/>\n"
            + "<parameter name=\""
            + R_NAME
            + "2\" value=\""
            + R_VALUE
            + "2\"/>\n"
            + "<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.component.HelloWorldApplication\">\n"
            + "<parameter name=\""
            + A_PARAM_NAME
            + "1\" value=\""
            + A_PARAM_VALUE
            + "1\"/>\n"
            + "<parameter name=\""
            + A_PARAM_NAME
            + "2\" value=\""
            + A_PARAM_VALUE
            + "2\"/>\n"
            + "</attach>\n"
            + "<attach uriPattern=\"/foo\" targetClass=\"org.restlet.test.component.HelloWorldApplication\">\n"
            + "<parameter name=\""
            + A_PARAM_NAME
            + "3\" value=\""
            + A_PARAM_VALUE
            + "3\"/>\n"
            + "<parameter name=\""
            + A_PARAM_NAME
            + "4\" value=\""
            + A_PARAM_VALUE
            + "4\"/>\n"
            + "</attach>\n"
            + "</defaultHost>\n"
            + "<host hostPort=\""
            + PORT2
            + "\">\n"
            + "<parameter name=\""
            + R_NAME
            + "3\" value=\""
            + R_VALUE
            + "3\"/>\n"
            + "<parameter name=\""
            + R_NAME
            + "4\" value=\""
            + R_VALUE
            + "4\"/>\n"
            + "<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.component.HelloWorldApplication\">\n"
            + "<parameter name=\""
            + A_PARAM_NAME
            + "5\" value=\""
            + A_PARAM_VALUE
            + "5\"/>\n"
            + "<parameter name=\""
            + A_PARAM_NAME
            + "6\" value=\""
            + A_PARAM_VALUE
            + "6\"/>\n"
            + "</attach>\n"
            + "</host>\n"
            + "<logService enabled=\""
            + LOG_ENABLED
            + "\" identityCheck=\""
            + LOG_IDENTITY_CHECKED
            + "\" logFormat=\""
            + LOG_FORMAT
            + "\" loggerName=\""
            + LOGGER_NAME
            + "\" />\n"
            + "</component>";

    private static final String SERVER = "SERVER";

    private Component c;

    /**
     * Check if a designated {@code init-param} element does not exist within a
     * given {@link Context}.
     * 
     * @param msg
     *            contextual message to better describe the output.
     * @param ctx
     *            the {@link Context} within which the check is performed.
     * @param n
     *            the name of the {@code init-param} to check for.
     */
    private void checkNegativeParam(String msg, Context ctx, String n) {
        Parameter p = ctx.getParameters().getFirst(n);
        assertNull(msg + " Parameter '" + n + "' MUST be null", p);
    }

    // default 0-arguments constructor

    /**
     * Check if a designated {@code init-param} element exist within a given
     * {@link Context} with the expected name and value.
     * 
     * @param msg
     *            contextual message to better describe the output.
     * @param ctx
     *            the {@link Context} within which the check is performed.
     * @param n
     *            the name of the {@code context-param} to check for.
     * @param v
     *            the expected value of the named {@code init-param}.
     */
    private void checkPositiveParam(String msg, Context ctx, String n, String v) {
        assertNotNull(msg + "Context MUST NOT be null", ctx);
        Parameter p = ctx.getParameters().getFirst(n);
        assertNotNull(msg + "Parameter '" + n + "' MUST NOT be null", p);
        String pValue = p.getValue();
        assertNotNull(msg + "Parameter '" + n + "' MUST NOT have a null value",
                pValue);
        assertEquals(msg + "Parameter '" + n + "' has unexpected value", v,
                pValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Representation xml = new StringRepresentation(RESTLET_XML);
        c = new Component(xml);
        assertNotNull("Component (parsed) MUST NOT be null", c);
    }

    @Override
    protected void tearDown() throws Exception {
        c.stop();
        c = null;
        super.tearDown();
    }

    public void testAttachParams1a() throws Exception {
        System.out.println("-- testAttachParams1a()");

        TemplateRoute route = (TemplateRoute) c.getDefaultHost().getRoutes()
                .get(0);
        assertNotNull(
                "The first Attach element of the Default Host MUST NOT be null",
                route);
        String msg = "[" + ATTACH + " #1] ";
        Context ctx = route.getNext().getContext();

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkPositiveParam(msg, ctx, A_PARAM_NAME + "1", A_PARAM_VALUE + "1");
        checkPositiveParam(msg, ctx, A_PARAM_NAME + "2", A_PARAM_VALUE + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }

    public void testAttachParams1b() throws Exception {
        System.out.println("-- testAttachParams1b()");

        TemplateRoute route = (TemplateRoute) c.getDefaultHost().getRoutes()
                .get(1);
        assertNotNull(
                "The second Attach element of the Default Host MUST NOT be null",
                route);
        String msg = "[" + ATTACH + " #2] ";
        Context ctx = route.getNext().getContext();

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkPositiveParam(msg, ctx, A_PARAM_NAME + "3", A_PARAM_VALUE + "3");
        checkPositiveParam(msg, ctx, A_PARAM_NAME + "4", A_PARAM_VALUE + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }

    public void testAttachParams2() throws Exception {
        System.out.println("-- testAttachParams2()");

        TemplateRoute route = (TemplateRoute) c.getHosts().get(0).getRoutes()
                .get(0);
        assertNotNull(
                "The single Attach element of the Single Host MUST NOT be null",
                route);
        String msg = "[" + ATTACH + "] ";
        Context ctx = route.getNext().getContext();

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkPositiveParam(msg, ctx, A_PARAM_NAME + "5", A_PARAM_VALUE + "5");
        checkPositiveParam(msg, ctx, A_PARAM_NAME + "6", A_PARAM_VALUE + "6");
    }

    public void testClient() throws Exception {
        System.out.println("-- testClient()");
        ClientList clients = c.getClients();
        assertNotNull("Client list MUST NOT be null", clients);
        assertEquals("Client list MUST contain 1 item", 1, clients.size());
        Client client = clients.get(0);
        assertNotNull("The single Client MUST NOT be null", client);

        assertEquals(client.getName(), RESTLET_NAME);
        assertEquals(client.getDescription(), RESTLET_DESCRIPTION);
    }

    public void testClientParams() throws Exception {
        System.out.println("-- testClientParams()");

        ClientList clients = c.getClients();
        assertNotNull("Client list MUST NOT be null", clients);
        assertEquals("Client list MUST contain 1 item", 1, clients.size());
        Client client = clients.get(0);
        assertNotNull("The single Client MUST NOT be null", client);

        String msg = "[" + CLIENT + "] ";
        Context ctx = client.getContext();

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkPositiveParam(msg, ctx, CON_PARAM_NAME + "3", CON_PARAM_VALUE
                + "3");
        checkPositiveParam(msg, ctx, CON_PARAM_NAME + "4", CON_PARAM_VALUE
                + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }

    public void testComponentParams() throws Exception {
        System.out.println("-- testComponentParams()");

        String msg = "[" + COMPONENT + "] ";
        Context ctx = c.getContext();

        checkPositiveParam(msg, ctx, C_NAME + "1", C_VALUE + "1");
        checkPositiveParam(msg, ctx, C_NAME + "2", C_VALUE + "2");

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }

    public void testDefaultHost() throws Exception {
        System.out.println("-- testDefaultHost()");

        final VirtualHost dh = c.getDefaultHost();
        assertNotNull("Default Host MUST NOT be null", dh);

        assertEquals(dh.getName(), RESTLET_NAME);
        assertEquals(dh.getDescription(), RESTLET_DESCRIPTION);
    }

    public void testDefaultHostParams() throws Exception {
        System.out.println("-- testDefaultHostParams()");

        VirtualHost dh = c.getDefaultHost();
        assertNotNull("Default Host MUST NOT be null", dh);
        String msg = "[" + HOST + ":" + dh.getHostPort() + "] ";
        Context ctx = dh.getContext();

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkPositiveParam(msg, ctx, R_NAME + "1", R_VALUE + "1");
        checkPositiveParam(msg, ctx, R_NAME + "2", R_VALUE + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }

    public void testHostParams() throws Exception {
        System.out.println("-- testHostParams()");

        List<VirtualHost> hosts = c.getHosts();
        assertNotNull("Host List MUST NOT be null", hosts);
        assertEquals("Server list MUST contain 1 item", 1, hosts.size());
        VirtualHost host = hosts.get(0);
        assertNotNull("The single Host MUST NOT be null", host);

        String msg = "[" + HOST + ":" + host.getHostPort() + "] ";
        Context ctx = host.getContext();

        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkPositiveParam(msg, ctx, R_NAME + "3", R_VALUE + "3");
        checkPositiveParam(msg, ctx, R_NAME + "4", R_VALUE + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }

    public void testLogService() throws Exception {
        System.out.println("-- testLogService()");

        LogService ls = c.getLogService();
        assertNotNull("Log service MUST NOT be null", ls);

        assertEquals(LOG_ENABLED, ls.isEnabled());
        assertEquals(LOG_IDENTITY_CHECKED, ls.isIdentityCheck());
        assertEquals(LOG_FORMAT, ls.getResponseLogFormat());
        assertEquals(LOGGER_NAME, ls.getLoggerName());
    }

    public void testServer() throws Exception {
        System.out.println("-- testServerParams()");

        ServerList servers = c.getServers();
        assertNotNull("Server list MUST NOT be null", servers);
        assertEquals("Server list MUST contain 1 item", 1, servers.size());
        Server server = servers.get(0);
        assertNotNull("The single Server MUST NOT be null", server);

        assertEquals(server.getName(), RESTLET_NAME);
        assertEquals(server.getDescription(), RESTLET_DESCRIPTION);
    }

    public void testServerParams() throws Exception {
        System.out.println("-- testServerParams()");

        ServerList servers = c.getServers();
        assertNotNull("Server list MUST NOT be null", servers);
        assertEquals("Server list MUST contain 1 item", 1, servers.size());
        Server server = servers.get(0);
        assertNotNull("The single Server MUST NOT be null", server);

        String msg = "[" + SERVER + "] ";
        Context ctx = server.getContext();

        checkPositiveParam(msg, ctx, CON_PARAM_NAME + "1", CON_PARAM_VALUE
                + "1");
        checkPositiveParam(msg, ctx, CON_PARAM_NAME + "2", CON_PARAM_VALUE
                + "2");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, CON_PARAM_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_PARAM_NAME + "1");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "2");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "3");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "4");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "5");
        checkNegativeParam(msg, ctx, A_PARAM_NAME + "6");
    }
}
