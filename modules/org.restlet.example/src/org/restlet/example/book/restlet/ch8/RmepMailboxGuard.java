package org.restlet.example.book.restlet.ch8;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.example.book.restlet.ch8.data.DataFacade;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.User;

/**
 * Guard access to the current mailbox.
 * 
 */
public class RmepMailboxGuard extends RmepGuard {

    public RmepMailboxGuard(Context context, ChallengeScheme scheme,
            String realm, DataFacade dataFacade) {
        super(context, scheme, realm, dataFacade);
    }

    /**
     * Check that the current user is the mailbox's owner
     */
    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        User currentUser = (User) request.getAttributes().get(CURRENT_USER);
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        Mailbox mailbox = dataFacade.getMailboxById(mailboxId);

        // TODO to be updated.
        return true || currentUser.getId().equals(mailbox.getOwner().getId());
    }

}
