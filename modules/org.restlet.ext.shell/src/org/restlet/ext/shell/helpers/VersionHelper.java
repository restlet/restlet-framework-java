package org.restlet.ext.shell.helpers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class VersionHelper {

    private static String version = null;

    private VersionHelper() {
    }

    public static String getVersion() {
        if (version == null) {
            Properties manifestProperties = new Properties();
            URL url = ClassLoader.getSystemResource("META-INF/MANIFEST.MF");

            try {
                 manifestProperties.load(url.openStream());
            } catch (IOException ex) {
                version = "unknown";
            }

            version = manifestProperties.getProperty("Implementation-Version");
        }

        return version;
    }
}
