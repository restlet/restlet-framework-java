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

package org.restlet.ext.rdf.internal.xml;

import java.io.IOException;

import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.internal.RdfReader;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the RDF/XML format.
 * 
 * @author Thierry Boileau
 */
public class RdfXmlReader extends RdfReader {

    /**
     * Constructor.
     * 
     * @param rdfRepresentation
     *            The representation to read.
     * @param graphHandler
     *            The graph handler invoked during the parsing.
     * @throws IOException
     */
    public RdfXmlReader(Representation rdfRepresentation,
            GraphHandler graphHandler) {
        super(rdfRepresentation, graphHandler);
    }

    /**
     * Parses the current representation.
     * 
     * @throws Exception
     */
    public void parse() throws Exception {
        SaxRepresentation saxRepresentation;
        if (getRdfRepresentation() instanceof SaxRepresentation) {
            saxRepresentation = (SaxRepresentation) getRdfRepresentation();
        } else {
            saxRepresentation = new SaxRepresentation(getRdfRepresentation());
            // Transmit the identifier used as a base for the resolution of
            // relative URIs.
            saxRepresentation.setLocationRef(getRdfRepresentation().getLocationRef());
        }

        saxRepresentation.parse(new ContentReader(saxRepresentation,
                getGraphHandler()));
    }

}
