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
    public void parse() throws IOException {
        SaxRepresentation saxRepresentation;
        if (getRdfRepresentation() instanceof SaxRepresentation) {
            saxRepresentation = (SaxRepresentation) getRdfRepresentation();
        } else {
            saxRepresentation = new SaxRepresentation(getRdfRepresentation());
            // Transmit the identifier used as a base for the resolution of
            // relative URIs.
            saxRepresentation.setLocationRef(getRdfRepresentation()
                    .getLocationRef());
        }

        saxRepresentation.parse(new ContentReader(saxRepresentation,
                getGraphHandler()));
    }

}
