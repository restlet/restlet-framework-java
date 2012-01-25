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

package org.restlet.ext.oauth;

import java.io.IOException;
import java.util.StringTokenizer;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.Header;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.util.Series;

/**
 * Implementation of OAuth2 Authentication. If this helper is not automatically
 * added to your Engine add it with:
 * 
 * <pre>
 * {
 *     &#064;code
 *     List&lt;AuthenticatorHelper&gt; authenticators = Engine.getInstance()
 *             .getRegisteredAuthenticators();
 *     authenticators.add(new OAuthAuthenticationHelper());
 * }
 * </pre>
 * 
 * Here is the list of parameters that are supported. They should be set
 * before an OAuth2Server or Client is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>authPage</td>
 * <td>String</td>
 * <td>auth_page</td>
 * <td>Specifies where an AuthorizationServerResource should redirect
 * authorization requests for user interaction. This resource will be accessed
 * using riap, i.e. riap://application/+authPage</td>
 * </tr>
 * <tr>
 * <td>authPageTemplate</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Specifies an html file or freemarker template for a GUI. If none is
 * provided Roles (scopes) will automatically granted. Accessed using clap, i.e.
 * clap:///+authPageTemplate</td>
 * </tr>
 * <td>authPageTemplate</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Specifies an html file or freemarker template for a GUI. If none is
 * provided Roles (scopes) will automatically granted. Used by
 * AuthPageServerResource</td>
 * </tr>
 * <tr>
 * <td>authSkipApproved</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true no authorization page will be shown if the Roles (scopes) have
 * been previously approved by the user</td>
 * </tr>
 * <tr>
 * <td>loginPage</td>
 * <td>String</td>
 * <td>login</td>
 * <td>Specifing a login resource location relative to the Application root.
 * Defaults to "login". This resource will be accessed using riap, i.e.
 * riap://application/+loginPage</td>
 * </tr>
 * 
 * </td>
 * </table>
 * 
 * @author Kristoffer Gronowski
 */
public class HttpOAuthHelper extends AuthenticatorHelper {
    /**
     * Returns the value of the "authPage" parameter.
     * 
     * @param c
     *            The context where to find the parameter.
     * @return The value of the "authPage" parameter.
     */
    public static String getAuthPage(Context c) {
        return c.getParameters().getFirstValue("authPage", "/auth_page");
    }

    /**
     * Returns the value of the "authPageTemplate" parameter.
     * 
     * @param c
     *            The context where to find the parameter.
     * @return The value of the "authPageTemplate" parameter.
     */
    public static String getAuthPageTemplate(Context c) {
        return c.getParameters().getFirstValue("authPageTemplate");
    }

    /**
     * Returns the value of the "authSkipApproved" parameter.
     * 
     * @param c
     *            The context where to find the parameter.
     * @return The value of the "authSkipApproved" parameter.
     */
    public static boolean getAuthSkipApproved(Context c) {
        c.getLogger().fine("Trying to get auth page template");
        String skip = c.getParameters().getFirstValue("authSkipApproved");
        if (skip == null)
            return false;
        return Boolean.parseBoolean(skip);
    }

    /**
     * Sets the value of the "authpage" parameter.
     * 
     * @param authPage
     *            The value of the "authpage" parameter.
     * @param c
     *            The context to update.
     */
    public static void setAuthPage(String authPage, Context c) {
        c.getParameters().set("authPage", authPage);
    }

    /**
     * Sets the value of the "authPageTemplate" parameter.
     * 
     * @param authPageTemplate
     *            The value of the "authPageTemplate" parameter.
     * @param c
     *            The context to update.
     */
    public static void setAuthPageTemplate(String authPageTemplate, Context c) {
        c.getParameters().set("authPageTemplate", authPageTemplate);
    }

    /**
     * Sets the value of the "authSkipApproved" parameter.
     * 
     * @param skip
     *            The value of the "authSkipApproved" parameter.
     * @param c
     *            The context to update.
     */
    public static void setAuthSkipApproved(boolean skip, Context c) {
        c.getParameters().set("authSkipApproved", Boolean.toString(skip));
    }

    /**
     * Constructor. Use the {@link ChallengeScheme#HTTP_OAUTH} authentication
     * scheme.
     */
    public HttpOAuthHelper() {
        super(ChallengeScheme.HTTP_OAUTH, true, true);
    }

    @Override
    public void formatRequest(ChallengeWriter cw,
            ChallengeRequest challenge, Response response,
            Series<Header> httpHeaders) throws IOException {
        // Format the parameters WWW-Authenticate: OAuth realm='Example
        // Service', error='expired-token'
        cw.append("realm='");
        cw.append(challenge.getRealm());
        cw.append("'");

        for (Parameter p : challenge.getParameters()) {
            cw.append(", ");
            cw.append(p.getName());
            cw.append("='");
            cw.append(p.getValue());
            cw.append("'");
        }
    }

    @Override
    public void parseRequest(ChallengeRequest challenge, Response response,
            Series<Header> httpHeaders) {
        String raw = challenge.getRawValue();

        if (raw != null && raw.length() > 0) {
            StringTokenizer st = new StringTokenizer(raw, ",");
            String realm = st.nextToken();

            if (realm != null && realm.length() > 0) {
                int eq = realm.indexOf('=');

                if (eq > 0) {
                    String value = realm.substring(eq + 1).trim();
                    // Remove the quotes, first and last after trim...
                    challenge.setRealm(value.substring(1, value.length() - 1));
                }
            }

            Series<Parameter> params = new Form();

            while (st.hasMoreTokens()) {
                String param = st.nextToken();

                if (param != null && param.length() > 0) {
                    int eq = param.indexOf('=');

                    if (eq > 0) {
                        String name = param.substring(0, eq).trim();
                        String value = param.substring(eq + 1).trim();
                        // Remove the quotes, first and last after trim...
                        params.add(name, value.substring(1, value.length() - 1));
                    }
                }
            }

            challenge.setParameters(params);
        }
    }

}
