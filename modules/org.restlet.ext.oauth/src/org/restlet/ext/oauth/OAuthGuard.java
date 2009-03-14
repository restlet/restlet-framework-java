/**
 * Copyright 2005-2009 Noelios Technologies.
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

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.SimpleOAuthValidator;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;

/**
 * Handles authentication using
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://oauth.net">OAuth</a>
 * @author Adam Rosien
 */
public class OAuthGuard extends Guard {

    private volatile OAuthProvider provider;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param realm
     *            The authentication realm.
     * @param provider
     *            The OAuth provider.
     * @throws IllegalArgumentException
     *             if the scheme is null
     */
    public OAuthGuard(Context context, String realm, OAuthProvider provider) {
        super(context, ChallengeScheme.HTTP_OAUTH, realm);
        this.provider = provider;
    }

    @Override
    public int authenticate(Request request) {
        final OAuthMessage requestMessage = OAuthHelper.getMessage(request);
        final OAuthAccessor accessor = this.provider
                .getAccessor(requestMessage);

        if (accessor == null) {
            return Guard.AUTHENTICATION_MISSING;
        }

        try {
            requestMessage
                    .validateMessage(accessor, new SimpleOAuthValidator());
        } catch (Exception e1) {
            return Guard.AUTHENTICATION_INVALID;
        }

        return Boolean.TRUE.equals(accessor.getProperty("authorized")) ? Guard.AUTHENTICATION_VALID
                : Guard.AUTHENTICATION_INVALID;
    }

}
