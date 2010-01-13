package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch04.sec3.common.AccountResource;
import org.restlet.example.book.restlet.ch04.sec3.common.AccountsResource;
import org.restlet.example.book.restlet.ch04.sec3.common.RootResource;
import org.restlet.resource.ClientResource;

/**
 * Mail client.
 */
public class MailClient {

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new MailClient().start();
    }

    /** The prototype client resource. */
    private ClientResource prototypeResource;

    /**
     * 
     * @param <T>
     * @param uri
     * @param resourceInterface
     * @return
     */
    public <T> T create(String uri, Class<? extends T> resourceInterface) {
        ClientResource cr = getPrototypeResource(); // CLONE
        cr.setReference(uri);
        return cr.wrap(resourceInterface);
    }

    /**
     * Returns the prototype client resource.
     * 
     * @return The prototype client resource.
     */
    public ClientResource getPrototypeResource() {
        return prototypeResource;
    }

    /**
     * Sets the prototype client resource.
     * 
     * @param prototypeResource
     *            The prototype client resource.
     */
    public void setPrototypeResource(ClientResource prototypeResource) {
        this.prototypeResource = prototypeResource;
    }

    /**
     * @throws Exception
     */
    public void start() throws Exception {
        System.out.println("\n1) Set-up the client connector\n");
        Client client = new Client(Protocol.HTTP);
        client.start();

        setPrototypeResource(new ClientResource((String) null));
        getPrototypeResource().setNext(client);

        System.out.println("\n2) Display the root resource\n");
        RootResource mailRoot = create("http://localhost:8182/",
                RootResource.class);
        System.out.println(mailRoot.represent());

        System.out.println("\n3) Display the initial list of accounts\n");
        AccountsResource mailAccounts = create(
                "http://localhost:8182/accounts/", AccountsResource.class);
        String list = mailAccounts.represent();
        System.out.println(list == null ? "<empty>\n" : list);

        System.out.println("4) Adds new accounts\n");
        mailAccounts.add("Tim Berners-Lee");
        mailAccounts.add("Roy Fielding");
        mailAccounts.add("Mark Baker");
        System.out.println("Three accounts added !");

        System.out.println("\n5) Display the updated list of accounts\n");
        System.out.println(mailAccounts.represent());

        System.out.println("6) Display the second account\n");
        AccountResource mailAccount = create(
                "http://localhost:8182/accounts/2", AccountResource.class);
        System.out.println(mailAccount.represent());

        System.out
                .println("\n7) Update the individual account and display it again\n");
        mailAccount.store("Roy T. Fielding");
        System.out.println(mailAccount.represent());

        System.out
                .println("\n8) Delete the first account and display the list again\n");
        mailAccount = create("http://localhost:8182/accounts/1",
                AccountResource.class);
        mailAccount.remove();
        System.out.println(mailAccounts.represent());
    }
}
