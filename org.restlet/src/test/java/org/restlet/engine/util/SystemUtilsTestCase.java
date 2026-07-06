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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SystemUtilsTestCase {

    @Test
    void getJavaMajorVersion_parsesJavaVersionSystemProperty() {
        String javaVersion = System.getProperty("java.version");
        int dotIndex = javaVersion.indexOf('.');
        int expected = dotIndex < 0 ? 0 : Integer.parseInt(javaVersion.substring(0, dotIndex));
        assertEquals(expected, SystemUtils.getJavaMajorVersion());
    }

    @Test
    void getJavaMinorVersion_returnsZero_whenVersionHasNoMinorComponent() {
        // Modern JDKs (e.g. "21") have no "major.minor" form, exercising the catch branch.
        assertEquals(0, SystemUtils.getJavaMinorVersion());
    }

    @Test
    void getJavaUpdateVersion_returnsZero_whenVersionHasNoUpdateComponent() {
        // Modern JDKs have no "_update" suffix, exercising the catch branch.
        assertEquals(0, SystemUtils.getJavaUpdateVersion());
    }

    @Test
    void hashCode_nullVarargs_returnsBaseValue() {
        assertEquals(17, SystemUtils.hashCode((Object[]) null));
    }

    @Test
    void hashCode_noArguments_returnsBaseValue() {
        assertEquals(17, SystemUtils.hashCode(new Object[0]));
    }

    @Test
    void hashCode_withNullElement_treatsAsZero() {
        assertEquals(17 * 31, SystemUtils.hashCode((Object) null));
    }

    @Test
    void hashCode_isConsistentForEqualInputs() {
        assertEquals(SystemUtils.hashCode("a", "b"), SystemUtils.hashCode("a", "b"));
    }

    @Test
    void hashCode_differsForDifferentInputs() {
        assertNotEquals(SystemUtils.hashCode("a", "b"), SystemUtils.hashCode("a", "c"));
    }

    @Test
    void isWindows_matchesOsNameProperty() {
        boolean expected = System.getProperty("os.name").toLowerCase().contains("win");
        assertEquals(expected, SystemUtils.isWindows());
    }

    @Test
    void shouldApplyBug6331920Patch_returnsFalseOnModernNonSunJvm() {
        // This JVM is neither an old Sun JVM nor an old Java version, so no patch is needed.
        assertFalse(SystemUtils.shouldApplyBug6331920Patch());
    }
}
