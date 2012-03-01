/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs.client;

import java.lang.annotation.Annotation;

import org.restlet.data.Method;
import org.restlet.engine.resource.AnnotationUtils;

/**
 * JAX-RS specific Utilities to manipulate Restlet annotations.
 * 
 * @author Shaun Elliott
 */
public class JaxRsAnnotationUtils extends AnnotationUtils {
    /** Current instance. */
    private static JaxRsAnnotationUtils instance = new JaxRsAnnotationUtils();

    /**
     * Returns the current instance.
     * 
     * @return The current instance.
     */
    public static JaxRsAnnotationUtils getInstance() {
        return instance;
    }

    private JaxRsAnnotationUtils() {
    }

    @Override
    protected Method getRestletMethod(Annotation annotation,
            Annotation methodAnnotation) {
        // try to get the method from the Annotation name itself, as JAX-RS
        // annotations map exactly to this
        Method restletMethod = Method.valueOf(annotation.annotationType()
                .getSimpleName());
        if (restletMethod == null) {
            super.getRestletMethod(annotation, methodAnnotation);
        }

        return restletMethod;
    }
}
