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
import org.restlet.ext.rdf.internal.RdfConstants;

/**
 * Represents a list of Turtle tokens.
 * 
 * @author Thierry Boileau
 */
public class ListToken extends LexicalUnit {
    /** The list of contained tokens. */
    List<LexicalUnit> lexicalUnits;

    /**
     * Constructor with arguments.
     * 
     * @param contentHandler
     *            The document's parent handler.
     * @param context
     *            The parsing context.
     */
    public ListToken(RdfTurtleReader contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        lexicalUnits = new ArrayList<LexicalUnit>();
        this.parse();
    }

    public List<LexicalUnit> getLexicalUnits() {
        return lexicalUnits;
    }

    @Override
    public String getValue() {
        return lexicalUnits.toString();
    }

    @Override
    public void parse() throws IOException {
        getContentReader().parseList(this);
    }

    @Override
    public Object resolve() {
        Reference currentBlankNode = (Reference) new BlankNodeToken(
                getContentReader().newBlankNodeId()).resolve();
        for (LexicalUnit lexicalUnit : lexicalUnits) {
            Object element = lexicalUnit.resolve();

            if (element instanceof Reference) {
                getContentReader().link(currentBlankNode,
                        RdfConstants.LIST_FIRST, element);
            } else if (element instanceof String) {
                getContentReader().link(currentBlankNode,
                        RdfConstants.LIST_FIRST,
                        new Reference((String) element));
            } else {
                org.restlet.Context
                        .getCurrentLogger()
                        .warning(
                                "The list contains an element which is neither a Reference nor a literal.");
            }

            Reference restBlankNode = (Reference) new BlankNodeToken(
                    getContentReader().newBlankNodeId()).resolve();

            getContentReader().link(currentBlankNode, RdfConstants.LIST_REST,
                    restBlankNode);
            currentBlankNode = restBlankNode;
        }
        getContentReader().link(currentBlankNode, RdfConstants.LIST_REST,
                RdfConstants.OBJECT_NIL);

        return currentBlankNode;
    }
}
