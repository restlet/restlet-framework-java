package org.restlet.ext.apispark.internal.agent.resource;

import org.restlet.ext.apispark.internal.agent.bean.Credentials;
import org.restlet.ext.apispark.internal.agent.bean.User;
import org.restlet.resource.Post;

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
