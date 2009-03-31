/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.test;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.TransformRepresentation;
import org.restlet.routing.Transformer;

/**
 * Test case for the Transformer class.
 * 
 * @author Jerome Louvel
 */
public class TransformerTestCase extends TestCase {
    class FailureTracker {
        boolean allOk = true;

        final StringBuffer trackedMessages = new StringBuffer();

        void report() {
            if (!this.allOk) {
                fail("TRACKER REPORT: \n" + this.trackedMessages.toString());
            }
        }

        void trackFailure(String message) {
            System.err.println(message);
            this.trackedMessages.append(message + "\n");
            this.allOk = false;
        }

        void trackFailure(String message, int index, Throwable e) {
            e.printStackTrace();
            trackFailure(message + " " + index + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            new TransformerTestCase().testTransform();
            new TransformerTestCase().parallelTestTransform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>23.45";

    // Create a source XML document
    final Representation source = new StringRepresentation(
            "<?xml version=\"1.0\"?>" + "<purchase id=\"p001\">"
                    + "<customer db=\"cust123\"/>" + "<product db=\"prod345\">"
                    + "<amount>23.45</amount>" + "</product>" + "</purchase>",
            MediaType.TEXT_XML);

    // Create a transform XSLT sheet
    final Representation xslt = new StringRepresentation(
            "<?xml version=\"1.0\"?>"
                    + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                    + "<xsl:template match =\"customer\">"
                    + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                    + "</xsl:template>" + "</xsl:transform>",
            MediaType.TEXT_XML);

    /**
     * This was removed from the automatically tested method because it is too
     * consuming.
     * 
     * @throws Exception
     */
    public void parallelTestTransform() throws Exception {
        final Component comp = new Component();
        final TransformRepresentation tr = new TransformRepresentation(comp
                .getContext(), this.source, this.xslt);
        final FailureTracker tracker = new FailureTracker();

        final int testVolume = 5000;
        final Thread[] parallelTransform = new Thread[testVolume];
        for (int i = 0; i < parallelTransform.length; i++) {
            final int index = i;
            parallelTransform[i] = new Thread() {

                @Override
                public void run() {
                    try {
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        tr.write(out);
                        final String result = out.toString();
                        assertEquals(TransformerTestCase.this.output, result);
                        out.close();

                    } catch (Throwable e) {
                        tracker.trackFailure(
                                "Exception during write in thread ", index, e);
                    }
                }
            };
        }

        for (final Thread pt : parallelTransform) {
            pt.start();
        }

        tracker.report();
    }

    public void testTransform() throws Exception {
        final Transformer transformer = new Transformer(
                Transformer.MODE_REQUEST, this.xslt);
        final String result = transformer.transform(this.source).getText();

        assertEquals(this.output, result);
    }
}
