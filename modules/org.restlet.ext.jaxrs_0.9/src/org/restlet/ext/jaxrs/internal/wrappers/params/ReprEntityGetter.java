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
package org.restlet.ext.jaxrs.internal.wrappers.params;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import org.restlet.data.Request;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.InjectObjectGetter;
import org.restlet.resource.Representation;

/**
 * An {@link EntityGetter} for Restlet {@link Representation}s.
 * 
 * @author Stephan Koops
 */
public abstract class ReprEntityGetter implements InjectObjectGetter {

    /**
     * EntityGetter, if there is a constructor for only the entity.
     */
    static class ReprOnlyEntityGetter extends ReprEntityGetter {

        /**
         * @param constructor
         */
        ReprOnlyEntityGetter(Constructor<?> constructor) {
            super(constructor);
        }

        @Override
        Representation createInstance(Representation entity)
                throws IllegalArgumentException, InstantiationException,
                IllegalAccessException, InvocationTargetException {
            return constr.newInstance(entity);
        }

    }

    /**
     * EntityGetter, if there is a constructor with two arguments: Class and
     * Representation
     */
    static class ClassReprEntityGetter extends ReprEntityGetter {
        private Class<?> clazz;

        ClassReprEntityGetter(Class<?> genClass, Constructor<?> constructor) {
            super(constructor);
            this.clazz = genClass;
        }

        @Override
        Representation createInstance(Representation entity)
                throws IllegalArgumentException, InstantiationException,
                IllegalAccessException, InvocationTargetException {
            return constr.newInstance(clazz, entity);
        }

    }

    /**
     * EntityGetter, if Representation could directly be injected
     */
    static class DirectReprEntityGetter implements InjectObjectGetter {

        /**
         * @see org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.InjectObjectGetter#getValue()
         */
        public Object getValue() throws InvocationTargetException,
                ConvertRepresentationException, WebApplicationException {
            return Request.getCurrent().getEntity();
        }

    }

    /**
     * EntityGetter, if there is a constructor with two arguments:
     * Representation and Class
     */
    static class ReprClassEntityGetter extends ReprEntityGetter {

        private Class<?> clazz;

        ReprClassEntityGetter(Constructor<?> constructor, Class<?> genClass) {
            super(constructor);
            this.clazz = genClass;
        }

        @Override
        Representation createInstance(Representation entity)
                throws IllegalArgumentException, InstantiationException,
                IllegalAccessException, InvocationTargetException {
            return constr.newInstance(entity, clazz);
        }

    }

    /**
     * Creates an EntityGetter for the given values, or null, if it is not
     * available.
     * 
     * @param representationType
     * @param convToGen
     * @param logger
     * @return
     */
    public static InjectObjectGetter create(Class<?> representationType,
            Type convToGen, Logger logger) {
        if (representationType.equals(Representation.class))
            return new DirectReprEntityGetter();
        try {
            return new ReprOnlyEntityGetter(representationType
                    .getConstructor(Representation.class));
        } catch (SecurityException e) {
            logger.warning("The constructor " + representationType
                    + "(Representation) is not accessable.");
        } catch (NoSuchMethodException e) {
            // try next
        }
        if (!(convToGen instanceof ParameterizedType))
            return null;
        ParameterizedType pType = (ParameterizedType) convToGen;
        Type[] typeArgs = pType.getActualTypeArguments();
        if (typeArgs.length != 1)
            return null;
        Type typeArg = typeArgs[0];
        if (!(typeArg instanceof Class))
            return null;
        Class<?> genClass = (Class<?>) typeArg;
        try {
            return new ReprClassEntityGetter(representationType.getConstructor(
                    Representation.class, Class.class), genClass);
        } catch (SecurityException e) {
            logger.warning("The constructor " + representationType
                    + "(Representation) is not accessable.");
        } catch (NoSuchMethodException e) {
            // try next
        }
        try {
            return new ClassReprEntityGetter(genClass, representationType
                    .getConstructor(Class.class, Representation.class));
        } catch (SecurityException e) {
            logger.warning("The constructor " + representationType
                    + "(Representation) is not accessable.");
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    Constructor<? extends Representation> constr;

    @SuppressWarnings("unchecked")
    ReprEntityGetter(Constructor<?> constr) {
        this.constr = (Constructor) constr;
    }

    abstract Representation createInstance(Representation entity)
            throws IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException;

    /**
     * @return the class of the {@link Representation}.
     */
    private Class<? extends Representation> getReprClass() {
        return this.constr.getDeclaringClass();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList.InjectObjectGetter#getValue()
     */
    public Object getValue() throws InvocationTargetException,
            ConvertRepresentationException, WebApplicationException {
        try {
            Representation entity = Request.getCurrent().getEntity();
            if(entity == null)
                return null;
            return createInstance(entity);
        } catch (IllegalArgumentException e) {
            throw ConvertRepresentationException.object(getReprClass(),
                    "the message body", e);
        } catch (InstantiationException e) {
            throw ConvertRepresentationException.object(getReprClass(),
                    "the message body", e);
        } catch (IllegalAccessException e) {
            throw ConvertRepresentationException.object(getReprClass(),
                    "the message body", e);
        }
    }
}