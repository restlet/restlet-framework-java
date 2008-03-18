/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.restlet.Component;
import org.restlet.Transformer;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.TransformRepresentation;

/**
 * Test case for the Transformer class.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TransformerTestCase extends TestCase {
    class FailureTracker {
        boolean allOk = true;

        final StringBuffer trackedMessages = new StringBuffer();

        void report() {
            if (!allOk) {
                fail("TRACKER REPORT: \n" + trackedMessages.toString());
            }
        }

        void trackFailure(String message) {
            System.err.println(message);
            trackedMessages.append(message + "\n");
            allOk = false;
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
        Component comp = new Component();
        final TransformRepresentation tr = new TransformRepresentation(comp
                .getContext(), source, xslt);
        final FailureTracker tracker = new FailureTracker();

        int testVolume = 5000;
        Thread[] parallelTransform = new Thread[testVolume];
        for (int i = 0; i < parallelTransform.length; i++) {
            final int index = i;
            parallelTransform[i] = new Thread() {

                @Override
                public void run() {
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        tr.write(out);
                        String result = out.toString();
                        assertEquals(output, result);
                        out.close();

                    } catch (Throwable e) {
                        tracker.trackFailure(
                                "Exception during write in thread ", index, e);
                    }
                }
            };
        }

        for (Thread pt : parallelTransform) {
            pt.start();
        }

        tracker.report();
    }

    public void testTransform() throws Exception {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST,
                xslt);
        String result = transformer.transform(source).getText();

        assertEquals(output, result);
    }
}
