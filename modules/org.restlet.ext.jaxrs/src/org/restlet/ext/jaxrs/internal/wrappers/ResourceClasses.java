/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

/**
 * A ResourceClasses creates and caches some of the wrapper objects.
 * 
 * @author Stephan Koops
 */
public class ResourceClasses {

    private final ExtensionBackwardMapping extensionBackwardMapping;

    private final JaxRsProviders jaxRsProviders;

    private final Logger logger;

    private final Map<Class<?>, ResourceClass> resourceClasses = new HashMap<Class<?>, ResourceClass>();

    /**
     * This set must only changed by adding a root resource class to this
     * JaxRsRestlet.
     */
    private final Set<RootResourceClass> rootResourceClasses = new CopyOnWriteArraySet<RootResourceClass>();

    private final ThreadLocalizedContext tlContext;

    /**
     * @param tlContext
     *            the {@link ThreadLocalizedContext} of the
     *            {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     *            the extension backward mapping
     * @param logger
     */
    public ResourceClasses(ThreadLocalizedContext tlContext,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger) {
        this.tlContext = tlContext;
        this.jaxRsProviders = jaxRsProviders;
        this.extensionBackwardMapping = extensionBackwardMapping;
        this.logger = logger;
    }

    /**
     * @param rootResourceClass
     * @return true, if the root resource class could be added, or false if not.
     */
    public boolean addRootClass(Class<?> rootResourceClass) {
        RootResourceClass newRrc;
        try {
            newRrc = this.getPerRequestRootClassWrapper(rootResourceClass);
        } catch (IllegalParamTypeException e) {
            String msg = "Ignore provider " + rootResourceClass.getName()
                    + ": Could not instantiate class "
                    + rootResourceClass.getName();
            this.logger.log(Level.WARNING, msg, e);
            return false;
        } catch (IllegalPathOnClassException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " is annotated with an illegal path: " + e.getPath()
                    + ". (" + e.getMessage() + ")");
            return false;
        } catch (IllegalArgumentException e) {
            this.logger.log(Level.WARNING, "The root resource class "
                    + rootResourceClass.getName()
                    + " is not a valud root resource class: " + e.getMessage(),
                    e);
            return false;
        } catch (MissingAnnotationException e) {
            this.logger.log(Level.WARNING, "The root resource class "
                    + rootResourceClass.getName()
                    + " is not a valud root resource class: " + e.getMessage(),
                    e);
            return false;
        } catch (MissingConstructorException e) {
            this.logger
                    .warning("The root resource class "
                            + rootResourceClass.getName()
                            + " has no valid constructor");
            return false;
        } catch (IllegalConstrParamTypeException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " has an invalid parameter: " + e.getMessage());
            return false;
        } catch (IllegalBeanSetterTypeException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " has an bean setter with an illegal type: "
                    + e.getMessage());
            return false;
        } catch (IllegalFieldTypeException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " has an illegal annotated and typed field: "
                    + e.getMessage());
            return false;
        }
        PathRegExp uriTempl = newRrc.getPathRegExp();
        for (RootResourceClass rrc : this.rootResourceClasses) {
            if (rrc.getJaxRsClass().equals(rootResourceClass)) {
                return true;
            }
            if (rrc.getPathRegExp().equals(uriTempl)) {
                this.logger
                        .warning("There is already a root resource class with path "
                                + uriTempl.getPathTemplateEnc());
                return false;
            }
        }
        rootResourceClasses.add(newRrc);
        return true;
    }

    /**
     * 
     * @param jaxRsRootObject
     * @return true, if the root resource class could be added, or false if not.
     */
    public boolean addRootSingleton(Object jaxRsRootObject) {
        Class<?> rootResourceClass = jaxRsRootObject.getClass();
        RootResourceClass newRrc;
        try {
            newRrc = this.getSingletonRootClassWrapper(jaxRsRootObject);
        } catch (IllegalParamTypeException e) {
            String msg = "Ignore provider " + rootResourceClass.getName()
                    + ": Could not instantiate class "
                    + rootResourceClass.getName();
            this.logger.log(Level.WARNING, msg, e);
            return false;
        } catch (IllegalPathOnClassException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " is annotated with an illegal path: " + e.getPath()
                    + ". (" + e.getMessage() + ")");
            return false;
        } catch (IllegalArgumentException e) {
            this.logger.log(Level.WARNING, "The root resource class "
                    + rootResourceClass.getName()
                    + " is not a valud root resource class: " + e.getMessage(),
                    e);
            return false;
        } catch (MissingAnnotationException e) {
            this.logger.log(Level.WARNING, "The root resource class "
                    + rootResourceClass.getName()
                    + " is not a valud root resource class: " + e.getMessage(),
                    e);
            return false;
        } catch (MissingConstructorException e) {
            this.logger
                    .warning("The root resource class "
                            + rootResourceClass.getName()
                            + " has no valid constructor");
            return false;
        } catch (IllegalConstrParamTypeException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " has an invalid parameter: " + e.getMessage());
            return false;
        } catch (IllegalBeanSetterTypeException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " has an bean setter with an illegal type: "
                    + e.getMessage());
            return false;
        } catch (IllegalFieldTypeException e) {
            this.logger.warning("The root resource class "
                    + rootResourceClass.getName()
                    + " has an illegal annotated and typed field: "
                    + e.getMessage());
            return false;
        } catch (InjectException e) {
            this.logger.warning("Dependencies could not be injected in "
                    + "root resource class " + rootResourceClass.getName()
                    + ": " + e.getMessage());
            return false;
        } catch (InvocationTargetException e) {
            this.logger.warning("Exception while calling bean setters in "
                    + "root resource class " + rootResourceClass.getName()
                    + ": " + e.getMessage());
            return false;
        }
        PathRegExp uriTempl = newRrc.getPathRegExp();
        for (RootResourceClass rrc : this.rootResourceClasses) {
            if (rrc.getJaxRsClass().equals(rootResourceClass)) {
                return true;
            }
            if (rrc.getPathRegExp().equals(uriTempl)) {
                this.logger
                        .warning("There is already a root resource class with path "
                                + uriTempl.getPathTemplateEnc());
                return false;
            }
        }
        rootResourceClasses.add(newRrc);
        return true;
    }

    /**
     * Creates a new JAX-RS root resource class wrapper.
     * 
     * @param jaxRsRootResourceClass
     * @return the wrapped root resource class.
     * @throws IllegalArgumentException
     *             if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *             if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     *             if no valid constructor could be found.
     * @throws IllegalBeanSetterTypeException
     * @throws IllegalFieldTypeException
     * @throws IllegalConstrParamTypeException
     * @throws IllegalPathParamTypeException
     */
    private RootResourceClass getPerRequestRootClassWrapper(
            Class<?> jaxRsRootResourceClass) throws IllegalArgumentException,
            MissingAnnotationException, IllegalPathOnClassException,
            MissingConstructorException, IllegalConstrParamTypeException,
            IllegalFieldTypeException, IllegalBeanSetterTypeException,
            IllegalPathParamTypeException {
        return new PerRequestRootResourceClass(jaxRsRootResourceClass,
                this.tlContext, this.jaxRsProviders,
                this.extensionBackwardMapping, Context.getCurrentLogger());
    }

    /**
     * Creates a new JAX-RS resource class wrapper.
     * 
     * @param jaxRsResourceClass
     * @return
     * @throws MissingAnnotationException
     * @throws IllegalArgumentException
     */
    ResourceClass getResourceClass(Class<?> jaxRsResourceClass)
            throws IllegalArgumentException, MissingAnnotationException {
        ResourceClass rc;
        // NICE thread save Map access without synchronized?
        synchronized (this.resourceClasses) {
            rc = this.resourceClasses.get(jaxRsResourceClass);
        }
        if (rc == null) {
            rc = new ResourceClass(jaxRsResourceClass, this.tlContext,
                    this.jaxRsProviders, this.extensionBackwardMapping, Context
                            .getCurrentLogger());
            synchronized (this.resourceClasses) {
                this.resourceClasses.put(jaxRsResourceClass, rc);
            }
        }
        return rc;
    }

    /**
     * Creates a new JAX-RS root resource object wrapper.
     * 
     * @param jaxRsRootResourceClass
     * @return the wrapped root resource class.
     * @throws InvocationTargetException
     *             if an exception occurs while the injecting of dependencies.
     * @throws IllegalArgumentException
     *             if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *             if the class is not annotated with &#64;Path.
     * @throws MissingConstructorException
     *             if no valid constructor could be found.
     */
    private RootResourceClass getSingletonRootClassWrapper(
            Object jaxRsRootResourceObject) throws IllegalPathOnClassException,
            IllegalConstrParamTypeException, IllegalFieldTypeException,
            IllegalBeanSetterTypeException, IllegalPathParamTypeException,
            IllegalArgumentException, MissingAnnotationException,
            MissingConstructorException, InjectException,
            InvocationTargetException {
        // TEST singleton root resource class
        return new SingletonRootResourceClass(jaxRsRootResourceObject,
                this.tlContext, this.jaxRsProviders,
                this.extensionBackwardMapping, Context.getCurrentLogger());
    }

    /**
     * @return the wrapped root resource classes
     */
    public Iterable<RootResourceClass> roots() {
        return this.rootResourceClasses;
    }
}