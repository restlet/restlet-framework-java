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

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;
import static org.restlet.ext.atom.Service.APP_NAMESPACE;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Atom Protocol collection, part of a workspace.
 * 
 * @author Jerome Louvel
 */
public class Collection {

    /**
     * The accepted media types.
     */
    private volatile List<MediaType> accept;

    /**
     * The base reference used to resolve relative references found within the
     * scope of the xml:base attribute.
     */
    private volatile Reference baseReference;

    /** The categories. */
    private volatile Categories categories;

    /**
     * The hypertext reference.
     */
    private volatile Reference href;

    /**
     * The title.
     */
    private volatile String title;

    /**
     * The parent workspace.
     */
    private volatile Workspace workspace;

    /**
     * Constructor.
     * 
     * @param workspace
     *            The parent workspace.
     * @param title
     *            The title.
     * @param href
     *            The hypertext reference.
     */
    public Collection(Workspace workspace, String title, String href) {
        this.workspace = workspace;
        this.title = title;
        this.href = new Reference(href);
        this.accept = null;
        this.categories = null;
    }

    /**
     * Returns the accepted media types.
     * 
     * @return The accepted media types.
     */
    public List<MediaType> getAccept() {
        return this.accept;
    }

    /**
     * Returns the base reference used to resolve relative references found
     * within the scope of the xml:base attribute.
     * 
     * @return The base reference used to resolve relative references found
     *         within the scope of the xml:base attribute.
     */
    public Reference getBaseReference() {
        return baseReference;
    }

    /**
     * Returns the categories.
     * 
     * @return The categories.
     */
    public Categories getCategories() {
        return categories;
    }

    /**
     * Returns the feed representation.
     * 
     * @return The feed representation.
     * @throws Exception
     */
    public Feed getFeed() throws Exception {
        final Reference feedRef = getHref();

        if (feedRef.isRelative()) {
            feedRef.setBaseRef(getWorkspace().getService().getReference());
        }

        final Request request = new Request(Method.GET, feedRef.getTargetRef());
        final Response response = getWorkspace().getService()
                .getClientDispatcher().handle(request);

        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            return new Feed(response.getEntity());
        }

        throw new Exception(
                "Couldn't get the feed representation. Status returned: "
                        + response.getStatus());
    }

    /**
     * Returns the hypertext reference.
     * 
     * @return The hypertext reference.
     */
    public Reference getHref() {
        return this.href;
    }

    /**
     * Returns the title.
     * 
     * @return The title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the parent workspace.
     * 
     * @return The parent workspace.
     */
    public Workspace getWorkspace() {
        return this.workspace;
    }

    /**
     * Posts a member to the collection resulting in the creation of a new
     * resource.
     * 
     * @param member
     *            The member representation to post.
     * @return The reference of the new resource.
     * @throws Exception
     */
    public Reference postMember(Representation member) throws Exception {
        final Request request = new Request(Method.POST, getHref(), member);
        final Response response = getWorkspace().getService()
                .getClientDispatcher().handle(request);

        if (response.getStatus().equals(Status.SUCCESS_CREATED)) {
            return response.getLocationRef();
        }

        throw new Exception(
                "Couldn't post the member representation. Status returned: "
                        + response.getStatus());
    }

    /**
     * Sets the accepted media types.
     * 
     * @param accept
     *            The accepted media types.
     */
    public void setAccept(List<MediaType> accept) {
        this.accept = accept;
    }

    /**
     * Sets the base reference used to resolve relative references found within
     * the scope of the xml:base attribute.
     * 
     * @param baseReference
     *            The base reference used to resolve relative references found
     *            within the scope of the xml:base attribute.
     */
    public void setBaseReference(Reference baseReference) {
        this.baseReference = baseReference;
    }

    /**
     * Sets the categories.
     * 
     * @param categories
     *            The categories.
     */
    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    /**
     * Sets the hypertext reference.
     * 
     * @param href
     *            The hypertext reference.
     */
    public void setHref(Reference href) {
        this.href = href;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the parent workspace.
     * 
     * @param workspace
     *            The parent workspace.
     */
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {

        final AttributesImpl attributes = new AttributesImpl();
        if ((getHref() != null) && (getHref().toString() != null)) {
            attributes.addAttribute("", "href", null, "atomURI", getHref()
                    .toString());
        }

        writer.startElement(APP_NAMESPACE, "collection", null, attributes);

        if (getTitle() != null) {
            writer.dataElement(ATOM_NAMESPACE, "title", getTitle());
        }

        if (getAccept() != null) {
            StringBuilder sb = new StringBuilder();
            for (MediaType mediaType : getAccept()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }

                sb.append(mediaType.toString());
            }

            writer.dataElement(APP_NAMESPACE, "accept", sb.toString());
        }

        try {
            if (getCategories() != null) {
                getCategories().writeElement(writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        writer.endElement(APP_NAMESPACE, "collection");
    }

}
