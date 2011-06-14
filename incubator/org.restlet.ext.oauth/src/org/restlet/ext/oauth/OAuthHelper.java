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
 * Here is the list of parameters that are supported. They should be set in the
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
public class OAuthHelper extends AuthenticatorHelper {

    public static String getAuthPage(Context c) {
        return c.getParameters().getFirstValue("authPage", "auth_page");
    }

    public static void setAuthPage(String authPage, Context c) {
        c.getParameters().set("authPage", authPage);
    }

    public static String getAuthPageTemplate(Context c) {
        return c.getParameters().getFirstValue("authPageTemplate");
    }

    public static void setAuthPageTemplate(String authPageTemplate, Context c) {
        c.getParameters().set("authPageTemplate", authPageTemplate);
    }

    public static boolean getAuthSkipApproved(Context c) {
        c.getLogger().info("Trying to get auth page templat");
        String skip = c.getParameters().getFirstValue("authSkipApproved");
        if (skip == null)
            return false;
        return Boolean.parseBoolean(skip);
    }

    public static void setAuthSkipApproved(boolean skip, Context c) {
        c.getParameters().set("authSkipApproved", Boolean.toString(skip));
    }

    public static String getLoginPage(Context c) {
        return c.getParameters().getFirstValue("loginPage", "login");
    }

    public static void setLoginPage(String loginPage, Context c) {
        c.getParameters().set("loginPage", loginPage);
    }

    public OAuthHelper() {
        super(ChallengeScheme.HTTP_OAUTH, true, true);
    }

    @Override
    public void formatRawRequest(ChallengeWriter cw,
            ChallengeRequest challenge, Response response,
            Series<Parameter> httpHeaders) throws IOException {
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
            Series<Parameter> httpHeaders) {
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
