/*
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.shell.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.restlet.representation.Representation;

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
        } catch (IOException e) {
            throw new RuntimeException("unable to read file");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
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
        } catch (IOException ex) {
            throw new RuntimeException("cannot write to "
                    + representation.getDownloadName(), ex);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                // ignored
            }
        }
    }

    private RepresentationHelper() {
    }
}
