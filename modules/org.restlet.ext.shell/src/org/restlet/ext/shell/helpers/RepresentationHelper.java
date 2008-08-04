/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.ext.shell.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.restlet.resource.Representation;

/**
 * 
 * @author Davide Angelocola
 */
public class RepresentationHelper {

    public static String loadFrom(String fileName) {
        final File file = new File(fileName);

        BufferedReader bufferedReader = null;
        StringWriter stringWriter = null;
        String line;

        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            stringWriter = new StringWriter();

            while ((line = bufferedReader.readLine()) != null) {
                stringWriter.append(line);
            }

            return stringWriter.toString();
        } catch (final IOException e) {
            throw new RuntimeException("unable to read file");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException ex) {
                    // ignored
                }
            }
        }
    }

    public static void save(Representation representation) {
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(representation
                    .getDownloadName());
            representation.write(outputStream);
        } catch (final IOException ex) {
            throw new RuntimeException("cannot write to "
                    + representation.getDownloadName(), ex);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (final IOException ex) {
                // ignored
            }
        }
    }

    private RepresentationHelper() {
    }
}
