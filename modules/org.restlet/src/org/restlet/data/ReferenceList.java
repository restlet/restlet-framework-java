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

    private static final String webPageStyle = "h1{border-bottom:1px solid silver;margin-bottom:10px;padding-bottom:10px;white-space:nowrap}"
            + "thead tr{font-weight:700}td.detailsColumn{-webkit-padding-start:2em;-moz-padding-start:2em;text-align:end;white-space:nowrap}"
            + "a.icon{-webkit-padding-start:1.5em;-moz-padding-start:1.5em;text-decoration:none}"
            + "a.icon:hover{text-decoration:underline}"
            + "a.file{background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH1QQWFA84umAmQgAAANpJREFUOMutkj1uhDAQhb8HSLtbISGfgZ+zbJkix0HmFhwhUdocBnMBGvqtTIqIFSReWKK8aix73nwzHrVt+zEMwwvH9FrX9TsA1trpqKy10+yUzME4jnjvAZB0LzXHkojjmDRNVyh3A+89zrlVwlKSqKrqVy/J8lAUxSZBSMny4ZLgp54iyPM8UPHGNJ2IomibAKDv+9VlWZbABbgB5/0WQgSSkC4PF2JF4JzbHN430c4vhAm0TyCJruuClefph4yCBCGT3T3Isoy/KDHGfDZNcz2SZIx547/0BVRRX7n8uT/sAAAAAElFTkSuQmCC) left top no-repeat}"
            + "a.dir{background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAN1wAADdcBQiibeAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAHCSURBVDiNpZAxa5NRFIafc+9XLCni4BC6FBycMnbrLpkcgtDVX6C70D/g4lZX/4coxLlgxFkpiiSSUGm/JiXfveee45AmNlhawXc53HvPee55X+l2u/yPqt3d3Tfu/viatwt3fzIYDI5uBJhZr9fr3TMzzAx3B+D09PR+v98/7HQ6z5fNOWdCCGU4HH6s67oAVDlnV1UmkwmllBUkhMD29nYHeLuEAkyn06qU8qqu64MrgIyqYmZrkHa73drc3KTVahFjJITAaDRiPB4/XFlQVVMtHH5IzJo/P4EA4MyB+erWPQB7++zs7ccYvlU5Z08pMW2cl88eIXLZeDUpXzsBkNQ5eP1+p0opmaoCTgzw6fjs6gLLsp58FB60t0DcK1Ul54yIEIMQ43Uj68pquDmCeJVztpwzuBNE2LgBoMVpslHMCUEAFgDVxQbzVAiA+aK5uGPmmDtZF3VpoUm2ArhqQaRiUjcMf81p1G60UEVhcjZfAFTVUkrgkS+jc06mDX9nvq4YhJ9nlxZExMwMEaHJRutOdWuIIsJFUoBSuTvHJ4YIfP46unV4qdlsjsBRZRtb/XfHd5+C8+P7+J8BIoxFwovfRxYhnhxjpzEAAAAASUVORK5CYII=) left top no-repeat}"
            + "a.up{background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAN1wAADdcBQiibeAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAIkSURBVDiNpZJNa1NBFIafM3Nr08SqWDDEQsFF7SIboeKie8mqiyB06y/QvdA/4MZd3fo3RBTiTigY7cYilVYpTUmoNrdpmzszd46LfLSB4sazGebMOc/5eEdqtRr/Y8ny8vIbVV294u1MVZ80m83NfwJijPV6vX4zxkiMEVUF4Pj4eK7RaGxUq9Xno2DvPcaY/ODg4HOapjlA4r3XEAKdToc8z8cQYwyVSqUKvBtBAXq9XpLn+as0TdcvATwhBGKME5ByuVwsFAoUi0WstRhjaLVatNvtxfEIIYQYQs7GR8dpdlEJBAClD/THXlUDrKyxsLJmrfmZeO/VOUcvU14+e4zIMPDypnTiBoALyvrrDwuJcy6GEADFGviy273cwOiY3LwV7pVLIKpJCAHvPSKCNYK1FynXCwmPFm+TGOHT99+k536gRohoVATRxHsfvfegihFhygrGCPcr11m6O4s1A2DtQZlv+ylf97q4oJiBfwAIYdBB3+XcuTHN0vwsM9csZy4fj6+qzM/NcKs0xdZel8zFMUBDyBFJ6KQZ2/snbO78YfVhZVRlsAuBzEfeNg+xRliYmxkAQgjROQdq2WmdcNTLUOCo58aKjJbpQiQ991gjHHaHI4hIjDEiImQ+UpxOAOj7fJh4QcmjUppOEBHOXADIE1Vl9ygiAls/WuPg7b3DSQmH7Yy+9elpH4HNJE6VGu93Z5+Csv+rfYXqV5sIbRHz4i/uqzb/IJewTwAAAABJRU5ErkJggg==) left top no-repeat}";

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
     * @see http://amundsen.com/hypermedia/urilist/
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
        sb.append("<html><head><meta charset=\"utf-8\">");
        sb.append("<style>").append(webPageStyle).append("</style>");
        sb.append("</head>");
        sb.append("<body>");

        if (getIdentifier() != null) {
            sb.append("<h1>Index of \"").append(getIdentifier().getPath())
                    .append("\"</h1>\n");
        }
        sb.append("<table><thead><tr><td>Name</td></tr></thead>");
        sb.append("<tbody>");
        if (getIdentifier() != null) {
            Reference parentRef = getIdentifier().getParentRef();
            if (!getIdentifier().equals(parentRef)) {
                sb.append("<tr><td><a class=\"icon up\" href=\"");
                sb.append(parentRef.toString());
                sb.append("\">[parent directory]</a></td></tr>");
            }
        }
        for (Reference ref : this) {
            String str = ref.toString();
            if (str.endsWith("/")) {
                sb.append("<tr><td><a class=\"icon dir\" href=\"");
            } else {
                sb.append("<tr><td><a class=\"icon file\" href=\"");
            }
            sb.append(str);
            sb.append("\">");
            sb.append(ref.getRelativeRef(getIdentifier()));
            sb.append("</td></tr>");
        }
        sb.append("</tbody></table>\n");
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
