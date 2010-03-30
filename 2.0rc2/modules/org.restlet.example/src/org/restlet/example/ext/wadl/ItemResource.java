package org.restlet.example.ext.wadl;

import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ItemResource extends BaseResource {

    /** The underlying Item object. */
    Item item;

    /** The sequence of characters that identifies the resource. */
    String itemName;

    @Override
    public Representation describe() {
        setTitle("Representation of a single item");
        return super.describe();
    }

    @Override
    protected void describeDelete(MethodInfo info) {
        info.setDocumentation("Delete the current item.");

        ResponseInfo response = new ResponseInfo(
                "No representation is returned.");
        response.getStatuses().add(Status.SUCCESS_NO_CONTENT);
        info.getResponses().add(response);
    }

    @Override
    protected void describeGet(MethodInfo info) {
        info.setIdentifier("item");
        info.setDocumentation("To retrieve details of a specific item");

        ResponseInfo response = new ResponseInfo();
        RepresentationInfo repInfo = new RepresentationInfo(MediaType.TEXT_XML);
        repInfo.setXmlElement("item");
        repInfo.setDocumentation("XML representation of the current item.");
        response.getRepresentations().add(repInfo);

        info.getResponses().add(response);

        response = new ResponseInfo("Item not found");
        repInfo = new RepresentationInfo(MediaType.TEXT_HTML);
        repInfo.setIdentifier("itemError");
        response.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        response.getRepresentations().add(repInfo);
        info.getResponses().add(response);
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
        repInfo.setDocumentation("Web form.");
        info.getRequest().getRepresentations().add(repInfo);

        ResponseInfo response = new ResponseInfo();
        response.getStatuses().add(Status.SUCCESS_OK);
        response.getStatuses().add(Status.SUCCESS_CREATED);
        info.getResponses().add(response);

        response = new ResponseInfo();
        response.getStatuses().add(Status.SUCCESS_OK);
        response.getStatuses().add(Status.SUCCESS_CREATED);
        info.getResponses().add(response);

        super.describePut(info);
    }

    @Override
    protected void doInit() throws ResourceException {
        // Get the "itemName" attribute value taken from the URI template
        // /items/{itemName}.
        this.itemName = (String) getRequest().getAttributes().get("itemName");

        if (itemName != null) {
            // Get the item directly from the "persistence layer".
            this.item = getItems().get(itemName);
        }

        setExisting(this.item != null);
    }

    /**
     * Handle DELETE requests.
     */
    @Delete
    public void removeItem() {
        if (item != null) {
            // Remove the item from the list.
            getItems().remove(item.getName());
        }

        // Tells the client that the request has been successfully fulfilled.
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    /**
     * Handle PUT requests.
     * 
     * @throws IOException
     */
    @Put
    public void storeItem(Representation entity) throws IOException {
        // The PUT request updates or creates the resource.
        if (item == null) {
            item = new Item(itemName);
        }

        // Update the description.
        Form form = new Form(entity);
        item.setDescription(form.getFirstValue("description"));

        if (getItems().putIfAbsent(item.getName(), item) == null) {
            setStatus(Status.SUCCESS_CREATED);
        } else {
            setStatus(Status.SUCCESS_OK);
        }
    }

    @Get("xml")
    public Representation toXml() {
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
            eltDescription.appendChild(d.createTextNode(item.getDescription()));
            eltItem.appendChild(eltDescription);

            d.normalizeDocument();

            // Returns the XML representation of this document.
            return representation;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
