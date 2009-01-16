/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.apache.tika.parser.rtf.RTFParser;
import org.restlet.Client;
import org.restlet.ext.lucene.TikaRepresentation;
import org.restlet.resource.Representation;

/**
 * Unit tests for the Lucene extension.
 * 
 * @author Jerome Louvel
 */
public class LuceneTestCase extends TestCase {

    public void testTika() throws Exception {

        Client clapClient = new Client("clap");
        Representation rtfSample = clapClient.get(
                "clap://system/org/restlet/test/LuceneTestCase.rtf")
                .getEntity();
        // rtfSample.write(System.out);

        // Prepare a SAX content handler
        SAXTransformerFactory factory = ((SAXTransformerFactory) TransformerFactory
                .newInstance());
        TransformerHandler transform = factory.newTransformerHandler();
        transform.setResult(new StreamResult(System.out));

        // Analyze the RTF representation
        TikaRepresentation tr = new TikaRepresentation(rtfSample);
        tr.setTikaParser(new RTFParser());
        tr.parse(transform);
    }
}
