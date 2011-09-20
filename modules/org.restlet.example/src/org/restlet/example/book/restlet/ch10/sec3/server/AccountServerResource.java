/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.example.book.restlet.ch10.sec3.server;

import java.util.List;

import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch10.sec3.api.AccountRepresentation;
import org.restlet.example.book.restlet.ch10.sec3.api.AccountResource;
import org.restlet.ext.rdf.Graph;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Implementation of a mail account resource.
 */
public class AccountServerResource extends ServerResource implements
        AccountResource {

    /** The account identifier. */
    private int accountId;

    public List<Account> getAccounts(){
        
    }
    
    /**
     * Retrieve the account identifier based on the URI path variable
     * "accountId" declared in the URI template attached to the application
     * router.
     */
    @Override
    protected void doInit() throws ResourceException {
        String accountIdAttribute = (String) getRequestAttributes().get(
                "accountId");

        if (accountIdAttribute != null) {
            this.accountId = Integer.parseInt(accountIdAttribute);
        }
    }

    public void remove() {
        AccountsServerResource.getAccounts().remove(this.accountId - 1);
    }

    public Graph representFoaf() {
        Graph result = new Graph();
        AccountRepresentation accountRep = represent();

        accountRep.get
        
        for (String contactRef : accountRep.getContactRefs()) {
            result.add(getReference(), FoafConstants.KNOWS, new Reference(
                    contactRef));
        }

        return result;
    }

    public AccountRepresentation represent() {
        return AccountsServerResource.getAccounts().get(this.accountId - 1);
    }

    public void store(AccountRepresentation account) {
        AccountsServerResource.getAccounts().set(this.accountId - 1, account);
    }
}
