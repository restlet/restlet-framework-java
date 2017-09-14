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

package org.restlet.ext.rdf.internal.n3;

import java.io.IOException;

import org.restlet.ext.rdf.internal.turtle.Context;
import org.restlet.ext.rdf.internal.turtle.LexicalUnit;

/**
 * Allows to parse a formula in RDF N3 notation. Please note that this kind of
 * feature is not supported yet.
 * 
 * @author Thierry Boileau
 */
public class FormulaToken extends LexicalUnit {

    public FormulaToken(RdfN3Reader contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        this.parse();
    }

    @Override
    public void parse() throws IOException {
        ((RdfN3Reader) getContentReader()).parseFormula(this);
    }

    @Override
    public Object resolve() {
        org.restlet.Context.getCurrentLogger().warning(
                "Formulae are not supported yet.");
        return null;
    }
}
