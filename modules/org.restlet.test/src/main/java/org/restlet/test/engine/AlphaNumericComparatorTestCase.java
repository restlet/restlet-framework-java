/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.restlet.data.Reference;
import org.restlet.engine.util.AlphaNumericComparator;
import org.restlet.resource.Directory;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the alphanum algorithm used by {@link Directory}.
 * 
 * @author Davide Angelocola
 */
public class AlphaNumericComparatorTestCase extends RestletTestCase {

    private static List<Reference> refs(String... uris) {
        List<Reference> result = new LinkedList<Reference>();

        for (String uri : uris) {
            result.add(new Reference(uri));
        }

        return result;
    }

    private static List<Reference> unsorted = refs("1", "2", "3", "1.0", "1.1",
            "1.1.1", "2.0", "2.2", "2.2.2", "3.0", "3.3");

    private static List<Reference> expected = refs("1", "1.0", "1.1", "1.1.1",
            "2", "2.0", "2.2", "2.2.2", "3", "3.0", "3.3");

    public void testBug() {
        List<Reference> result = new ArrayList<Reference>(unsorted);
        Collections.sort(result, new AlphaNumericComparator());
        Assert.assertEquals(expected, result);
    }

    public void test02() throws Exception {
        AlphaNumericComparator anc = new AlphaNumericComparator();
        System.out.println(anc.compare("Intel 5000X", "Intel 5500"));
        System.out.println(anc.compare("66", "3"));
        System.out.println(anc.compare("200", "66"));
        System.out.println(anc.compare("18", "2"));
    }

}
