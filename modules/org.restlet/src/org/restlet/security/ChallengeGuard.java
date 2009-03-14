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

package org.restlet.security;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;

/**
 * Guard that controls that incoming requests are properly authenticated against
 * a given challenge scheme. The features provided by this class are similar to
 * the now deprecated {@link org.restlet.Guard} class.
 * 
 * @author Jerome Louvel
 */
public class ChallengeGuard extends Guard {

    /**
     * Constructor which creates a {@link ChallengeAuthenticator} for the given
     * {@link ChallengeScheme} and uses an {@link Authorizer} which accepts
     * everything.
     * 
     * @param context
     *            The current context.
     * @param scheme
     *            The challenge scheme.
     * @param realm
     *            The authentication realm.
     */
    public ChallengeGuard(Context context, ChallengeScheme scheme, String realm) {
        this(context, scheme, Authorizer.ALWAYS, realm);
    }

    /**
     * Constructor which creates a {@link ChallengeAuthenticator} for the given
     * {@link ChallengeScheme} and uses a given {@link Authorizer}.
     * 
     * @param context
     *            The current context.
     * @param scheme
     *            The challenge scheme.
     * @param authorizer
     *            The custom authorizer.
     * @param realm
     *            The authentication realm.
     */
    public ChallengeGuard(Context context, ChallengeScheme scheme,
            Authorizer authorizer, String realm) {
        super(context, new ChallengeAuthenticator(context, scheme, realm),
                authorizer);
    }

    @Override
    public ChallengeAuthenticator getAuthenticator() {
        return (ChallengeAuthenticator) super.getAuthenticator();
    }

}
