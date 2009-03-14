/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.jaxrs.util;

import java.lang.reflect.Constructor;

import javax.ws.rs.Path;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.util.MatchingResult;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;

/**
 * @author Stephan Koops
 * @see PathRegExp
 */
public class PathRegExpTests extends TestCase {

    static final String ID1 = "id1";

    static final String ID2 = "id2";

    /** as {@link #PATH_PATTERN_2} but without "/" at end */
    static final String PATH_PATTERN_1 = "/abc/{" + ID1 + "}/shf/{" + ID2
            + "}/xyz";

    /** as {@link #PATH_PATTERN_1} but with "/" at end */
    static final String PATH_PATTERN_2 = PATH_PATTERN_1 + "/";

    /** as {@link #VALID_PATH_2} but without "/" at end */
    static final String VALID_PATH_1 = "/abc/25478/shf/12345/xyz";

    /** as {@link #VALID_PATH_2} but without "/" at end */
    static final RemainingPath VALID_PATH_1_RP = new RemainingPath(VALID_PATH_1);

    /** as {@link #VALID_PATH_1} but with "/" at end */
    public static final String VALID_PATH_2 = VALID_PATH_1 + "/";

    /** as {@link #VALID_PATH_1} but with "/" at end */
    public static final RemainingPath VALID_PATH_2_RP = new RemainingPath(
            VALID_PATH_2);

    private static final PathRegExp newPathRegExp(String pathPattern) {
        try {
            final Constructor<PathRegExp> constructor;
            final Class<PathRegExp> pathRegExpClass = PathRegExp.class;
            constructor = pathRegExpClass.getDeclaredConstructor(String.class,
                    Path.class);
            constructor.setAccessible(true);
            return constructor.newInstance(pathPattern, null);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final PathRegExp regExpMultipleSegments1 = newPathRegExp(PATH_PATTERN_1);

    private final PathRegExp regExpMultipleSegments2 = newPathRegExp(PATH_PATTERN_2);

    private final PathRegExp regExpOneSegment1 = newPathRegExp(PATH_PATTERN_1);

    private final PathRegExp regExpOneSegment2 = newPathRegExp(PATH_PATTERN_2);

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.util.PathRegExp#match(java.lang.String)} .
     */
    public void testMatchM1() {
        MatchingResult matchingResult = this.regExpMultipleSegments1
                .match(VALID_PATH_1_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(""), matchingResult
                .getFinalCapturingGroup());

        matchingResult = this.regExpMultipleSegments1.match(VALID_PATH_2_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(""), matchingResult
                .getFinalCapturingGroup());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.util.PathRegExp#match(java.lang.String)} .
     */
    public void testMatchM2() {
        MatchingResult matchingResult = this.regExpMultipleSegments2
                .match(VALID_PATH_1_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));

        matchingResult = this.regExpMultipleSegments2.match(VALID_PATH_2_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(""), matchingResult
                .getFinalCapturingGroup());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.util.PathRegExp#match(java.lang.String)} .
     */
    public void testMatchM3() {
        final String rest = "/jkgjg";
        tryWithRest(rest);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.util.PathRegExp#match(java.lang.String)} .
     */
    public void testMatchM4() {
        final String rest = "/qarear/iuguz/izu/";
        tryWithRest(rest);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.util.PathRegExp#match(java.lang.String)} .
     */
    public void testMatchO1() {
        final MatchingResult matchingResult = this.regExpOneSegment1
                .match(VALID_PATH_1_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(""), matchingResult
                .getFinalCapturingGroup());

        this.regExpOneSegment1.match(VALID_PATH_2_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(""), matchingResult
                .getFinalCapturingGroup());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.util.PathRegExp#match(java.lang.String)} .
     */
    public void testMatchO21() {
        MatchingResult matchingResult = this.regExpOneSegment2
                .match(VALID_PATH_1_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));

        matchingResult = this.regExpOneSegment2.match(VALID_PATH_2_RP);
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(""), matchingResult
                .getFinalCapturingGroup());
    }

    /**
     * @param rest
     */
    private void tryWithRest(final String rest) {
        final MatchingResult matchingResult = this.regExpMultipleSegments2
                .match(new RemainingPath(VALID_PATH_2 + rest));
        assertNotNull(matchingResult);
        assertEquals("25478", matchingResult.getVariables().get(ID1));
        assertEquals("12345", matchingResult.getVariables().get(ID2));
        assertEquals(new RemainingPath(rest), matchingResult
                .getFinalCapturingGroup());
    }
}