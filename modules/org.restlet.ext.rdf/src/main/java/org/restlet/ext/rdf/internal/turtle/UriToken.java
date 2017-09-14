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

package org.restlet.ext.rdf.internal.turtle;

import java.io.IOException;

import org.restlet.data.Reference;

/**
 * Represents a URI token inside a RDF Turtle document.
 * 
 * @author Thierry Boileau
 */
public class UriToken extends LexicalUnit {
    /**
     * Constructor with arguments.
     * 
     * @param contentHandler
     *            The document's parent handler.
     * @param context
     *            The parsing context.
     */
    public UriToken(RdfTurtleReader contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        this.parse();
    }

    @Override
    public void parse() throws IOException {
        getContentReader().parseUri(this);
    }

    @Override
    public Reference resolve() {
        return new Reference(getValue());
    }

    @Override
    public String toString() {
        return getValue();
    }
}
