package org.restlet.example.book.restlet.ch9;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.example.book.restlet.ch9.dao.DAOFactory;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.example.book.restlet.ch9.objects.User;

/**
 * Guard access to the current mailbox.
 * 
 */
public class RmepMailboxGuard extends RmepGuard {

    public RmepMailboxGuard(Context context, ChallengeScheme scheme,
            String realm, DAOFactory daoFactory) {
        super(context, scheme, realm, daoFactory);
    }

    /**
     * Check that the current user is the mailbox's owner
     */
    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        User currentUser = (User) request.getAttributes().get(CURRENT_USER);
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        Mailbox mailbox = daoFactory.getMailboxDAO().getMailboxById(mailboxId);

        return currentUser.getId().equals(mailbox.getOwner().getId());
    }

}
