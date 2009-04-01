package org.restlet.example.firstResource;

import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ItemResource extends BaseResource {

    /** The sequence of characters that identifies the resource. */
    String itemName;

    /** The underlying Item object. */
    Item item;

    @Override
    public void init() {
        // Get the "itemName" attribute value taken from the URI template
        // /items/{itemName}.
        this.itemName = (String) getRequest().getAttributes().get("itemName");

        // Get the item directly from the "persistence layer".
        this.item = getItems().get(itemName);

        if (this.item != null) {
            // Define the supported variant.
            getVariants().put(Method.ALL, new Variant(MediaType.TEXT_XML));
            // By default a resource cannot be updated.
        } else {
            // This resource is not available.
            setExists(false);
        }
    }

    /**
     * Handle DELETE requests.
     */
    @Override
    public Representation delete() throws ResourceException {
        if (item != null) {
            // Remove the item from the list.
            getItems().remove(item.getName());
        }

        // Tells the client that the request has been successfully fulfilled.
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            try {
                DomRepresentation representation = new DomRepresentation(
                        MediaType.TEXT_XML);
                // Generate a DOM document representing the item.
                Document d = representation.getDocument();

                Element eltItem = d.createElement("item");
                d.appendChild(eltItem);
                Element eltName = d.createElement("name");
                eltName.appendChild(d.createTextNode(item.getName()));
                eltItem.appendChild(eltName);

                Element eltDescription = d.createElement("description");
                eltDescription.appendChild(d.createTextNode(item
                        .getDescription()));
                eltItem.appendChild(eltDescription);

                d.normalizeDocument();

                // Returns the XML representation of this document.
                return representation;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Handle PUT requests.
     */
    @Override
    public Representation put(Representation entity) throws ResourceException {
        // Tells if the item is to be created of not.
        boolean creation = (item == null);

        // The PUT request updates or creates the resource.
        if (item == null) {
            item = new Item(itemName);
        }

        // Update the description.
        Form form = new Form(entity);
        item.setDescription(form.getFirstValue("description"));

        // Update the item in the list.
        getItems().put(item.getName(), item);

        if (creation) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }

        return null;
    }
}
