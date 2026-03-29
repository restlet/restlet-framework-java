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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.restlet.data.MediaType.ALL;
import static org.restlet.data.MediaType.APPLICATION_ALL;
import static org.restlet.data.MediaType.APPLICATION_ALL_XML;
import static org.restlet.data.MediaType.APPLICATION_ATOM;
import static org.restlet.data.MediaType.APPLICATION_ATOMPUB_SERVICE;
import static org.restlet.data.MediaType.APPLICATION_OCTET_STREAM;
import static org.restlet.data.MediaType.APPLICATION_XML;
import static org.restlet.data.MediaType.IMAGE_ALL;
import static org.restlet.data.MediaType.TEXT_ALL;
import static org.restlet.data.MediaType.TEXT_PLAIN;
import static org.restlet.data.MediaType.getMostSpecific;
import static org.restlet.data.MediaType.register;
import static org.restlet.data.MediaType.valueOf;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.util.Series;

/**
 * Test {@link org.restlet.data.MediaType}.
 *
 * @author Jerome Louvel
 */
class MediaTypeTestCase {

    /**
     * Makes sure that a {@link MediaType} instance initialized on the specified name has the
     * expected values.
     *
     * @param name type to analyze.
     * @param expectedMain expected main type.
     * @param expectedSub expected subtype.
     * @param expectedConcrete expected 'concrete' flag.
     */
    public void assertMediaType(
            String name, String expectedMain, String expectedSub, boolean expectedConcrete) {
        MediaType type;

        type = new MediaType(name);
        assertEquals(expectedMain, type.getMainType());
        assertEquals(expectedSub, type.getSubType());
        assertEquals(expectedConcrete, type.isConcrete());
    }

    /** Makes sure concrete types are properly initialized. */
    @ParameterizedTest
    @CsvSource({
        "application/xml,application,xml,true",
        "application/ xml,application,xml,true,",
        "application /xml,application,xml,true",
        "application / xml ,application,xml,true",
        "application/atom+xml;type=entry,application,atom+xml,true",
    })
    void testConcrete(
            String name, String expectedMain, String expectedSub, boolean expectedConcrete) {
        assertMediaType(name, expectedMain, expectedSub, expectedConcrete);
    }

    /** Makes sure concrete types are properly initialized. */
    @Test
    void testParameters() {
        MediaType mt = valueOf("application/atom+xml;type=entry");
        assertEquals("entry", mt.getParameters().getFirstValue("type"));

        mt = valueOf("multipart/x-mixed-replace; boundary=\"My boundary\"");
        assertEquals("\"My boundary\"", mt.getParameters().getFirstValue("boundary"));
    }

    /** Equality tests. */
    @Test
    void testEquals() {
        MediaType mt1 = new MediaType("application/xml");
        MediaType mt2 = APPLICATION_XML;
        assertEquals(mt1, mt2);
        assertEquals(mt1, mt2);

        final Series<Parameter> mediaParams1 = new Form();
        mediaParams1.add(new Parameter("charset", "ISO-8859-1"));
        final MediaType mt1Bis = new MediaType("application/xml", mediaParams1);

        final Series<Parameter> mediaParams2 = new Form();
        mediaParams2.add(new Parameter("charset", "ISO-8859-1"));
        final MediaType mt2Bis = new MediaType("application/xml", mediaParams2);

        final Series<Parameter> mediaParams3 = new Form();
        mediaParams3.add(new Parameter("charset", "ISO-8859-15"));
        final MediaType mt3 = new MediaType("application/xml", mediaParams3);

        assertEquals(mt1Bis, mt2Bis);
        assertEquals(mt1, mt2);
        assertTrue(mt1Bis.equals(mt1, true));
        assertTrue(mt1Bis.equals(mt2, true));
        assertTrue(mt1Bis.equals(mt3, true));

        mt1 = new MediaType("application/*");
        mt2 = APPLICATION_ALL;
        assertEquals(mt1, mt2);
        assertEquals(mt1, mt2);
    }

    /** Test inclusion. */
    @Nested
    class Inclusion {

        @ParameterizedTest
        @MethodSource("inclusionTestCases")
        void shouldInclude(MediaType mt, MediaType included) {
            assertTrue(mt.includes(included));
        }

        static Stream<Arguments> inclusionTestCases() {
            return Stream.of(
                    Arguments.of(APPLICATION_ALL, APPLICATION_ALL),
                    Arguments.of(APPLICATION_XML, APPLICATION_XML),
                    Arguments.of(APPLICATION_ALL, APPLICATION_XML),
                    Arguments.of(APPLICATION_ALL_XML, APPLICATION_ALL_XML),
                    Arguments.of(APPLICATION_XML, APPLICATION_XML),
                    Arguments.of(APPLICATION_ALL_XML, APPLICATION_XML),
                    Arguments.of(APPLICATION_ATOMPUB_SERVICE, APPLICATION_ATOMPUB_SERVICE),
                    Arguments.of(APPLICATION_ALL_XML, APPLICATION_ATOMPUB_SERVICE));
        }

