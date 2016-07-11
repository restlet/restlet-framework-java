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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Workspace containing collections of members entries.
 * 
 * @author Jerome Louvel
 */
public class Workspace {

    /**
     * The base reference used to resolve relative references found within the
     * scope of the xml:base attribute.
     */
    private volatile Reference baseReference;

    /**
     * The list of collections.
     */
    private volatile List<Collection> collections;

    /**
     * The parent service.
     */
    private volatile Service service;

    /**
     * The title.
     */
    private volatile String title;

    /**
     * Constructor.
     * 
     * @param service
     *            The parent service.
     */
    public Workspace(Service service) {
        this(service, null);
    }

    /**
     * Constructor.
     * 
     * @param service
     *            The parent service.
     * @param title
     *            The title.
     */
    public Workspace(Service service, String title) {
        this.service = service;
        this.title = title;
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
     * Returns the list of collections.
     * 
     * @return The list of collections.
     */
    public List<Collection> getCollections() {
        if (this.collections == null) {
            this.collections = new ArrayList<Collection>();
        }

        return this.collections;
    }

    /**
     * Returns the parent service.
     * 
     * @return The parent service.
     */
    public Service getService() {
        return this.service;
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
     * Sets the parent service.
     * 
     * @param service
     *            The parent service.
     */
    public void setService(Service service) {
        this.service = service;
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
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        writer.startElement(APP_NAMESPACE, "workspace");

        if (getTitle() != null) {
            writer.dataElement(ATOM_NAMESPACE, "title", getTitle());
        }

        for (final Collection collection : getCollections()) {
            collection.writeElement(writer);
        }

        writer.endElement(APP_NAMESPACE, "workspace");
    }

}
