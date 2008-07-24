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

package org.restlet.ext.shell.helpers;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * 
 * @author Davide Angelocola
 */
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
