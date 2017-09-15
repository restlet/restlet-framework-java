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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.restlet.engine.util.DateUtils;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.engine.util.DateUtils}.
 * 
 * @author Thierry Boileau
 */
public class ImmutableDateTestCase extends RestletTestCase {

    public void test() {
        Date now = new Date();
        Calendar yesterdayCal = new GregorianCalendar();
        yesterdayCal.add(Calendar.DAY_OF_MONTH, -1);

        Date yesterday = yesterdayCal.getTime();

        assertTrue(now.after(yesterday));
        assertTrue(now.after(DateUtils.unmodifiable(yesterday)));
        assertTrue(DateUtils.unmodifiable(now).after(yesterday));
        assertTrue(DateUtils.unmodifiable(now).after(
                DateUtils.unmodifiable(yesterday)));

        assertTrue(yesterday.before(now));
        assertTrue(yesterday.before(DateUtils.unmodifiable(now)));
        assertTrue(DateUtils.unmodifiable(yesterday).before(
                DateUtils.unmodifiable(now)));
        assertTrue(DateUtils.unmodifiable(yesterday).before(now));
    }

}
