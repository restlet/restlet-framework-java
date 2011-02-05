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

package org.restlet.ext.lucene.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.solr.common.util.ContentStream;
import org.restlet.representation.Representation;

/**
 * Solr content stream wrapping a Restlet representation.
 * 
 * @author Rémi Dewitte <remi@gide.net>
 */
public class SolrRepresentationContentStream implements ContentStream {

    /** The wrapped representation. */
    private Representation representation;

    /**
     * Constructor.
     * 
     * @param representation
     *            The wrapped representation.
     */
    public SolrRepresentationContentStream(Representation representation) {
        this.representation = representation;
    }

    /**
     * Returns the wrapped representation's media type.
     * 
     * @return The wrapped representation's media type.
     * @see ContentStream#getContentType()
     */
    public String getContentType() {
        if (representation.getMediaType() != null)
            return representation.getMediaType().getName();
        return null;
    }

    /**
     * Returns the wrapped representation's download name.
     * 
     * @return The wrapped representation's download name.
     * @see ContentStream#getName()
     */
    public String getName() {
        if (representation.getDisposition() != null) {
            representation.getDisposition().getFilename();
        }

        return null;
    }

    /**
     * Returns the wrapped representation's reader.
     * 
     * @return The wrapped representation's reader.
     * @see ContentStream#getReader()
     */
    public Reader getReader() throws IOException {
        return representation.getReader();
    }

    /**
     * Returns the wrapped representation's size.
     * 
     * @return The wrapped representation's size.
     * @see ContentStream#getSize()
     */
    public Long getSize() {
        long s = representation.getSize();
        if (s == Representation.UNKNOWN_SIZE)
            return null;
        return s;
    }

    /**
     * Returns the wrapped representation's identifier.
     * 
     * @return The wrapped representation's identifier.
     * @see ContentStream#getSourceInfo()
     */
    public String getSourceInfo() {
        if (representation.getLocationRef() != null)
            return representation.getLocationRef().toString();
        return null;
    }

    /**
     * Returns the wrapped representation's stream.
     * 
     * @return The wrapped representation's stream.
     * @see ContentStream#getStream()
     */
    public InputStream getStream() throws IOException {
        return representation.getStream();
    }

}