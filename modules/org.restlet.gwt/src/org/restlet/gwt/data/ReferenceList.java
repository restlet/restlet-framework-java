/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.gwt.internal.util.LineReader;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.resource.StringRepresentation;
import org.restlet.gwt.util.WrapperList;

/**
 * List of URI references.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ReferenceList extends WrapperList<Reference> {
    /** The list's identifier. */
    private volatile Reference identifier;

    /**
     * Constructor.
     */
    public ReferenceList() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity
     *                The initial list capacity.
     */
    public ReferenceList(int initialCapacity) {
        super(new ArrayList<Reference>(initialCapacity));
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *                The delegate list.
     */
    public ReferenceList(List<Reference> delegate) {
        super(delegate);
    }

    /**
     * Constructor from a "text/uri-list" representation.
     * 
     * @param uriList
     *                The "text/uri-list" representation to parse.
     * @throws IOException
     */
    public ReferenceList(Representation uriList) throws Exception {
        LineReader lr = new LineReader(uriList.getText());
        String line = lr.readLine();

        // Checks if the list reference is specified as the first comment.
        if ((line != null) && line.startsWith("#")) {
            setIdentifier(new Reference(line.substring(1).trim()));
            line = lr.readLine();
        }

        while (line != null) {
            if (!line.startsWith("#")) {
                add(new Reference(line.trim()));
            }

            line = lr.readLine();
        }
    }

    /**
     * Creates then adds a reference at the end of the list.
     * 
     * @param uri
     *                The uri of the reference to add.
     * @return True (as per the general contract of the Collection.add method).
     */
    public boolean add(String uri) {
        return add(new Reference(uri));
    }

    /**
     * Returns the list identifier.
     * 
     * @return The list identifier.
     */
    public Reference getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns a representation of the list in the "text/uri-list" format.
     * 
     * @return A representation of the list in the "text/uri-list" format.
     */
    public Representation getTextRepresentation() {
        StringBuilder sb = new StringBuilder();

        if (getIdentifier() != null) {
            sb.append("# ").append(getIdentifier().toString()).append("\r\n");
        }

        for (Reference ref : this) {
            sb.append(ref.toString()).append("\r\n");
        }

        return new StringRepresentation(sb.toString(), MediaType.TEXT_URI_LIST);
    }

    /**
     * Returns a representation of the list in "text/html" format.
     * 
     * @return A representation of the list in "text/html" format.
     */
    public Representation getWebRepresentation() {
        // Create a simple HTML list
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>\n");

        if (getIdentifier() != null) {
            sb.append("<h2>Listing of \"" + getIdentifier().getPath()
                    + "\"</h2>\n");
            Reference parentRef = getIdentifier().getParentRef();

            if (!parentRef.equals(getIdentifier())) {
                sb.append("<a href=\"" + parentRef + "\">..</a><br>\n");
            }
        } else {
            sb.append("<h2>List of references</h2>\n");
        }

        for (Reference ref : this) {
            sb.append("<a href=\"" + ref.toString() + "\">"
                    + ref.getRelativeRef(getIdentifier()) + "</a><br>\n");
        }
        sb.append("</body></html>\n");

        return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
    }

    /**
     * Sets the list reference.
     * 
     * @param identifier
     *                The list identifier.
     */
    public void setIdentifier(Reference identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the list reference.
     * 
     * @param identifier
     *                The list identifier as a URI.
     */
    public void setIdentifier(String identifier) {
        setIdentifier(new Reference(identifier));
    }

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     * 
     * @param fromIndex
     *                The start position.
     * @param toIndex
     *                The end position (exclusive).
     * @return The sub-list.
     */
    @Override
    public ReferenceList subList(int fromIndex, int toIndex) {
        return new ReferenceList(getDelegate().subList(fromIndex, toIndex));
    }

}
