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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/** Test {@link org.restlet.data.ReferenceList}. */
class ReferenceListTestCase {

    @Test
    void defaultConstructor_isEmpty() {
        ReferenceList list = new ReferenceList();
        assertTrue(list.isEmpty());
        assertNull(list.getIdentifier());
    }

    @Test
    void constructor_withInitialCapacity_isEmpty() {
        ReferenceList list = new ReferenceList(10);
        assertTrue(list.isEmpty());
    }

    @Test
    void constructor_withDelegate_wrapsGivenList() {
        List<Reference> delegate = new ArrayList<>();
        delegate.add(new Reference("https://example.com/a"));
        ReferenceList list = new ReferenceList(delegate);
        assertEquals(1, list.size());
    }

    @Test
    void addUri_createsReferenceAndAppendsIt() {
        ReferenceList list = new ReferenceList();
        assertTrue(list.add("https://example.com/a"));
        assertEquals(1, list.size());
        assertEquals("https://example.com/a", list.getFirst().toString());
    }

    @Test
    void setAndGetIdentifier_withReference() {
        ReferenceList list = new ReferenceList();
        Reference identifier = new Reference("https://example.com/");
        list.setIdentifier(identifier);
        assertEquals(identifier, list.getIdentifier());
    }

    @Test
    void setIdentifier_withStringUri_createsReference() {
        ReferenceList list = new ReferenceList();
        list.setIdentifier("https://example.com/");
        assertEquals("https://example.com/", list.getIdentifier().toString());
    }

    @Test
    void constructor_fromUriListRepresentation_parsesLines() throws IOException {
        String content =
                """
                # https://example.com/
                https://example.com/a
                https://example.com/b
                """;
        Representation representation = new StringRepresentation(content, MediaType.TEXT_URI_LIST);
        ReferenceList list = new ReferenceList(representation);

        assertEquals("https://example.com/", list.getIdentifier().toString());
        assertEquals(2, list.size());
        assertEquals("https://example.com/a", list.getFirst().toString());
        assertEquals("https://example.com/b", list.get(1).toString());
    }

    @Test
    void constructor_fromUriListRepresentation_withoutIdentifierComment() throws IOException {
        String content = "https://example.com/a\r\n";
        Representation representation = new StringRepresentation(content, MediaType.TEXT_URI_LIST);
        ReferenceList list = new ReferenceList(representation);

        assertNull(list.getIdentifier());
        assertEquals(1, list.size());
    }

    @Test
    void getTextRepresentation_containsIdentifierAndReferences() throws IOException {
        ReferenceList list = new ReferenceList();
        list.setIdentifier("https://example.com/");
        list.add("https://example.com/a");

        Representation representation = list.getTextRepresentation();
        assertEquals(MediaType.TEXT_URI_LIST, representation.getMediaType());
        String text = representation.getText();
        assertTrue(text.contains("# https://example.com/"));
        assertTrue(text.contains("https://example.com/a"));
    }

    @Test
    void getWebRepresentation_producesHtmlListing() throws IOException {
        ReferenceList list = new ReferenceList();
        list.setIdentifier("https://example.com/dir/");
        list.add("https://example.com/dir/a");

        Representation representation = list.getWebRepresentation();
        assertEquals(MediaType.TEXT_HTML, representation.getMediaType());
        String text = representation.getText();
        assertTrue(text.contains("<html>"));
        assertTrue(text.contains("https://example.com/dir/a"));
    }

    @Test
    void getWebRepresentation_withoutIdentifier_usesGenericTitle() throws IOException {
        ReferenceList list = new ReferenceList();
        list.add("https://example.com/a");

        Representation representation = list.getWebRepresentation();
        String text = representation.getText();
        assertTrue(text.contains("List of references"));
    }

    @Test
    void subList_returnsReferenceListInstance() {
        ReferenceList list = new ReferenceList();
        list.add("https://example.com/a");
        list.add("https://example.com/b");
        list.add("https://example.com/c");

        ReferenceList sub = list.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("https://example.com/b", sub.getFirst().toString());
        assertEquals("https://example.com/c", sub.get(1).toString());
    }
}
