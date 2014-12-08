/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch10.sec3.server;

import java.util.Map;

import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch10.sec3.FoafConstants;
import org.restlet.example.book.restlet.ch10.sec3.api.AccountRepresentation;
import org.restlet.example.book.restlet.ch10.sec3.api.AccountResource;
import org.restlet.example.book.restlet.ch10.sec3.model.Account;
import org.restlet.example.book.restlet.ch10.sec3.model.Contact;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Literal;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Implementation of a mail account resource.
 */
public class AccountServerResource extends ServerResource implements
        AccountResource {

    /** The account identifier. */
    private Account account;

    public Map<String, Account> getAccounts() {
        return ((MailServerApplication) getApplication()).getAccounts();
    }

    /**
     * Retrieve the account identifier based on the URI path variable
     * "accountId" declared in the URI template attached to the application
     * router.
     */
    @Override
    protected void doInit() throws ResourceException {
        String accountId = getAttribute("accountId");
        this.account = getAccounts().get(accountId);
    }

    public void remove() {
        getAccounts().remove(this.account);
    }

    /**
     * Builds the RDF graph.
     */
    public Graph getFoafProfile() {
        Graph result = null;

        if (account != null) {
            result = new Graph();
            result.add(getReference(), FoafConstants.MBOX, new Literal(
                    "mailto:" + account.getEmailAddress()));
            result.add(getReference(), FoafConstants.FIRST_NAME, new Literal(
                    account.getFirstName()));
            result.add(getReference(), FoafConstants.LAST_NAME, new Literal(
                    account.getLastName()));
            result.add(getReference(), FoafConstants.NICK,
                    new Literal(account.getNickName()));
            result.add(getReference(), FoafConstants.NAME,
                    new Literal(account.getSenderName()));

            for (Contact contact : account.getContacts()) {
                result.add(getReference(), FoafConstants.KNOWS, new Reference(
                        getReference(), contact.getProfileRef()).getTargetRef());
            }
        }

        return result;
    }

    /**
     * Builds the representation bean.
     */
    public AccountRepresentation represent() {
        AccountRepresentation result = null;

        if (account != null) {
            result = new AccountRepresentation();
            result.setEmailAddress(account.getEmailAddress());
            result.setFirstName(account.getFirstName());
            result.setLastName(account.getLastName());
            result.setLogin(account.getLogin());
            result.setNickName(account.getNickName());
            result.setSenderName(account.getSenderName());

            for (Contact contact : account.getContacts()) {
                result.getContactRefs().add(contact.getProfileRef());
            }
        }

        return result;
    }

}
