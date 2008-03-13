/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.oauth;

import java.util.logging.Logger;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;

/**
 * Handles authentication using
 * 
 * @see <a href="http://oauth.net">OAuth</a>
 * 
 * @author Adam Rosien
 */
public class OAuthGuard extends Guard {
    /**
     * ChallengeScheme for OAuth.
     */
    public static final ChallengeScheme SCHEME = new ChallengeScheme(
            "HTTP_OAuth", "OAuth");

    static final Logger logger = Logger.getLogger(OAuthGuard.class.getName());

    private OAuthProvider provider;

    /**
     * Constructor.
     * 
     * @param context
     * @param realm
     * @param provider
     */
    public OAuthGuard(Context context, String realm, OAuthProvider provider) {
        super(context, SCHEME, realm);
        this.provider = provider;
    }

    @Override
    public int authenticate(Request request) {
        OAuthMessage requestMessage = OAuthHelper.getMessage(request);
        OAuthAccessor accessor = provider.getAccessor(requestMessage);

        if (accessor == null) {
            return Guard.AUTHENTICATION_MISSING;
        }

        try {
            requestMessage.validateSignature(accessor);
        } catch (Exception e1) {
            return Guard.AUTHENTICATION_INVALID;
        }

        return Boolean.TRUE.equals(accessor.getProperty("authorized")) ? Guard.AUTHENTICATION_VALID
                : Guard.AUTHENTICATION_INVALID;
    }

}