        @ParameterizedTest
        @MethodSource("noInclusionTestCases")
        void shouldNotInclude(MediaType mt, MediaType notIncluded) {
            assertFalse(mt.includes(notIncluded));
        }

        static Stream<Arguments> noInclusionTestCases() {
            return Stream.of(
                    Arguments.of(APPLICATION_XML, APPLICATION_ALL),
                    Arguments.of(APPLICATION_XML, APPLICATION_ALL_XML),
                    Arguments.of(APPLICATION_ATOMPUB_SERVICE, APPLICATION_ALL_XML),
                    Arguments.of(IMAGE_ALL, APPLICATION_OCTET_STREAM),
                    Arguments.of(APPLICATION_OCTET_STREAM, IMAGE_ALL),
                    Arguments.of(IMAGE_ALL, null));
        }

        /**
         * test inclusion for media types with parameters. The rule is: media type A includes media
         * type B iff for each parameter name/value pair in A, B contains the same parameter
         * name/value pair
         */
        @Test
        void testIncludes() {
            // set up test data

            MediaType typeWithNoParams = new MediaType("application/sometype");

            Series<Parameter> singleParam = new Series<>(Parameter.class);
            singleParam.add(new Parameter("name1", "value1"));
            MediaType typeWithSingleParam = new MediaType("application/sometype", singleParam);

            Series<Parameter> singleMatchingParam = new Series<>(Parameter.class);
            singleMatchingParam.add(new Parameter("name1", "value1"));
            MediaType typeWithSingleMatchingParam =
                    new MediaType("application/sometype", singleMatchingParam);

            Series<Parameter> singleNonMatchingParamValue = new Series<>(Parameter.class);
            singleNonMatchingParamValue.add(new Parameter("name1", "value2"));
            MediaType typeWithSingleNonMatchingParamValue =
                    new MediaType("application/sometype", singleNonMatchingParamValue);

            Series<Parameter> singleNonMatchingParamName = new Series<>(Parameter.class);
            singleNonMatchingParamName.add(new Parameter("name2", "value2"));
            MediaType typeWithSingleNonMatchingParamName =
                    new MediaType("application/sometype", singleNonMatchingParamName);

            Series<Parameter> twoParamsOneMatches = new Series<>(Parameter.class);
            twoParamsOneMatches.add(new Parameter("name1", "value1"));
            twoParamsOneMatches.add(new Parameter("name2", "value2"));
            MediaType typeWithTwoParamsOneMatches =
                    new MediaType("application/sometype", twoParamsOneMatches);

            // SCENARIO 1: test whether type with no params includes type with one
            // param

            assertTrue(typeWithNoParams.includes(typeWithSingleParam, true));
            assertTrue(typeWithNoParams.includes(typeWithSingleParam, false));

            // SCENARIO 2: test whether type with one param includes type with no
            // params

            assertTrue(typeWithSingleParam.includes(typeWithNoParams, true));
            assertFalse(typeWithSingleParam.includes(typeWithNoParams, false));

            // SCENARIO 3: test whether type with single param includes type with
            // matching single param.
            // Note that this is distinct from testing whether a type includes
            // itself, as there is a special check for that.
            assertTrue(typeWithSingleParam.includes(typeWithSingleMatchingParam, true));
            assertTrue(typeWithSingleParam.includes(typeWithSingleMatchingParam, false));

            // SCENARIO 4: test whether type with single param includes type with
            // single param having different name
            assertTrue(typeWithSingleParam.includes(typeWithSingleNonMatchingParamName, true));
            assertFalse(typeWithSingleParam.includes(typeWithSingleNonMatchingParamName, false));

            // SCENARIO 5: test whether type with single param includes type with
            // single param having the same name but different value
            assertTrue(typeWithSingleParam.includes(typeWithSingleNonMatchingParamValue, true));
            assertFalse(typeWithSingleParam.includes(typeWithSingleNonMatchingParamValue, false));

            // SCENARIO 6: test whether type with single param includes type with
            // two params, one matching
            assertTrue(typeWithSingleParam.includes(typeWithTwoParamsOneMatches, true));
            assertTrue(typeWithSingleParam.includes(typeWithTwoParamsOneMatches, false));

            // SCENARIO 7: test whether type with two params includes type with
            // single matching param
            assertTrue(typeWithTwoParamsOneMatches.includes(typeWithSingleParam, true));
            assertFalse(typeWithTwoParamsOneMatches.includes(typeWithSingleParam, false));
        }
    }

