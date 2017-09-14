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

package org.restlet.ext.oauth;

import java.io.IOException;
import java.util.StringTokenizer;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.StringUtils;
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
 * Here is the list of supported parameters. They should be set before starting an OAuth2 server or client:
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
 * <td>/auth_page</td>
 * <td>Specifies the path of the resource an {@link AuthorizationServerResource} should redirect authorization requests
 * for user interaction. This resource will be accessed using internal protocol {@link Protocol#RIAP} (i.e.
 * riap://application/authPage)</td>
 * </tr>
 * <tr>
 * <td>authPageTemplate</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Specifies the name of the authorization page. The name is used by {@link AuthPageServerResource#getPage(String)}
 * method in order to generate the HTML representation of the authorization page. If no scope is provided, the scope
 * "Roles" will be automatically granted.</td>
 * </tr>
 * <tr>
 * <td>authSkipApproved</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true no authorization page will be shown if the Roles (scopes) have been previously approved by the user</td>
 * </tr>
 * <tr>
 * <td>errorPageTemplate</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Specifies the name of the error page. The name is used by
 * {@link AuthorizationBaseServerResource#getErrorPage(String, OAuthException)} method in order to generate the HTML
 * representation of the error page. If no scope is provided, the scope "Roles" will be automatically granted.</td>
 * </tr>
 * <tr>
 * <td>loginPage</td>
 * <td>String</td>
 * <td>/login</td>
 * <td>Specifing a login resource location relative to the Application root. This resource will be accessed using
 * internal protocol {@link Protocol#RIAP} (i.e. riap://application/login)</td>
 * </tr>
 * </table>
 * 
 * @author Kristoffer Gronowski
 */
public class HttpOAuthHelper extends AuthenticatorHelper {
    /**
     * Returns the value of the "authPage" parameter.
     * 
     * @param context
     *            The context where to find the parameter.
     * @return The value of the "authPage" parameter.
     */
    public static String getAuthPage(Context context) {
        return context.getParameters().getFirstValue("authPage", "/auth_page");
    }

    /**
     * Returns the value of the "authPageTemplate" parameter.
     * 
     * @param context
     *            The context where to find the parameter.
     * @return The value of the "authPageTemplate" parameter.
     */
    public static String getAuthPageTemplate(Context context) {
        return context.getParameters().getFirstValue("authPageTemplate");
    }

    /**
     * Returns the value of the "authSkipApproved" parameter.
     * 
     * @param context
     *            The context where to find the parameter.
     * @return The value of the "authSkipApproved" parameter.
     */
    public static boolean getAuthSkipApproved(Context context) {
        context.getLogger().fine("Trying to get auth page template");
        String skip = context.getParameters().getFirstValue("authSkipApproved");
        return Boolean.parseBoolean(skip);
    }

    
    /**
     * Returns the value of the "errorPageTemplate" parameter.
     * 
     * @param context
     *            The context where to find the parameter.
     * @return The value of the "errorPageTemplate" parameter.
     */
    public static String getErrorPageTemplate(Context context) {
        return context.getParameters().getFirstValue("errorPageTemplate");
    }

    /**
     * Returns the value of the "loginPage" parameter.
     * 
     * @param context
     *            The context where to find the parameter.
     * @return The value of the "loginPage" parameter.
     */
    public static String getLoginPage(Context context) {
        return context.getParameters().getFirstValue("login", "/login");
    }

    /**
     * Sets the value of the "authPage" parameter.
     * 
     * @param authPage
     *            The value of the "authPage" parameter.
     * @param context
     *            The context to update.
     */
    public static void setAuthPage(String authPage, Context context) {
        context.getParameters().set("authPage", authPage);
    }

    /**
     * Sets the value of the "authPageTemplate" parameter.
     * 
     * @param authPageTemplate
     *            The value of the "authPageTemplate" parameter.
     * @param context
     *            The context to update.
     */
    public static void setAuthPageTemplate(String authPageTemplate, Context context) {
        context.getParameters().set("authPageTemplate", authPageTemplate);
    }

    /**
     * Sets the value of the "authSkipApproved" parameter.
     * 
     * @param skip
     *            The value of the "authSkipApproved" parameter.
     * @param context
     *            The context to update.
     */
    public static void setAuthSkipApproved(boolean skip, Context context) {
        context.getParameters().set("authSkipApproved", Boolean.toString(skip));
    }

    /**
     * Sets the value of the "errorPageTemplate" parameter.
     * 
     * @param errorPageTemplate
     * @param context
     */
    public static void setErrorPageTemplate(String errorPageTemplate, Context context) {
        context.getParameters().set("errorPageTemplate", errorPageTemplate);
    }

    /**
     * Sets the value of the "loginPage" parameter.
     * 
     * @param loginPage
     *            The value of the "loginPage" parameter.
     * @param context
     *            The context to update.
     */
    public static void setLoginPage(String loginPage, Context context) {
        context.getParameters().set("loginPage", loginPage);
    }

    /**
     * Constructor. Use the {@link ChallengeScheme#HTTP_OAUTH} authentication
     * scheme.
     */
    public HttpOAuthHelper() {
        super(ChallengeScheme.HTTP_OAUTH, true, true);
    }

    @Override
    public void formatRequest(ChallengeWriter cw, ChallengeRequest challenge,
            Response response, Series<Header> httpHeaders) throws IOException {
        // Format the parameters WWW-Authenticate: OAuth realm='Example Service', error='expired-token'
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
    public void parseRequest(ChallengeRequest challenge, Response response, Series<Header> httpHeaders) {
        String raw = challenge.getRawValue();

        if (raw != null && raw.length() > 0) {
            StringTokenizer st = new StringTokenizer(raw, ",");
            String realm = st.nextToken();

            if (!StringUtils.isNullOrEmpty(realm)) {
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

                if (!StringUtils.isNullOrEmpty(param)) {
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
