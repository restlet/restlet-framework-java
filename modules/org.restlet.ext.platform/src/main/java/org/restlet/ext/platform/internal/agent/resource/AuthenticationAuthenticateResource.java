/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.platform.internal.agent.resource;

import org.restlet.ext.platform.internal.agent.bean.Credentials;
import org.restlet.ext.platform.internal.agent.bean.User;
import org.restlet.resource.Post;

/**
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public interface AuthenticationAuthenticateResource {

    /**
     * Authenticate a user from its credentials
     *
     * @param credentials
     *            The user credentials
     *
     * @return The user is the authentication succeeded <br>
     *         Status details:
     *         <ul>
     *         <li>200 if the user is authenticated</li>
     *         <li>401 if the user is not authenticated or does not exist</li>
     *         </ul>
     */
    @Post
    public User authenticate(Credentials credentials);
}