    @Test
    void testMostSpecificMediaType() {
        assertEquals(TEXT_ALL, getMostSpecific(ALL, TEXT_ALL));
        assertEquals(TEXT_ALL, getMostSpecific(TEXT_ALL, ALL));

        assertEquals(TEXT_PLAIN, getMostSpecific(ALL, TEXT_ALL, TEXT_PLAIN));
        assertEquals(TEXT_PLAIN, getMostSpecific(ALL, TEXT_PLAIN, TEXT_ALL));
        assertEquals(TEXT_PLAIN, getMostSpecific(TEXT_ALL, ALL, TEXT_PLAIN));
        assertEquals(TEXT_PLAIN, getMostSpecific(TEXT_ALL, TEXT_PLAIN, ALL));
        assertEquals(TEXT_PLAIN, getMostSpecific(TEXT_PLAIN, ALL, TEXT_ALL));
        assertEquals(TEXT_PLAIN, getMostSpecific(TEXT_PLAIN, TEXT_ALL, ALL));
    }

    /** Makes sure that 'abstract' types are properly initialised. */
    @ParameterizedTest
    @CsvSource({
        "'',*,*,false",
        "'  ',*,*,false",
        "*/,*,*,false",
        "*/  ,*,*,false",
        " * /,*,*,false",
        "/*,*,*,false",
        "  /*,*,*,false",
        "/ * ,*,*,false",
        "  / * ,*,*,false",
        "*/*,*,*,false",
        " * /*,*,*,false",
        "*/ * ,*,*,false",
        " * / * ,*,*,false",
        "/xml,*,xml,false",
        "/ xml ,*,xml,false",
        "  /xml,*,xml,false",
        "  / xml ,*,xml,false",
        "*/xml,*,xml,false",
        " * /xml,*,xml,false",
        "*/ xml ,*,xml,false",
        " * / xml ,*,xml,false",
        "application,application,*,false",
        " application ,application,*,false",
        "application/,application,*,false",
        " application /,application,*,false",
        " application /  ,application,*,false",
        "application/*,application,*,false",
        " application /*,application,*,false",
        "application/ * ,application,*,false",
        " application /*,application,*,false"
    })
    void testNotConcrete(
            String name, String expectedMain, String expectedSub, boolean expectedConcrete) {
        assertMediaType(name, expectedMain, expectedSub, expectedConcrete);
    }

    /** Test references that are unequal. */
    @Test
    void testUnEquals() {
        MediaType mt1 = new MediaType("application/xml");
        MediaType mt2 = new MediaType("application/xml2");
        assertNotEquals(mt1, mt2);

        final Series<Parameter> mediaParams1 = new Form();
        mediaParams1.add(new Parameter("charset", "ISO-8859-1"));
        final MediaType mt1Bis = new MediaType("application/xml", mediaParams1);

        final Series<Parameter> mediaParams3 = new Form();
        mediaParams3.add(new Parameter("charset", "ISO-8859-15"));
        final MediaType mt3 = new MediaType("application/xml", mediaParams3);

        assertNotEquals(mt1Bis, mt1);
        assertNotEquals(mt1Bis, mt3);

        mt1 = new MediaType("application/1");
        mt2 = APPLICATION_ALL;
        assertNotEquals(mt1, mt2);
    }

    /** Testing {@link MediaType#valueOf(String)} and {@link MediaType#register(String, String)} */
    @Test
    void testValueOf() {
        assertSame(APPLICATION_XML, valueOf("application/xml"));
        assertSame(ALL, valueOf("*/*"));
        final MediaType newType = valueOf("application/x-restlet-test");
        assertEquals("application", newType.getMainType());
        assertEquals("x-restlet-test", newType.getSubType());
        assertEquals("application/x-restlet-test", newType.getName());

        // Should not have got registered by call to valueOf() alone
        assertNotSame(newType, valueOf("application/x-restlet-test"));

        final MediaType registeredType = register("application/x-restlet-test", "Restlet testcase");
        assertNotSame(newType, registeredType); // didn't touch old value
        assertEquals("application/x-restlet-test", registeredType.getName());
        assertEquals("Restlet testcase", registeredType.getDescription());

        // Later valueOf calls always returns the registered type
        assertSame(registeredType, valueOf("application/x-restlet-test"));
        assertSame(registeredType, valueOf("application/x-restlet-test"));

        // Test toString() equivalence
        MediaType mediaType = valueOf("application/atom+xml; name=value");
        assertEquals("application/atom+xml; name=value", mediaType.toString());
        assertEquals(APPLICATION_ATOM, mediaType.getParent());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUnmodifiable() {
        final Form form = new Form();
        form.add("name1", "value1");

        final Series<Parameter> unmodifiableForm =
                (Series<Parameter>) Series.unmodifiableSeries(form);

        assertThrows(
                UnsupportedOperationException.class, () -> unmodifiableForm.add("name2", "value2"));
    }
}
