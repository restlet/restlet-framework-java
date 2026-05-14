/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.thymeleaf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.thymeleaf.templateresource.ITemplateResource;

/** Unit tests for {@link ThymeleafConverter}. */
class ThymeleafConverterTestCase {

    private static ITemplateResource dummyResource() {
        return new ITemplateResource() {
            @Override
            public String getDescription() {
                return "dummy";
            }

            @Override
            public String getBaseName() {
                return "test";
            }

            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public Reader reader() {
                return new StringReader("");
            }

            @Override
            public ITemplateResource relative(String relativeLocation) {
                return this;
            }
        };
    }

    @Test
    void getObjectClasses_alwaysReturnsEmptyList() {
        assertTrue(
                new ThymeleafConverter()
                        .getObjectClasses(new Variant(MediaType.TEXT_HTML))
                        .isEmpty());
    }

    @Test
    void getVariants_forITemplateResource_returnsMediaTypeAll() {
        List<?> variants = new ThymeleafConverter().getVariants(ITemplateResource.class);
        assertEquals(1, variants.size());
        assertTrue(MediaType.ALL.isCompatible(((Variant) variants.getFirst()).getMediaType()));
    }

    @Test
    void getVariants_forOtherClass_returnsEmptyList() {
        assertTrue(new ThymeleafConverter().getVariants(String.class).isEmpty());
    }

    @Test
    void score_iTemplateResource_returnsOne() {
        assertEquals(
                1.0f,
                new ThymeleafConverter()
                        .score(dummyResource(), new Variant(MediaType.TEXT_HTML), null));
    }

    @Test
    void score_nonITemplateResource_returnsMinusOne() {
        assertEquals(
                -1.0f,
                new ThymeleafConverter()
                        .score("not a template", new Variant(MediaType.TEXT_HTML), null));
    }

    @Test
    void score_repr_alwaysReturnsMinusOne() {
        assertEquals(
                -1.0f,
                new ThymeleafConverter()
                        .score(new StringRepresentation("text"), String.class, null));
    }

    @Test
    void toObject_alwaysReturnsNull() {
        assertNull(
                new ThymeleafConverter()
                        .toObject(new StringRepresentation("text"), String.class, null));
    }

    @Test
    void toRepresentation_nonITemplateResource_returnsNull() {
        assertNull(
                new ThymeleafConverter()
                        .toRepresentation("string", new Variant(MediaType.TEXT_HTML), null));
    }

    @Test
    void updatePreferences_forITemplateResource_addsMediaTypeAll() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new ThymeleafConverter().updatePreferences(prefs, ITemplateResource.class);
        assertEquals(1, prefs.size());
        assertEquals(MediaType.ALL, prefs.getFirst().getMetadata());
    }

    @Test
    void updatePreferences_forOtherClass_doesNotModify() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new ThymeleafConverter().updatePreferences(prefs, String.class);
        assertTrue(prefs.isEmpty());
    }
}
