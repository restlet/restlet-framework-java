package org.restlet.example.book.restlet.ch9;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.example.book.restlet.ch9.dao.DAOFactory;
import org.restlet.example.book.restlet.ch9.objects.User;

/**
 * Guard access to the RMEP application.
 * 
 */
public class RmepGuard extends Guard {

    /** DAO objects factory. */
    protected DAOFactory daoFactory;

    /** Storage key in request's context. */
    public final static String CURRENT_USER = "CURRENT_USER";

    public RmepGuard(Context context, ChallengeScheme scheme, String realm,
            DAOFactory daoFactory) {
        super(context, scheme, realm);
        this.daoFactory = daoFactory;
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        User user = daoFactory.getUserDAO().getUserByLoginPwd(identifier,
                secret);
        if (user != null) {
            request.getAttributes().put(CURRENT_USER, user);
            return true;
        }

        return false;
    }

}
