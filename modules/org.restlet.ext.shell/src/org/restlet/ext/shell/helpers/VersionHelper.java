package org.restlet.ext.shell.helpers;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class VersionHelper {

    private static String version = null;

    public static String getVersion() {
        if (version == null) {
            final Properties manifestProperties = new Properties();
            final URL url = ClassLoader
                    .getSystemResource("META-INF/MANIFEST.MF");

            try {
                manifestProperties.load(url.openStream());
            } catch (final IOException ex) {
                version = "unknown";
            }

            version = manifestProperties.getProperty("Implementation-Version");
        }

        return version;
    }

    private VersionHelper() {
    }
}
