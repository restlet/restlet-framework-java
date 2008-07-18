package org.restlet.ext.shell.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.restlet.resource.Representation;

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
