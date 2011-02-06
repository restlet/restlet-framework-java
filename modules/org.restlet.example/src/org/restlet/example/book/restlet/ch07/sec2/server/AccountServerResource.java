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

package org.restlet.example.book.restlet.ch07.sec2.server;

import org.restlet.example.book.restlet.ch03.sect5.sub5.common.AccountResource;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Implementation of a mail account resource.
 */
public class AccountServerResource extends WadlServerResource implements
        AccountResource {

    /** The account identifier. */
    private int accountId;

    @Override
    protected RepresentationInfo describe(MethodInfo methodInfo,
            Class<?> representationClass, Variant variant) {
        RepresentationInfo result = new RepresentationInfo(variant);
        result.setReference("account");
        return result;
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
            setName("Resource for mail account \"" + this.accountId + "\"");
            setDescription("The resource describing the mail account number \""
                    + this.accountId + "\"");
        } else {
            setName("Mail account resource");
            setDescription("The resource describing a mail account");
        }
    }

    public void remove() {
        AccountsServerResource.getAccounts().remove(this.accountId - 1);
    }

    public String represent() {
        return AccountsServerResource.getAccounts().get(this.accountId - 1);
    }

    public void store(String account) {
        AccountsServerResource.getAccounts().set(this.accountId - 1, account);
    }
}
