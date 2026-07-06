/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/** Unit tests for {@link ConverterService}. */
class ConverterServiceTestCase {

    @BeforeEach
    void setUp() {
        Engine.register();
    }

    @AfterEach
    void tearDown() {
        Engine.clearThreadLocalVariables();
    }

    @Test
    void constructors_createUsableInstances() {
        assertNotNull(new ConverterService());
        ConverterService disabled = new ConverterService(false);
        assertFalse(disabled.isEnabled());
    }

    @Test
    void toRepresentation_convertsStringToStringRepresentation() throws Exception {
        ConverterService service = new ConverterService();

        Representation representation = service.toRepresentation("hello");

        assertNotNull(representation);
        assertEquals("hello", representation.getText());
        assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
    }

    @Test
    void toRepresentation_withTargetMediaType_usesRequestedType() throws Exception {
        ConverterService service = new ConverterService();

        Representation representation = service.toRepresentation("hello", MediaType.TEXT_HTML);

        assertNotNull(representation);
        assertEquals(MediaType.TEXT_HTML, representation.getMediaType());
    }

    @Test
    void toObject_convertsRepresentationToString() throws Exception {
        ConverterService service = new ConverterService();
        Representation source = new StringRepresentation("test content", MediaType.TEXT_PLAIN);

        Object result = service.toObject(source, String.class, null);

        assertEquals("test content", result);
    }

    @Test
    void toObject_withEmptyRepresentation_returnsNull() throws Exception {
        ConverterService service = new ConverterService();

        Object result = service.toObject(null);

        assertNull(result);
    }

    @Test
    void getObjectClasses_forTextVariant_includesString() {
        ConverterService service = new ConverterService();
        Variant variant = new Variant(MediaType.TEXT_PLAIN);

        List<Class<?>> classes = service.getObjectClasses(variant);

        assertNotNull(classes);
        assertTrue(classes.contains(String.class));
    }

    @Test
    void getVariants_forStringSource_returnsNonEmptyList() {
        ConverterService service = new ConverterService();

        List<? extends Variant> variants =
                service.getVariants(String.class, new Variant(MediaType.TEXT_PLAIN));

        assertNotNull(variants);
    }

    @Test
    void applyPatchCreatePatchRevertPatch_returnNullByDefault() throws Exception {
        ConverterService service = new ConverterService();
        Representation initial = new StringRepresentation("initial");
        Representation modified = new StringRepresentation("modified");

        assertNull(service.applyPatch(initial, modified));
        assertNull(service.createPatch(initial, modified));
        assertNull(service.revertPatch(modified, initial));
    }

    @Test
    void getPatchTypes_returnsNullByDefault() {
        ConverterService service = new ConverterService();

        assertNull(service.getPatchTypes(MediaType.TEXT_PLAIN));
    }

    @Test
    void updatePreferences_doesNotThrowForStringEntity() {
        ConverterService service = new ConverterService();
        List<Preference<MediaType>> preferences = new ArrayList<>();

        service.updatePreferences(preferences, String.class);
    }
}
