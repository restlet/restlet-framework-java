/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.rdf.internal.xml;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the RDF/XML format.
 * 
 * @author Thierry Boileau
 */
public class RdfXmlParsingContentHandler extends GraphHandler {

    /** The set of links to update when parsing. */
    private Graph linkSet;

    /** The representation to read. */
    private SaxRepresentation rdfXmlRepresentation;

    /**
     * Constructor.
     * 
     * @param linkSet
     *            The set of links to update during the parsing.
     * @param rdfXmlRepresentation
     *            The representation to read.
     */
    public RdfXmlParsingContentHandler(Graph linkSet,
            Representation rdfXmlRepresentation) {
        super();
        this.linkSet = linkSet;
        if (rdfXmlRepresentation instanceof SaxRepresentation) {
            this.rdfXmlRepresentation = (SaxRepresentation) rdfXmlRepresentation;
        } else {
            this.rdfXmlRepresentation = new SaxRepresentation(
                    rdfXmlRepresentation);
            // Transmit the identifier used as a base for the resolution of
            // relative URIs.
            this.rdfXmlRepresentation.setIdentifier(rdfXmlRepresentation
                    .getIdentifier());
        }
    }

    @Override
    public void link(Graph source, Reference typeRef, Literal target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    @Override
    public void link(Graph source, Reference typeRef, Reference target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        if (source != null && typeRef != null && target != null) {
            this.linkSet.add(source, typeRef, target);
        }
    }

    /**
     * Parses the current representation.
     * 
     * @throws IOException
     */
    public void parse() throws IOException {
        this.rdfXmlRepresentation.parse(new ContentReader(this,
                rdfXmlRepresentation));
    }

}
