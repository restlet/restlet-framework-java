package org.restlet.example.ext.wadl;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

public class FirstResourceClient {

    public static void main(String[] args) throws IOException {
        // The URI of the application.
        Reference appUri = new Reference("http://localhost:8182/firstResource/");
        // The URI of the resource "list of items".
        Reference itemsUri = new Reference(appUri, "items");

        Client client = new Client(Protocol.HTTP);

        // Displays the WADL documentation of the application
        client.options(appUri).getEntity().write(System.out);

        // Displays the WADL documentation of the "items" resource
        client.options(itemsUri).getEntity().write(System.out);

        // Create a new item
        Item item = new Item("item1", "this is an item.");
        Reference itemUri = FirstResourceClient.createItem(item, client,
                itemsUri);

        // Displays the WADL documentation of the "item1" resource
        client.options(itemUri).getEntity().write(System.out);
    }

    /**
     * Try to create a new item.
     * 
     * @param item
     *            the new item.
     * @param client
     *            the Restlet HTTP client.
     * @param itemsUri
     *            where to POST the data.
     * @return the Reference of the new resource if the creation succeeds, null
     *         otherwise.
     */
    public static Reference createItem(Item item, Client client,
            Reference itemsUri) {
        // Gathering informations into a Web form.
        Form form = new Form();
        form.add("name", item.getName());
        form.add("description", item.getDescription());
        Representation rep = form.getWebRepresentation();

        // Launch the request
        Response response = client.post(itemsUri, rep);
        if (response.getStatus().isSuccess()) {
            return response.getEntity().getIdentifier();
        }

        return null;
    }
}
