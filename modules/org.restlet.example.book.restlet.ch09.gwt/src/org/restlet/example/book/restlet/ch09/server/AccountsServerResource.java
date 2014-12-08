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

package org.restlet.example.book.restlet.ch09.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.MediaType;
import org.restlet.example.book.restlet.ch09.common.AccountRepresentation;
import org.restlet.example.book.restlet.ch09.common.AccountsResource;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Implementation of the resource containing the list of mail accounts.
 */
public class AccountsServerResource extends WadlServerResource implements
        AccountsResource {

    /** Static list of accounts stored in memory. */
    private static final List<AccountRepresentation> accounts = new CopyOnWriteArrayList<AccountRepresentation>();

    @Override
    protected void describe(ApplicationInfo applicationInfo) {
        RepresentationInfo rep = new RepresentationInfo(MediaType.TEXT_PLAIN);
        rep.setIdentifier("account");
        applicationInfo.getRepresentations().add(rep);

        DocumentationInfo doc = new DocumentationInfo();
        doc.setTitle("Account");
        doc.setTextContent("Simple string containing the account ID");
        rep.getDocumentations().add(doc);
    }

    @Override
    protected RepresentationInfo describe(MethodInfo methodInfo,
            Class<?> representationClass, Variant variant) {
        RepresentationInfo result = new RepresentationInfo(variant);
        result.setReference("account");
        return result;
    }

    @Override
    protected void doInit() throws ResourceException {
        setName("Mail accounts resource");
        setDescription("The resource containing the list of mail accounts");
    }

    /**
     * Returns the static list of accounts stored in memory.
     * 
     * @return The static list of accounts.
     */
    public static List<AccountRepresentation> getAccounts() {
        return accounts;
    }

    public String represent() {
        StringBuilder result = new StringBuilder();

        for (AccountRepresentation account : getAccounts()) {
            result.append((account == null) ? "" : account).append('\n');
        }

        return result.toString();
    }

    public String add(AccountRepresentation account) {
        getAccounts().add(account);
        return Integer.toString(getAccounts().indexOf(account));
    }
}
