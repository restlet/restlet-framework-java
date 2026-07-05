/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.LocalReference}. */
class LocalReferenceTestCase {

    @Test
    void constructor_fromUriString() {
        LocalReference reference = new LocalReference("file:///tmp/test.txt");
        assertEquals("file", reference.getScheme());
    }

    @Test
    void constructor_fromReference() {
        Reference base = new Reference("file:///tmp/test.txt");
        LocalReference reference = new LocalReference(base);
        assertEquals("file", reference.getScheme());
    }

    @Test
    void createFileReference_fromFile() {
        LocalReference reference = LocalReference.createFileReference(new File("/tmp/test.txt"));
        assertEquals("file", reference.getScheme());
        assertNotNull(reference.getFile());
    }

    @Test
    void createFileReference_fromPath() {
        LocalReference reference = LocalReference.createFileReference("/tmp/test.txt");
        assertEquals("file", reference.getScheme());
        assertEquals("", reference.getAuthority());
    }

    @Test
    void createFileReference_withHostName() {
        LocalReference reference = LocalReference.createFileReference("localhost", "/tmp/test.txt");
        assertEquals("localhost", reference.getAuthority());
    }

    @Test
    void createClapReference_fromPackage() {
        LocalReference reference = LocalReference.createClapReference(String.class.getPackage());
        assertEquals("clap", reference.getScheme());
        assertTrue(reference.toString().contains("java/lang"));
    }

    @Test
    void createClapReference_fromPath() {
        LocalReference reference = LocalReference.createClapReference("/org/restlet/Restlet.class");
        assertEquals("clap", reference.getScheme());
    }

    @Test
    void createClapReference_withAuthorityType() {
        LocalReference reference =
                LocalReference.createClapReference(
                        LocalReference.CLAP_SYSTEM, "/org/restlet/Restlet.class");
        assertEquals("system", reference.getAuthority());
        assertEquals(LocalReference.CLAP_SYSTEM, reference.getClapAuthorityType());
    }

    @Test
    void getClapAuthorityType_defaultsToClapDefault() {
        LocalReference reference = LocalReference.createClapReference("/org/restlet/Restlet.class");
        assertEquals(LocalReference.CLAP_DEFAULT, reference.getClapAuthorityType());
    }

    @Test
    void getClapAuthorityType_forNonClapReference_returnsZero() {
        LocalReference reference = new LocalReference("http://example.com");
        assertEquals(0, reference.getClapAuthorityType());
    }

    @Test
    void createRiapReference_buildsRiapUri() {
        LocalReference reference =
                LocalReference.createRiapReference(LocalReference.RIAP_COMPONENT, "/myApp/path");
        assertEquals("riap", reference.getScheme());
        assertEquals(LocalReference.RIAP_COMPONENT, reference.getRiapAuthorityType());
    }

    @Test
    void getRiapAuthorityType_forNonRiapReference_returnsZero() {
        LocalReference reference = new LocalReference("http://example.com");
        assertEquals(0, reference.getRiapAuthorityType());
    }

    @Test
    void createJarReference_buildsJarUri() {
        Reference jarFile = new Reference("http://example.com/archive.jar");
        LocalReference reference = LocalReference.createJarReference(jarFile, "entry/path.txt");
        assertEquals("jar", reference.getScheme());
        assertEquals("entry/path.txt", reference.getJarEntryPath());
        assertEquals("http://example.com/archive.jar", reference.getJarFileRef().toString());
    }

    @Test
    void getJarEntryPath_forNonJarReference_returnsNull() {
        LocalReference reference = new LocalReference("http://example.com");
        assertNull(reference.getJarEntryPath());
    }

    @Test
    void createZipReference_buildsZipUri() {
        Reference zipFile = new Reference("file:///tmp/archive.zip");
        LocalReference reference = LocalReference.createZipReference(zipFile, "entry.txt");
        assertEquals("zip", reference.getScheme());
    }

    @Test
    void getFile_forRemoteHost_throwsRuntimeException() {
        LocalReference reference =
                LocalReference.createFileReference("remotehost", "/tmp/test.txt");
        assertThrows(RuntimeException.class, reference::getFile);
    }

    @Test
    void getFile_forNonFileReference_returnsNull() {
        LocalReference reference = new LocalReference("http://example.com");
        assertNull(reference.getFile());
    }

    @Test
    void getAuthorityName_mapsAuthorityConstants() {
        assertEquals("", LocalReference.getAuthorityName(LocalReference.CLAP_DEFAULT));
        assertEquals("class", LocalReference.getAuthorityName(LocalReference.CLAP_CLASS));
        assertEquals("system", LocalReference.getAuthorityName(LocalReference.CLAP_SYSTEM));
        assertEquals("thread", LocalReference.getAuthorityName(LocalReference.CLAP_THREAD));
        assertEquals(
                "application", LocalReference.getAuthorityName(LocalReference.RIAP_APPLICATION));
        assertEquals("component", LocalReference.getAuthorityName(LocalReference.RIAP_COMPONENT));
        assertEquals("host", LocalReference.getAuthorityName(LocalReference.RIAP_HOST));
    }

    @Test
    void localizePath_convertsSlashesToSystemSeparator() {
        String result = LocalReference.localizePath("a/b/c");
        assertEquals("a" + File.separatorChar + "b" + File.separatorChar + "c", result);
    }

    @Test
    void normalizePath_convertsSystemSeparatorToSlash() {
        String path = "a" + File.separatorChar + "b";
        String result = LocalReference.normalizePath(path);
        assertEquals("a/b", result);
    }
}
