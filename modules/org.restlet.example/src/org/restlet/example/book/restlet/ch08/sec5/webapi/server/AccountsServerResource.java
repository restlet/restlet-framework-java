package org.restlet.example.book.restlet.ch08.sec5.webapi.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.MediaType;
import org.restlet.example.book.restlet.ch08.sec5.webapi.common.AccountRepresentation;
import org.restlet.example.book.restlet.ch08.sec5.webapi.common.AccountsResource;
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
        return Integer.toString(getAccounts().indexOf(account) + 1);
    }
}
