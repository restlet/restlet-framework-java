/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.util.List;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Route;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.Parameter;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;

/**
 * JUnit test case for the newly added (Component.xsd version 1.2) parameter
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
 *     One of '{&quot;http://www.restlet.org/schemas/1.2/Component&quot;:client, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:server, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:parameter, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:defaultHost, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:host, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:internalRouter, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:logService, \
 *     &quot;http://www.restlet.org/schemas/1.2/Component&quot;:statusService}' is expected.
 * </pre>
 */
public class ComponentXmlConfigTestCase extends TestCase {
    private static final int PORT1 = RestletTestSuite.PORT;

    private static final int PORT2 = PORT1 + 1;

    private static final String C_NAME = "componentParamName";

    private static final String C_VALUE = "componentParamValue";

    private static final String CON_NAME = "connectorParamName";

    private static final String CON_VALUE = "connectorParamValue";

    private static final String A_NAME = "attachParamName";

    private static final String A_VALUE = "attachParamValue";

    private static final String R_NAME = "routerParamName";

    private static final String R_VALUE = "routerParamValue";

    // strings used in output to better identify the context of the check
    private static final String COMPONENT = "COMPONENT";

    private static final String CLIENT = "CLIENT";

    private static final String SERVER = "SERVER";

    private static final String HOST = "HOST";

    private static final String ATTACH = "ATTACH";

    /** Correct restlet.xml test instances. */
    private static final String RESTLET_XML = "<?xml version=\"1.0\"?>\n"
            + "<component xmlns=\"http://www.restlet.org/schemas/1.2/Component\">\n"
            + "<client protocol=\"HTTP\">\n" + "<parameter name=\""
            + CON_NAME
            + "3\" value=\""
            + CON_VALUE
            + "3\"/>\n"
            + "<parameter name=\""
            + CON_NAME
            + "4\" value=\""
            + CON_VALUE
            + "4\"/>\n"
            + "</client>\n"
            + "<illegal-element name=\""
            + C_NAME
            + "1\" value=\""
            + C_VALUE
            + "1\"/>\n"
            + "<server protocol=\"HTTP\" port=\""
            + PORT1
            + "\">\n"
            + "<parameter name=\""
            + CON_NAME
            + "1\" value=\""
            + CON_VALUE
            + "1\"/>\n"
            + "<parameter name=\""
            + CON_NAME
            + "2\" value=\""
            + CON_VALUE
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
            + "<defaultHost hostPort=\""
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
            + "<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.HelloWorldApplication\">\n"
            + "<parameter name=\""
            + A_NAME
            + "1\" value=\""
            + A_VALUE
            + "1\"/>\n"
            + "<parameter name=\""
            + A_NAME
            + "2\" value=\""
            + A_VALUE
            + "2\"/>\n"
            + "</attach>\n"
            + "<attach uriPattern=\"/foo\" targetClass=\"org.restlet.test.HelloWorldApplication\">\n"
            + "<parameter name=\""
            + A_NAME
            + "3\" value=\""
            + A_VALUE
            + "3\"/>\n"
            + "<parameter name=\""
            + A_NAME
            + "4\" value=\""
            + A_VALUE
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
            + "<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.HelloWorldApplication\">\n"
            + "<parameter name=\""
            + A_NAME
            + "5\" value=\""
            + A_VALUE
            + "5\"/>\n"
            + "<parameter name=\""
            + A_NAME
            + "6\" value=\""
            + A_VALUE
            + "6\"/>\n"
            + "</attach>\n"
            + "</host>\n"
            + "</component>";

    private Component c;

