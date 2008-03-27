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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.ext.jaxrs.internal.wrappers.ContextResolver;
import org.restlet.ext.jaxrs.internal.provider.ReturnNullContextResolver;

/**
 * @author Stephan Koops
 */
// TODO move from AbstractJaxRsWrapper to WrapperUtil.
class WrapperUtil {

    static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    /**
     * Creates the {@link ContextResolver} to inject in the given field.
     * 
     * @param field
     * @param allResolvers
     * @return
     */
    @SuppressWarnings("unchecked")
    static javax.ws.rs.ext.ContextResolver<?> getContextResolver(Field field,
            Collection<ContextResolver<?>> allResolvers) {
        Type genType = field.getGenericType();
        if (!(genType instanceof ParameterizedType))
            return ReturnNullContextResolver.get();
        Type t = ((ParameterizedType) genType).getActualTypeArguments()[0];
        if (!(t instanceof Class))
            return ReturnNullContextResolver.get();
        Class crType = (Class) t;
        List<javax.ws.rs.ext.ContextResolver<?>> returnResolvers = new ArrayList<javax.ws.rs.ext.ContextResolver<?>>();
        for (ContextResolver<?> cr : allResolvers) {
            javax.ws.rs.ext.ContextResolver<?> jaxRsResolver;
            jaxRsResolver = cr.getJaxRsContextResolver();
            Class<?> crClaz = jaxRsResolver.getClass();
            try {
                Method getContext = crClaz.getMethod("getContext", Class.class);
                if (getContext.getReturnType().equals(crType))
                    returnResolvers.add(jaxRsResolver);
            } catch (SecurityException e) {
                throw new RuntimeException(
                        "sorry, the method getContext(Class) of ContextResolver "
                                + crClaz + " is not accessible");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        "The ContextResolver "
                                + crClaz
                                + " is not valid, because it has no method getContext(Class)");
            }
        }
        if (returnResolvers.isEmpty())
            return ReturnNullContextResolver.get();
        if (returnResolvers.size() == 1)
            return returnResolvers.get(0);
        return new ContextResolverCollection(returnResolvers);
    }
}