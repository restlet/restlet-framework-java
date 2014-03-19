/**
 * Copyright 2005-2014 Restlet S.A.S.
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth;

import java.util.Date;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.Header;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.oauth.internal.CryptoUtils;
import org.restlet.util.Series;

/**
 * Implementation of the client-side OAuth2 support. If this helper is not
 * automatically added to your Engine add it with:
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
 * @author Thierry Templier
 */
public class HttpOAuthMacHelper extends AuthenticatorHelper {
    /**
     * Constructor. Use the {@link ChallengeScheme#HTTP_OAUTH_MAC} challenge
     * scheme.
     */
    public HttpOAuthMacHelper() {
        super(ChallengeScheme.HTTP_OAUTH_MAC, true, true);
    }

    public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {
        cw.append("id=\"");
        cw.append(challenge.getIdentifier());
        cw.append("\",ts=\"");
        cw.append((new Date()).getTime());
        cw.append("\",nonce=\"");
        String nonce = CryptoUtils.makeNonce(String.valueOf(challenge
                .getSecret()));
        cw.append(nonce);
        cw.append("\",mac=\"");
        cw.append(String.valueOf(challenge.getSecret()));
        cw.append("\"");
    }
}
