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

package org.restlet.example.book.restlet.ch03.sec3.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.example.book.restlet.ch02.sec5.sub5.common.AccountsResource;
import org.restlet.resource.ServerResource;

/**
 * Implementation of the resource containing the list of mail accounts.
 */
public class AccountsServerResource extends ServerResource implements
        AccountsResource {

    /** Static list of accounts stored in memory. */
    private static final List<String> accounts = new CopyOnWriteArrayList<String>();

    /**
     * Returns the static list of accounts stored in memory.
     * 
     * @return The static list of accounts.
     */
    public static List<String> getAccounts() {
        return accounts;
    }

    public String represent() {
        StringBuilder result = new StringBuilder();

        for (String account : getAccounts()) {
            result.append((account == null) ? "" : account).append('\n');
        }

        return result.toString();
    }

    public String add(String account) {
        getAccounts().add(account);
        return Integer.toString(getAccounts().indexOf(account));
    }
}
