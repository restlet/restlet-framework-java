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
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Resource that manages a list of items.
 * 
 */
public class ItemsResource extends BaseResource {
    public ItemsResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Declare the kind of representations supported by this resource.
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    /**
     * Handle POST requests: create a new item.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        // Parse the given representation and retrieve pairs of
        // "name=value" tokens.
        Form form = new Form(entity);
        String itemName = form.getFirstValue("name");
        String itemDescription = form.getFirstValue("description");

        // Check that the item is not already registered.
        if (getItems().containsKey(itemName)) {
            generateErrorRepresentation(
                    "Item " + itemName + " already exists.", "1", getResponse());
        } else {
            // Register the new item
            getItems().put(itemName, new Item(itemName, itemDescription));

            // Set the response's status and entity
            getResponse().setStatus(Status.SUCCESS_CREATED);
            Representation rep = new StringRepresentation("Item created",
                    MediaType.TEXT_PLAIN);
            // Indicates where is located the new resource.
            rep.setIdentifier(getRequest().getResourceRef().getIdentifier()
                    + "/" + itemName);
            getResponse().setEntity(rep);
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    protected Representation describe() {
        setTitle("List of items.");
        return super.describe();
    }

    @Override
    protected void describeGet(MethodInfo info) {
        info.setIdentifier("items");
        info.setDocumentation("Retrieve the list of current items.");

        RepresentationInfo repInfo = new RepresentationInfo(MediaType.TEXT_XML);
        repInfo.setXmlElement("items");
        repInfo.setDocumentation("List of items as XML file");
        info.getResponse().getRepresentations().add(repInfo);
    }

    @Override
    protected void describePost(MethodInfo info) {
        info.setIdentifier("create_item");
        info.setDocumentation("To create an item.");

        RepresentationInfo repInfo = new RepresentationInfo(
                MediaType.APPLICATION_WWW_FORM);
        ParameterInfo param = new ParameterInfo("name", ParameterStyle.PLAIN,
                "Name of the item");
        repInfo.getParameters().add(param);
        param = new ParameterInfo("description", ParameterStyle.PLAIN,
                "Description of the item");
        repInfo.getParameters().add(param);
        repInfo.getStatuses().add(Status.SUCCESS_CREATED);

        repInfo.setDocumentation("Web form.");
        info.getRequest().getRepresentations().add(repInfo);

        FaultInfo faultInfo = new FaultInfo(Status.CLIENT_ERROR_NOT_FOUND);
        faultInfo.setIdentifier("itemError");
        faultInfo.setMediaType(MediaType.TEXT_HTML);
        info.getResponse().getFaults().add(faultInfo);
    }

    /**
     * Generate an XML representation of an error response.
     * 
     * @param errorMessage
     *            the error message.
     * @param errorCode
     *            the error code.
     */
    private void generateErrorRepresentation(String errorMessage,
            String errorCode, Response response) {
        // This is an error
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        // Generate the output representation
        try {
            DomRepresentation representation = new DomRepresentation(
                    MediaType.TEXT_XML);
            // Generate a DOM document representing the list of
            // items.
            Document d = representation.getDocument();

            Element eltError = d.createElement("error");

            Element eltCode = d.createElement("code");
            eltCode.appendChild(d.createTextNode(errorCode));
            eltError.appendChild(eltCode);

            Element eltMessage = d.createElement("message");
            eltMessage.appendChild(d.createTextNode(errorMessage));
            eltError.appendChild(eltMessage);

            response.setEntity(representation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a listing of all registered items.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            try {
                DomRepresentation representation = new DomRepresentation(
                        MediaType.TEXT_XML);
                // Generate a DOM document representing the list of
                // items.
                Document d = representation.getDocument();
                Element r = d.createElement("items");
                d.appendChild(r);
                for (Item item : getItems().values()) {
                    Element eltItem = d.createElement("item");

                    Element eltName = d.createElement("name");
                    eltName.appendChild(d.createTextNode(item.getName()));
                    eltItem.appendChild(eltName);

                    Element eltDescription = d.createElement("description");
                    eltDescription.appendChild(d.createTextNode(item
                            .getDescription()));
                    eltItem.appendChild(eltDescription);

                    r.appendChild(eltItem);
                }
                d.normalizeDocument();

                // Returns the XML representation of this document.
                return representation;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
