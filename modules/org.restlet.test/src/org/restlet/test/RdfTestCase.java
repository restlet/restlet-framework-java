/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.RdfN3Representation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit test case for the RIAP Internal routing protocol.
 * 
 * @author Marc Portier (mpo@outerthought.org)
 */
public class RdfTestCase extends TestCase {

    public void testN3() throws Exception {
        Representation rep = new StringRepresentation(
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>."
                        + "@prefix cfg: <http://www.w3.org/2000/10/swap/grammar/bnf#>."
                        + "@prefix : <http://www.w3.org/2000/10/swap/grammar/n3#>."
                        + "@prefix n3: <http://www.w3.org/2000/10/swap/grammar/n3#>."
                        + "@prefix list: <http://www.w3.org/2000/10/swap/list#>."
                        + "@prefix doc: <http://www.w3.org/2000/10/swap/pim/doc#>."
                        + "@prefix dc: <http://purl.org/dc/elements/1.1/>."
                        + "@keywords a, is, of."

                        + "@base    <tru   c>.\n"
                        + "#Directive base.\n"
                        + "@prefix machin <http://www . \nexample .com>.\n\n"
                        + "@keywords a, is, of."
                        + " language has _:toto <http://rdf.com>. "
                        + " bidule has _:tutu <http://www.example.com>; _:titi <http://www.exampleavecpointvirgule.com>, <http://www.exampleavecvirgule.com>. "
                        + " _:toto has <http://www.rdf.com/language> <http://allemand.com>. "
                        + " <http://www.rdf.com/language> = <http://www.language.com>. "
                        + " <http://www.rdf.com/language> => <http://www.implies.com>. "
                        + " <http://www.language.com> <= <http://www.rdf.com/language>. "
                        + "machin <http://rdf.com> \"chaine\"."
                        + "truc <http://www.multiligne.com> \"\"\"cha\nine\"\"\"."
                        + "machin <= \"\"\"cha\nine\"\"\"."
                        + "truc = <http://rdf.com>."
                        + "machin => <http://rdf.com>."
                        + "machin is <http://rdf.com>."
                        + "machin @is bidule of <http://rdf.com>." + "x1^x2."
                        + "x3^x4^x5." + "x6^x7 x8 x9." + "x10!x11."
                        + "x12!x13!x14." + "[] x15 x16." + "[:x17 :x18]."
        // + "(machin <http://rdf.com>) @is <http://rdf.com>."
        // + "() @is <http://empty.list.com>."
        // +
        // "(machin <http://rdf.com> () bidule) @is <http://empty-list.inside-with-bidule.com>."
        // +
        // "(machin <http://rdf.com> ()) is <http://empty-list.inside.com> of <http://wow.com>."
        );

        // File file = new File("/home/thierry/data/bureau/rdf/bnf.n3");
        // rep = new FileRepresentation(file.getPath(), MediaType.TEXT_PLAIN);
        new RdfN3Representation(rep, new Graph());

    }
}
