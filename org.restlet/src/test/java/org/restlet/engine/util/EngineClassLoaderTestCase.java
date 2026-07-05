/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.util.Enumeration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.engine.Engine;

class EngineClassLoaderTestCase {

    private Engine engine;

    private ClassLoader previousUserClassLoader;

    @BeforeEach
    void setUpEach() {
        engine = Engine.getInstance();
        previousUserClassLoader = engine.getUserClassLoader();
    }

    @AfterEach
    void tearDownEach() {
        engine.setUserClassLoader(previousUserClassLoader);
    }

    @Test
    void loadClass_existingClass_delegatesToUserClassLoader() throws ClassNotFoundException {
        engine.setUserClassLoader(EngineClassLoaderTestCase.class.getClassLoader());
        EngineClassLoader loader = new EngineClassLoader(engine);

        Class<?> result = loader.loadClass("java.lang.String");

        assertEquals(String.class, result);
    }

    @Test
    void loadClass_withoutUserClassLoader_fallsBackToContextClassLoader()
            throws ClassNotFoundException {
        engine.setUserClassLoader(null);
        EngineClassLoader loader = new EngineClassLoader(engine);

        Class<?> result = loader.loadClass("java.lang.String");

        assertEquals(String.class, result);
    }

    @Test
    void loadClass_unknownClass_throwsClassNotFoundException() {
        EngineClassLoader loader = new EngineClassLoader(engine);

        assertThrows(
                ClassNotFoundException.class, () -> loader.loadClass("com.example.NoSuchClass"));
    }

    @Test
    void getResource_delegatesToUserClassLoader() {
        engine.setUserClassLoader(EngineClassLoaderTestCase.class.getClassLoader());
        EngineClassLoader loader = new EngineClassLoader(engine);

        URL result = loader.getResource("java/lang/String.class");

        assertNotNull(result);
    }

    @Test
    void getResources_returnsDeduplicatedEnumeration() throws Exception {
        engine.setUserClassLoader(EngineClassLoaderTestCase.class.getClassLoader());
        EngineClassLoader loader = new EngineClassLoader(engine);

        Enumeration<URL> resources = loader.getResources("java/lang/String.class");

        assertNotNull(resources);
    }

    @Test
    void getEngine_returnsConstructorArgument() {
        EngineClassLoader loader = new EngineClassLoader(engine);
        assertEquals(engine, loader.getEngine());
    }
}
