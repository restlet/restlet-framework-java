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

package org.restlet.example.book.restlet.ch06.sec2.server;

import org.restlet.example.book.restlet.ch02.sec5.sub5.common.AccountResource;
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
        RepresentationInfo result = super.describe(methodInfo,
                representationClass, variant);
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
        String accountIdAttribute = getAttribute("accountId");

        if (accountIdAttribute != null) {
            this.accountId = Integer.parseInt(accountIdAttribute);
            setName("Resource for mail account '" + this.accountId + "'");
            setDescription("The resource describing the mail account number '"
                    + this.accountId + "'");
        } else {
            setName("Mail account resource");
            setDescription("The resource describing a mail account");
        }
    }

    public void remove() {
        AccountsServerResource.getAccounts().remove(this.accountId);
    }

    public String represent() {
        return AccountsServerResource.getAccounts().get(this.accountId);
    }

    public void store(String account) {
        AccountsServerResource.getAccounts().set(this.accountId, account);
    }
}
