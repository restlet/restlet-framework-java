/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.engine.util;

import org.restlet.data.Method;

/**
 * 
 * @author Jerome Louvel
 */
public class AnnotationInfo {
    private Method restletMethod;

    private java.lang.reflect.Method javaMethod;

    private String value;

    public AnnotationInfo(Method restletMethod,
            java.lang.reflect.Method javaMethod, String value) {
        super();
        this.restletMethod = restletMethod;
        this.javaMethod = javaMethod;
        this.value = value;
    }

    public java.lang.reflect.Method getJavaMethod() {
        return javaMethod;
    }

    public Method getRestletMethod() {
        return restletMethod;
    }

    public String getValue() {
        return value;
    }

    public void setJavaMethod(java.lang.reflect.Method javaMethod) {
        this.javaMethod = javaMethod;
    }

    public void setRestletMethod(Method restletMethod) {
        this.restletMethod = restletMethod;
    }

    public void setValue(String value) {
        this.value = value;
    }
}