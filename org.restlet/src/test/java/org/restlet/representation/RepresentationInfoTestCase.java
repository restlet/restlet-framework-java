/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;

/**
 * Unit tests for the {@link RepresentationInfo} class.
 *
 * @author Jerome Louvel
 */
class RepresentationInfoTestCase {

    @Test
    void defaultConstructor_hasNoMediaTypeOrTag() {
        RepresentationInfo info = new RepresentationInfo();

        assertNull(info.getMediaType());
        assertNull(info.getTag());
        assertNull(info.getModificationDate());
    }

    @Test
    void constructor_withMediaType_setsMediaType() {
        RepresentationInfo info = new RepresentationInfo(MediaType.APPLICATION_JSON);

        assertEquals(MediaType.APPLICATION_JSON, info.getMediaType());
    }

    @Test
    void constructor_withMediaTypeAndModificationDate_setsBoth() {
        Date date = new Date(0);
        RepresentationInfo info = new RepresentationInfo(MediaType.TEXT_PLAIN, date);

        assertEquals(MediaType.TEXT_PLAIN, info.getMediaType());
        assertEquals(date, info.getModificationDate());
    }

    @Test
    void constructor_withMediaTypeModificationDateAndTag_setsAllFields() {
        Date date = new Date(0);
        Tag tag = new Tag("etag-value");
        RepresentationInfo info = new RepresentationInfo(MediaType.TEXT_PLAIN, date, tag);

        assertEquals(MediaType.TEXT_PLAIN, info.getMediaType());
        assertEquals(date, info.getModificationDate());
        assertEquals(tag, info.getTag());
    }

    @Test
    void constructor_withMediaTypeAndTag_setsBoth() {
        Tag tag = new Tag("etag-value");
        RepresentationInfo info = new RepresentationInfo(MediaType.TEXT_PLAIN, tag);

        assertEquals(MediaType.TEXT_PLAIN, info.getMediaType());
        assertEquals(tag, info.getTag());
        assertNull(info.getModificationDate());
    }

    @Test
    void constructor_fromVariant_copiesVariantMetadata() {
        Variant variant = new Variant(MediaType.APPLICATION_XML);
        variant.setCharacterSet(CharacterSet.UTF_8);
        Date date = new Date(0);
        Tag tag = new Tag("copied-tag");

        RepresentationInfo info = new RepresentationInfo(variant, date, tag);

        assertEquals(MediaType.APPLICATION_XML, info.getMediaType());
        assertEquals(CharacterSet.UTF_8, info.getCharacterSet());
        assertEquals(date, info.getModificationDate());
        assertEquals(tag, info.getTag());
    }

    @Test
    void constructor_fromVariantAndTag_copiesVariantMetadataWithoutDate() {
        Variant variant = new Variant(MediaType.APPLICATION_XML);
        Tag tag = new Tag("copied-tag");

        RepresentationInfo info = new RepresentationInfo(variant, tag);

        assertEquals(MediaType.APPLICATION_XML, info.getMediaType());
        assertEquals(tag, info.getTag());
        assertNull(info.getModificationDate());
    }

    @Test
    void setModificationDateThenGetModificationDate_roundTrips() {
        RepresentationInfo info = new RepresentationInfo();
        Date date = new Date(123456789L);

        info.setModificationDate(date);

        assertEquals(date, info.getModificationDate());
    }

    @Test
    void setTagThenGetTag_roundTrips() {
        RepresentationInfo info = new RepresentationInfo();
        Tag tag = new Tag("my-tag");

        info.setTag(tag);

        assertEquals(tag, info.getTag());
    }
}
