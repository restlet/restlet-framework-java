package org.restlet.example.ext.wadl;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.wadl.FaultInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ItemResource extends BaseResource {

    /** The underlying Item object. */
    Item item;

    /** The sequence of characters that identifies the resource. */
    String itemName;

    public ItemResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get the "itemName" attribute value taken from the URI template
        // /items/{itemName}.
        this.itemName = (String) getRequest().getAttributes().get("itemName");

        if (itemName != null) {
            // Get the item directly from the "persistence layer".
            this.item = getItems().get(itemName);

            if (this.item != null) {
                // Define the supported variant.
                getVariants().add(new Variant(MediaType.TEXT_XML));
            } else {
                // This resource is not available.
                setAvailable(false);
            }
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public Representation describe() {
        setTitle("Item " + this.itemName);
        return super.describe();
    }

    @Override
    protected void describeGet(MethodInfo info) {
        info.setIdentifier("item");
        info.setDocumentation("To retrieve details of a specific item");

        RepresentationInfo repInfo = new RepresentationInfo(MediaType.TEXT_XML);
        repInfo.setXmlElement("item");
        repInfo.setDocumentation("XML representation of the current item.");
        info.getResponse().getRepresentations().add(repInfo);

        FaultInfo faultInfo = new FaultInfo(Status.CLIENT_ERROR_NOT_FOUND,
                "Item not found");
        faultInfo.setIdentifier("itemError");
        faultInfo.setMediaType(MediaType.TEXT_HTML);
        info.getResponse().getFaults().add(faultInfo);
    }

    @Override
    protected void describeDelete(MethodInfo info) {
        info.setDocumentation("Delete the current item.");

        RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setDocumentation("No representation is returned.");
        repInfo.getStatuses().add(Status.SUCCESS_NO_CONTENT);
        info.getResponse().getRepresentations().add(repInfo);
    }

    @Override
    protected void describePut(MethodInfo info) {
        info.setDocumentation("Update or create the current item.");

        RepresentationInfo repInfo = new RepresentationInfo(
                MediaType.APPLICATION_WWW_FORM);
        ParameterInfo param = new ParameterInfo("name", ParameterStyle.PLAIN,
                "Name of the item");
        repInfo.getParameters().add(param);
        param = new ParameterInfo("description", ParameterStyle.PLAIN,
                "Description of the item");
        repInfo.getParameters().add(param);
        repInfo.getStatuses().add(Status.SUCCESS_OK);
        repInfo.getStatuses().add(Status.SUCCESS_CREATED);

        repInfo.setDocumentation("Web form.");
        info.getRequest().getRepresentations().add(repInfo);

        super.describePut(info);
    }

    /**
     * Handle DELETE requests.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        if (item != null) {
            // Remove the item from the list.
            getItems().remove(item.getName());
        }

        // Tells the client that the request has been successfully fulfilled.
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
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
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        // The PUT request updates or creates the resource.
        if (item == null) {
            item = new Item(itemName);
        }

        // Update the description.
        Form form = new Form(entity);
        item.setDescription(form.getFirstValue("description"));

        if (getItems().putIfAbsent(item.getName(), item) == null) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }
    }
}
