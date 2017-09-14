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

package org.restlet.test.ext.lucene;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.tika.parser.rtf.RTFParser;
import org.restlet.ext.lucene.TikaRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the Lucene extension.
 * 
 * @author Jerome Louvel
 */
public class LuceneTestCase extends RestletTestCase {

    public void testTika() throws Exception {
        ClientResource r = new ClientResource(
                "clap://system/org/restlet/test/ext/lucene/LuceneTestCase.rtf");

        Representation rtfSample = r.get();
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
