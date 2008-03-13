/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.todo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.restlet.util.Series;

/**
 * Here are notices for the implementation.
 */
public class Notizen {
    // LATER An implementation MUST allow other runtime exceptions to propagate
    // to the underlying container. This allows existing container facilities
    // (e.g. a Servlet filter) to be used to handle the error if desired.

    // LATER alle LATERs in Masterarbeit uebernehmen

    // TODO look for warnings in tests and put them away.
    // gehört. Gibt es dann ne Endlosrekursion?

    // TODO inject ContextResolver and MessageBodyWorkers into providers.

    // TODO When writing responses, implementations SHOULD respect
    // application-supplied character set metadata and SHOULD use UTF-8 if a
    // character set is not specified by the application or if the application
    // specifies a character set that is unsupported.

    // TODO @Context: ClientInfo und Coditions
    
    // LATER Constructor-Nutzung ist inkompatibel 

    // private List<Integer> x;

    public static void main(String[] args) throws Exception {
        Field field = Notizen.class.getDeclaredField("x");
        System.out.println(field.getType());
        System.out.println(field.getType().isAssignableFrom(Series.class));
        Type t = field.getGenericType();
        if (t instanceof ParameterizedType) {
            System.out.println(((ParameterizedType)t).getActualTypeArguments()[0]);
        } else {
            "".toString();
        }
    }
}