/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.example.book.rest.ch7;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.example.book.rest.ch7.domain.User;
import org.restlet.example.book.rest.ch7.resource.UserResource;

/**
 * Customized guard that check passwords against the users database.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Guard extends org.restlet.Guard {

    public Guard(Context context, ChallengeScheme scheme, String realm) {
        super(context, scheme, realm);
    }

    @Override
    protected int authorize(Request request) {
        int result = 0;

        if (request.getChallengeResponse() != null) {
            String identifier = request.getChallengeResponse().getIdentifier();
            String secret = request.getChallengeResponse().getSecret();

            if ((identifier != null) && (secret != null)) {
                User user = UserResource.findUser(identifier);

                if ((user != null) && secret.equals(user.getPassword())) {
                    result = 1;
                } else {
                    result = -1;
                }
            }
        }

        return result;
    }

}
