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

package org.restlet.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.WrapperList;

/**
 * List of URI references.
 * 
 * @author Jerome Louvel
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
     *            The initial list capacity.
     */
    public ReferenceList(int initialCapacity) {
        super(new ArrayList<Reference>(initialCapacity));
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate list.
     */
    public ReferenceList(List<Reference> delegate) {
        super(delegate);
    }

    /**
     * Constructor from a "text/uri-list" representation.
     * 
     * @param uriList
     *            The "text/uri-list" representation to parse.
     * @throws IOException
     */
    public ReferenceList(Representation uriList) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(uriList.getReader(), IoUtils.BUFFER_SIZE);

            String line = br.readLine();

            // Checks if the list reference is specified as the first comment.
            if ((line != null) && line.startsWith("#")) {
                setIdentifier(new Reference(line.substring(1).trim()));
                line = br.readLine();
            }

            while (line != null) {
                if (!line.startsWith("#")) {
                    add(new Reference(line.trim()));
                }

                line = br.readLine();
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Creates then adds a reference at the end of the list.
     * 
     * @param uri
     *            The uri of the reference to add.
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
        final StringBuilder sb = new StringBuilder();

        if (getIdentifier() != null) {
            sb.append("# ").append(getIdentifier().toString()).append("\r\n");
        }

        for (final Reference ref : this) {
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
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"font-family: sans-serif;\">\n");

        if (getIdentifier() != null) {
            sb.append("<h2>Listing of \"" + getIdentifier().getPath()
                    + "\"</h2>\n");
            final Reference parentRef = getIdentifier().getParentRef();

            if (!parentRef.equals(getIdentifier())) {
                sb.append("<a href=\"" + parentRef + "\">..</a><br>\n");
            }
        } else {
            sb.append("<h2>List of references</h2>\n");
        }

        for (final Reference ref : this) {
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
     *            The list identifier.
     */
    public void setIdentifier(Reference identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the list reference.
     * 
     * @param identifier
     *            The list identifier as a URI.
     */
    public void setIdentifier(String identifier) {
        setIdentifier(new Reference(identifier));
    }

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     * 
     * @param fromIndex
     *            The start position.
     * @param toIndex
     *            The end position (exclusive).
     * @return The sub-list.
     */
    @Override
    public ReferenceList subList(int fromIndex, int toIndex) {
        // [ifndef gwt] line
        return new ReferenceList(getDelegate().subList(fromIndex, toIndex));
        // [ifdef gwt] uncomment
        // return new
        // ReferenceList(org.restlet.engine.util.ListUtils.copySubList(getDelegate(),
        // fromIndex, toIndex));
        // [enddef]
    }

}
