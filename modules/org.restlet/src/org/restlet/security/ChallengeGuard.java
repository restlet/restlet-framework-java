/**
 * Copyright 2005-2009 Noelios Technologies.
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
     */
    public ChallengeGuard(Context context, ChallengeScheme scheme) {
        this(context, scheme, new AllAuthorizer());
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
     */
    public ChallengeGuard(Context context, ChallengeScheme scheme,
            Authorizer authorizer) {
        super(context, new ChallengeAuthenticator(scheme), authorizer);
    }

    @Override
    public ChallengeAuthenticator getAuthenticator() {
        return (ChallengeAuthenticator) super.getAuthenticator();
    }

}
