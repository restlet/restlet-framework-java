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

package org.restlet.example.book.restlet.ch8;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.example.book.restlet.ch8.objects.ObjectsFacade;
import org.restlet.example.book.restlet.ch8.objects.User;

/**
 * Guard access to the RMEP application.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 */
public class RmepGuard extends Guard {

    /** Data facade object. */
    protected ObjectsFacade dataFacade;

    /** Storage key in request's context. */
    public final static String CURRENT_USER = "CURRENT_USER";

    public RmepGuard(Context context, ChallengeScheme scheme, String realm,
            ObjectsFacade dataFacade) {
        super(context, scheme, realm);
        this.dataFacade = dataFacade;
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        final User user = this.dataFacade.getUserByLoginPwd(identifier, secret);
        if (user != null) {
            request.getAttributes().put(CURRENT_USER, user);
            return true;
        }

        return false;
    }

}
