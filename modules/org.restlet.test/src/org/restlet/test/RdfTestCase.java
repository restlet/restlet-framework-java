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
                        + " _:x1 has <http://www.rdf.com> :x2. "
                        + " :x3 has _:x4 <http://www.example.com>; _:x5 <http://www.examplewith;.com>, <http://www.examplewith,.com>. "
                        + " _:x6 has <http://www.rdf.com/language> <http://www.deutsch.com>. "
                        + " <http://www.rdf.com/language> = <http://www.language.com>. "
                        + " <http://www.rdf.com/language> => <http://www.implies.com>. "
                        + " <http://www.language.com> <= <http://www.rdf.com/language>. "
                        + ":x7 <http://rdf.com> \"string\"."
                        + ":x8 <http://www.multiline.com> \"\"\"str\ning\"\"\"."
                        + ":x9 <= \"\"\"str\ning\"\"\"."
                        + ":x10 @is <http://rdf.com> of <http://www.example.com>."
                        + ":x11^:x12." + ":x13^:x14^:x15."
                        + ":x16^:x17 :x18 :x19." + ":x20!:x21."
                        + ":x22!:x23!:x24." + "[] :x25 :x26." + "[:x27 :x28]."
                        + "(:x29 :x30) :x31 :x32.");

        // File file = new File("/bnf.n3");
        // rep = new FileRepresentation(file.getPath(), MediaType.TEXT_PLAIN);
        new RdfN3Representation(rep, new Graph());

    }
}
