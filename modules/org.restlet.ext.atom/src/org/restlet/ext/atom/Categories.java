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

import java.io.IOException;
import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.atom.internal.CategoriesContentReader;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
import org.xml.sax.SAXException;

/**
 * Collection of {@link Category} entries.
 * 
 * @author Jerome Louvel
 */
public class Categories extends SaxRepresentation {

    /**
     * The base reference used to resolve relative references found within the
     * scope of the xml:base attribute.
     */
    private volatile Reference baseReference;

    /**
     * The list of entries.
     */
    private volatile List<Category> entries;

    /** Indicates if the list is fixed. */
    private boolean fixed;

    /** */
    private Reference scheme;

    /**
     * Constructor.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     * @param categoriesUri
     *            The feed URI.
     * @throws IOException
     */
    public Categories(Client clientDispatcher, String categoriesUri)
            throws IOException {
        this(clientDispatcher.handle(new Request(Method.GET, categoriesUri))
                .getEntity());
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context from which the client dispatcher will be
     *            retrieved.
     * @param categoriesUri
     *            The feed URI.
     * @throws IOException
     */
    public Categories(Context context, String categoriesUri) throws IOException {
        this(context.getClientDispatcher()
                .handle(new Request(Method.GET, categoriesUri)).getEntity());
    }

    /**
     * Constructor.
     * 
     * @param categoriesFeed
     *            The XML entries document.
     * @throws IOException
     */
    public Categories(Representation categoriesFeed) throws IOException {
        super(categoriesFeed);
        setNamespaceAware(true);
        parse(new CategoriesContentReader(this));
    }

    /**
     * Constructor.
     * 
     * @param categoriesUri
     *            The feed URI.
     * @throws IOException
     */
    public Categories(String categoriesUri) throws IOException {
        this(new Client(new Reference(categoriesUri).getSchemeProtocol()),
                categoriesUri);

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
     * Returns the list of entries.
     * 
     * @return The list of entries.
     */
    public List<Category> getEntries() {
        return entries;
    }

    /**
     * Returns the scheme.
     * 
     * @return The scheme.
     */
    public Reference getScheme() {
        return this.scheme;
    }

    /**
     * Indicates if the list is fixed.
     * 
     * @return True if the list is fixed.
     */
    public boolean isFixed() {
        return fixed;
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
     * Sets the list of entries.
     * 
     * @param entries
     *            The list of entries.
     */
    public void setEntries(List<Category> entries) {
        this.entries = entries;
    }

    /**
     * Indicates if the list is fixed.
     * 
     * @param fixed
     *            True if the list is fixed.
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Sets the scheme.
     * 
     * @param scheme
     *            The scheme.
     */
    public void setScheme(Reference scheme) {
        this.scheme = scheme;
    }

    /**
     * Writes the representation to a XML writer.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws IOException
     */
    @Override
    public void write(XmlWriter writer) throws IOException {
        try {
            writer.setPrefix(APP_NAMESPACE, "");
            writer.setPrefix(ATOM_NAMESPACE, "atom");
            writer.setDataFormat(true);
            writer.setIndentStep(3);
            writer.startDocument();
            writeElement(writer);
            writer.endDocument();
        } catch (SAXException e) {
            IOException ioe = new IOException(
                    "Unable to write the AtomPub categories document.");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Writes the representation to a XML writer.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        writer.startElement(APP_NAMESPACE, "categories");

        for (final Category entry : getEntries()) {
            entry.writeElement(writer);
        }

        writer.endElement(APP_NAMESPACE, "categories");
        writer.endDocument();
    }
}
