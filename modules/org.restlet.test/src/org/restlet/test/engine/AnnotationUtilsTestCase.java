/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.test.engine;

import java.util.List;

import junit.framework.Assert;

import org.restlet.data.Method;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.test.RestletTestCase;

/**
 * Test case for generic interfaces.
 * 
 * @author Valdis Rigdon
 */
public class AnnotationUtilsTestCase extends RestletTestCase {

    public static interface IChild extends IParent<String> {

    }

    public static interface IParent<T> {

        @Get
        T getType();

        @Put
        void update(T generic);

    }

    public void testGetAnnotationsWithGenericParameterType() {
        List<AnnotationInfo> infos = AnnotationUtils
                .getAnnotations(IChild.class);
        Assert.assertEquals("Wrong count: " + infos, 4, infos.size());
        boolean found = false;
        for (AnnotationInfo ai : infos) {
            if (ai.getResourceInterface().equals(IChild.class)
                    && ai.getRestletMethod().equals(Method.PUT)) {
                found = true;
                Assert.assertEquals(String.class, ai.getJavaInputTypes()[0]);
            }
        }
        Assert.assertEquals(
                "Didn't find a method with IChild as the declaring class.",
                true, found);
    }

    public void testGetAnnotationsWithGenericReturnType() {
        List<AnnotationInfo> infos = AnnotationUtils
                .getAnnotations(IChild.class);
        Assert.assertEquals("Wrong count: " + infos, 4, infos.size());
        boolean found = false;
        for (AnnotationInfo ai : infos) {
            if (ai.getResourceInterface().equals(IChild.class)
                    && ai.getRestletMethod().equals(Method.GET)) {
                found = true;
                Assert.assertEquals(String.class, ai.getJavaOutputType());
            }
        }
        Assert.assertEquals(
                "Didn't find a method with IChild as the declaring class.",
                true, found);
    }
}
