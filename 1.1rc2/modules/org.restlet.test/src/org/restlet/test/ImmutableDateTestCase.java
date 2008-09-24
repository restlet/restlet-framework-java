/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.restlet.util.DateUtils;

/**
 * Test {@link org.restlet.util.DateUtils}.
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
