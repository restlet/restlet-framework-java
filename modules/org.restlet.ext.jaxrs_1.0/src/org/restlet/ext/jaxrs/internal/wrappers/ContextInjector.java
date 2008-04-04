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

import static org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil.getContextResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWorkers;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Helper class to inject into fields annotated with &#64;{@link Context}.
 * 
 * @author Stephan Koops
 * @see IntoRrcInjector
 */
public class ContextInjector {

    protected static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    // TODO bean setter for @Context (look for details)
    
    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link ContextResolver}.
     * </p>
     * <p>
     * Must be initiated with the other fields starting with initFields.
     * </p>
     * 
     * @see ContextResolver
     */
    private Field[] injectFieldsContextResolvers;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link MessageBodyWorkers}.
     * </p>
     * <p>
     * Must be initiated with the other fields starting with initFields.
     * </p>
     * 
     * @see MessageBodyWorkers
     */
    private Field[] injectFieldsMbWorkers;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext} .<br>
     * (Array is round about 10 times faster than the list.)
     * </p>
     * <p>
     * Must be initiated with the other fields starting with initFields.
     * </p>
     * 
     * @see UriInfo
     * @see SecurityContext
     * @see Request
     * @see HttpHeaders
     */
    private Field[] injectFieldsCallContext;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext} .
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsClientInfo;

    /**
     * <p>
     * This array contains the fields in this class in which are annotated to
     * inject an {@link CallContext} .
     * </p>
     * <p>
     * Must bei initiated with the other fields starting with initFields.
     * </p>
     */
    private Field[] injectFieldsConditions;

    /**
     * @param jaxRsClass
     * @throws ImplementationException
     */
    public ContextInjector(Class<?> jaxRsClass) {
        init(jaxRsClass);
    }

    /**
     * initiates the fields to cache the fields that needs injection.
     */
    private void init(Class<?> jaxRsClass) {
        List<Field> ifMbWorkers = new ArrayList<Field>(1);
        List<Field> ifContRs = new ArrayList<Field>(1);
        List<Field> ifContext = new ArrayList<Field>(3);
        List<Field> ifClientInfo = new ArrayList<Field>(1);
        List<Field> ifConditions = new ArrayList<Field>(1);
        do {
            for (Field field : jaxRsClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Context.class)) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.equals(ClientInfo.class))
                        ifClientInfo.add(field);
                    else if (fieldType.equals(Conditions.class))
                        ifConditions.add(field);
                    else if (fieldType.equals(MessageBodyWorkers.class))
                        ifMbWorkers.add(field);
                    else if (fieldType.equals(ContextResolver.class))
                        ifContRs.add(field);
                    else if (fieldType.equals(SecurityContext.class)
                            || fieldType.equals(UriInfo.class)
                            || fieldType.equals(HttpHeaders.class)
                            || fieldType.equals(Request.class))
                        ifContext.add(field);
                }
            }
            jaxRsClass = jaxRsClass.getSuperclass();
        } while (jaxRsClass != null);

        this.injectFieldsMbWorkers = ifMbWorkers.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsContextResolvers = ifContRs.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsCallContext = ifContext.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsClientInfo = ifClientInfo.toArray(EMPTY_FIELD_ARRAY);
        this.injectFieldsConditions = ifConditions.toArray(EMPTY_FIELD_ARRAY);
    }

    /**
     * Injects all the supported dependencies into the the given resource object
     * of this class.
     * 
     * @param jaxRsResObj
     * @param callContext
     *                The CallContext to get the dependencies from.
     * @param allResolvers
     *                all available wrapped {@link ContextResolver}s.
     * @param messageBodyWorkers
     *                the {@link MessageBodyReader}s and
     *                {@link MessageBodyWriter}s.
     * @throws InjectException
     *                 if the injection was not possible. See
     *                 {@link InjectException#getCause()} for the reason.
     */
    public void inject(
            Object jaxRsResObj,
            CallContext callContext,
            Collection<org.restlet.ext.jaxrs.internal.wrappers.provider.ContextResolver<?>> allResolvers,
            MessageBodyWorkers messageBodyWorkers) throws InjectException {
        for (Field field : this.injectFieldsContextResolvers) {
            ContextResolver<?> contextResolver;
            contextResolver = getContextResolver(field, allResolvers);
            Util.inject(jaxRsResObj, field, contextResolver);
        }
        for (Field mbwField : this.injectFieldsMbWorkers) {
            Util.inject(jaxRsResObj, mbwField, messageBodyWorkers);
        }
        for (Field contextField : this.injectFieldsCallContext) {
            if (callContext == null)
                throw new NotYetImplementedException(
                        "Sorry, @CallContext on providers is only allowed for ContextResolvers and MessageBodyWorkers");
            Util.inject(jaxRsResObj, contextField, callContext);
        }
        for (Field clientInfoField : this.injectFieldsClientInfo) {
            if (callContext == null) // TODO ThreadLocal CallContext
                throw new NotYetImplementedException(
                        "Sorry, @CallContext on providers is only allowed for ContextResolvers and MessageBodyWorkers");
            ClientInfo clientInfo = callContext.getRequest().getClientInfo();
            Util.inject(jaxRsResObj, clientInfoField, clientInfo);
        }
        for (Field conditionsField : this.injectFieldsConditions) {
            if (callContext == null)
                throw new NotYetImplementedException(
                        "Sorry, @CallContext on providers is only allowed for ContextResolvers and MessageBodyWorkers");
            Conditions conditions = callContext.getRequest().getConditions();
            Util.inject(jaxRsResObj, conditionsField, conditions);
        }
    }
}