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
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Link;

/**
 * Represents a blank node inside a RDF N3 document. Contains all the logic to
 * parse a blank node in N3 documents.
 * 
 * @author Thierry Boileau
 */
public class BlankNodeToken extends LexicalUnit {

    /** List of lexical units contained by this blank node. */
    private List<LexicalUnit> lexicalUnits;

    /** Indicates if the given blank node has been already resolved. */
    private boolean resolved = false;

    /**
     * Constructor. The blank node is given a new identifier thanks to the
     * context.
     * 
     * @param contentHandler
     *            The parent content handler.
     * @param context
     *            The context used to resolved references.
     * @throws IOException
     */
    public BlankNodeToken(RdfTurtleReader contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        lexicalUnits = new ArrayList<LexicalUnit>();
        this.setValue("_:" + contentHandler.newBlankNodeId());
        lexicalUnits.add(this);
        this.parse();
    }

    /**
     * Constructor
     * 
     * @param value
     *            The value of this blank node.
     */
    public BlankNodeToken(String value) {
        super(value);
        this.resolved = true;
    }

    public List<LexicalUnit> getLexicalUnits() {
        return lexicalUnits;
    }

    @Override
    public void parse() throws IOException {
        getContentReader().parseBlankNode(this);
    }

    @Override
    public Object resolve() {
        if (!this.resolved) {
            this.resolved = true;
            if (getContentReader() != null) {
                getContentReader().generateLinks(lexicalUnits);
            }
        }

        if (getValue() != null) {
            if (getValue().startsWith("_:")) {
                return new Reference(getValue());
            }

            return Link.createBlankRef(getValue());
        }

        org.restlet.Context.getCurrentLogger().warning(
                "A blank node has been detected with a null value.");

        return null;
    }
}
