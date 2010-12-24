/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.oauth.util;

import java.io.IOException;
import java.util.StringTokenizer;

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
 * added to your Engine add it
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
 * @author Kristoffer Gronowski
 */
public class OAuthHelper extends AuthenticatorHelper {

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
