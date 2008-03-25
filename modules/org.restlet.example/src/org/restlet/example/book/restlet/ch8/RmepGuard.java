package org.restlet.example.book.restlet.ch8;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.example.book.restlet.ch8.objects.Facade;
import org.restlet.example.book.restlet.ch8.objects.User;

/**
 * Guard access to the RMEP application.
 * 
 */
public class RmepGuard extends Guard {

    /** Data facade object. */
    protected Facade dataFacade;

    /** Storage key in request's context. */
    public final static String CURRENT_USER = "CURRENT_USER";

    public RmepGuard(Context context, ChallengeScheme scheme, String realm,
            Facade dataFacade) {
        super(context, scheme, realm);
        this.dataFacade = dataFacade;
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        User user = dataFacade.getUserByLoginPwd(identifier, secret);
        if (user != null) {
            request.getAttributes().put(CURRENT_USER, user);
            return true;
        }

        return false;
    }

}
