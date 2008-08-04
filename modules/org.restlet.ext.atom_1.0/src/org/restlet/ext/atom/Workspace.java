/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;
import static org.restlet.ext.atom.Service.APP_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Workspace containing collections of members entries.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Workspace {

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
