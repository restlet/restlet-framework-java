/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EngineTest {

    @Test
    public void engineVersionShouldBeEqualToMavenProjectVersion() {
        // When I retrieve the Maven project's version as stated in the pom file.
        Properties properties = new Properties();
        try (InputStream resourceAsStream = EngineTest.class.getClassLoader().getResourceAsStream("maven-version.properties")) {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            Assertions.fail("Can't load the properties file that contain the Maven's project version");
        }

        // Then the Maven project's version should be equal to the Engine's version.
        assertEquals(Engine.VERSION, properties.getProperty("maven.version"));
    }
}
