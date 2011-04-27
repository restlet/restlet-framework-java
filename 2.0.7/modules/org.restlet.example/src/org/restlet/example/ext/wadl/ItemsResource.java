/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

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
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Resource that manages a list of items.
 * 
 */
public class ItemsResource extends BaseResource {

    /**
     * Handle POST requests: create a new item.
     */
    @Post
    public Representation acceptItem(Representation entity) {
        Representation result = null;
        // Parse the given representation and retrieve pairs of
        // "name=value" tokens.
        Form form = new Form(entity);
        String itemName = form.getFirstValue("name");
        String itemDescription = form.getFirstValue("description");

        // Register the new item if one is not already registered.
        if (!getItems().containsKey(itemName)
                && getItems().putIfAbsent(itemName,
                        new Item(itemName, itemDescription)) == null) {
            // Set the response's status and entity
            setStatus(Status.SUCCESS_CREATED);
            Representation rep = new StringRepresentation("Item created",
                    MediaType.TEXT_PLAIN);
            // Indicates where is located the new resource.
            rep.setLocationRef(getRequest().getResourceRef().getIdentifier() + "/"
                    + itemName);
            result = rep;
        } else { // Item is already registered.
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            result = generateErrorRepresentation("Item " + itemName
                    + " already exists.", "1");
        }

        return result;
    }

    @Override
    protected Representation describe() {
        setName("List of items.");
        return super.describe();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void describeGet(MethodInfo info) {
        info.setIdentifier("items");
        info.setDocumentation("Retrieve the list of current items.");

        ResponseInfo response = new ResponseInfo();
        RepresentationInfo repInfo = new RepresentationInfo(MediaType.TEXT_XML);
        repInfo.setXmlElement("items");
        repInfo.setDocumentation("List of items as XML file");
        response.getRepresentations().add(repInfo);

        response.getFaults().add(
                new org.restlet.ext.wadl.FaultInfo(
                        Status.CLIENT_ERROR_BAD_REQUEST, "Not good at all"));
        info.getResponses().add(response);
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
        repInfo.setDocumentation("Web form.");
        info.getRequest().getRepresentations().add(repInfo);

        ResponseInfo response = new ResponseInfo();
        response.getStatuses().add(Status.SUCCESS_CREATED);
        info.getResponses().add(response);

        response = new ResponseInfo();
        response.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        info.getResponses().add(response);

        repInfo = new RepresentationInfo(MediaType.TEXT_HTML);
        repInfo.setIdentifier("itemError");
        response.getRepresentations().add(repInfo);
    }

    /**
     * Generate an XML representation of an error response.
     * 
     * @param errorMessage
     *            the error message.
     * @param errorCode
     *            the error code.
     */
    private Representation generateErrorRepresentation(String errorMessage,
            String errorCode) {
        DomRepresentation result = null;
        // This is an error
        // Generate the output representation
        try {
            result = new DomRepresentation(MediaType.TEXT_XML);
            // Generate a DOM document representing the list of
            // items.
            Document d = result.getDocument();

            Element eltError = d.createElement("error");

            Element eltCode = d.createElement("code");
            eltCode.appendChild(d.createTextNode(errorCode));
            eltError.appendChild(eltCode);

            Element eltMessage = d.createElement("message");
            eltMessage.appendChild(d.createTextNode(errorMessage));
            eltError.appendChild(eltMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Returns a listing of all registered items.
     */
    public Representation toXml() {
        // Generate the right representation according to its media type.
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

        return null;
    }
}