    // default 0-arguments constructor

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Representation xml = new StringRepresentation(RESTLET_XML);
        c = new Component(xml);
        assertNotNull("Component (parsed) MUST NOT be null", c);
    }

    public void testComponentParams() throws Exception {
        System.out.println("-- testComponentParams()");

        final String msg = "[" + COMPONENT + "] ";
        final Context ctx = c.getContext();

        checkPositiveParam(msg, ctx, C_NAME + "1", C_VALUE + "1");
        checkPositiveParam(msg, ctx, C_NAME + "2", C_VALUE + "2");

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testClientParams() throws Exception {
        System.out.println("-- testClientParams()");

        ClientList clients = c.getClients();
        assertNotNull("Client list MUST NOT be null", clients);
        assertEquals("Client list MUST contain 1 item", 1, clients.size());
        Client client = clients.get(0);
        assertNotNull("The single Client MUST NOT be null", client);

        final String msg = "[" + CLIENT + "] ";
        final Context ctx = client.getContext();

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkPositiveParam(msg, ctx, CON_NAME + "3", CON_VALUE + "3");
        checkPositiveParam(msg, ctx, CON_NAME + "4", CON_VALUE + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testServerParams() throws Exception {
        System.out.println("-- testServerParams()");

        ServerList servers = c.getServers();
        assertNotNull("Server list MUST NOT be null", servers);
        assertEquals("Server list MUST contain 1 item", 1, servers.size());
        Server server = servers.get(0);
        assertNotNull("The single Server MUST NOT be null", server);

        final String msg = "[" + SERVER + "] ";
        final Context ctx = server.getContext();

        checkPositiveParam(msg, ctx, CON_NAME + "1", CON_VALUE + "1");
        checkPositiveParam(msg, ctx, CON_NAME + "2", CON_VALUE + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testDefaultHostParams() throws Exception {
        System.out.println("-- testDefaultHostParams()");

        final VirtualHost dh = c.getDefaultHost();
        assertNotNull("Default Host MUST NOT be null", dh);
        final String msg = "[" + HOST + ":" + dh.getHostPort() + "] ";
        final Context ctx = dh.getContext();
        System.out.println("*** Context parameters = " + ctx.getParameters());

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkPositiveParam(msg, ctx, R_NAME + "1", R_VALUE + "1");
        checkPositiveParam(msg, ctx, R_NAME + "2", R_VALUE + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testHostParams() throws Exception {
        System.out.println("-- testHostParams()");

        final List<VirtualHost> hosts = c.getHosts();
        assertNotNull("Host List MUST NOT be null", hosts);
        assertEquals("Server list MUST contain 1 item", 1, hosts.size());
        VirtualHost host = hosts.get(0);
        assertNotNull("The single Host MUST NOT be null", host);

        final String msg = "[" + HOST + ":" + host.getHostPort() + "] ";
        final Context ctx = host.getContext();

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkPositiveParam(msg, ctx, R_NAME + "3", R_VALUE + "3");
        checkPositiveParam(msg, ctx, R_NAME + "4", R_VALUE + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testAttachParams1a() throws Exception {
        System.out.println("-- testAttachParams1a()");

        final Route route = c.getDefaultHost().getRoutes().get(0);
        assertNotNull(
                "The first Attach element of the Default Host MUST NOT be null",
                route);
        final String msg = "[" + ATTACH + " #1] ";
        Context ctx = route.getContext();

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkPositiveParam(msg, ctx, A_NAME + "1", A_VALUE + "1");
        checkPositiveParam(msg, ctx, A_NAME + "2", A_VALUE + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testAttachParams1b() throws Exception {
        System.out.println("-- testAttachParams1b()");

        final Route route = c.getDefaultHost().getRoutes().get(1);
        assertNotNull(
                "The second Attach element of the Default Host MUST NOT be null",
                route);
        final String msg = "[" + ATTACH + " #2] ";
        Context ctx = route.getContext();

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkPositiveParam(msg, ctx, A_NAME + "3", A_VALUE + "3");
        checkPositiveParam(msg, ctx, A_NAME + "4", A_VALUE + "4");
        checkNegativeParam(msg, ctx, A_NAME + "5");
        checkNegativeParam(msg, ctx, A_NAME + "6");
    }

    public void testAttachParams2() throws Exception {
        System.out.println("-- testAttachParams2()");

        final Route route = c.getHosts().get(0).getRoutes().get(0);
        assertNotNull(
                "The single Attach element of the Single Host MUST NOT be null",
                route);
        final String msg = "[" + ATTACH + "] ";
        final Context ctx = route.getContext();

        checkNegativeParam(msg, ctx, CON_NAME + "1");
        checkNegativeParam(msg, ctx, CON_NAME + "2");
        checkNegativeParam(msg, ctx, CON_NAME + "3");
        checkNegativeParam(msg, ctx, CON_NAME + "4");

        checkNegativeParam(msg, ctx, C_NAME + "1");
        checkNegativeParam(msg, ctx, C_NAME + "2");

        checkNegativeParam(msg, ctx, R_NAME + "1");
        checkNegativeParam(msg, ctx, R_NAME + "2");
        checkNegativeParam(msg, ctx, R_NAME + "3");
        checkNegativeParam(msg, ctx, R_NAME + "4");

        checkNegativeParam(msg, ctx, A_NAME + "1");
        checkNegativeParam(msg, ctx, A_NAME + "2");
        checkNegativeParam(msg, ctx, A_NAME + "3");
        checkNegativeParam(msg, ctx, A_NAME + "4");
        checkPositiveParam(msg, ctx, A_NAME + "5", A_VALUE + "5");
        checkPositiveParam(msg, ctx, A_NAME + "6", A_VALUE + "6");
    }

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
        final Parameter p = ctx.getParameters().getFirst(n);
        assertNotNull(msg + "Parameter '" + n + "' MUST NOT be null", p);
        final String pValue = p.getValue();
        assertNotNull(msg + "Parameter '" + n + "' MUST NOT have a null value",
                pValue);
        assertEquals(msg + "Parameter '" + n + "' has unexpected value", v,
                pValue);
    }

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
        final Parameter p = ctx.getParameters().getFirst(n);
        assertNull(msg + " Parameter '" + n + "' MUST be null", p);
    }
}
