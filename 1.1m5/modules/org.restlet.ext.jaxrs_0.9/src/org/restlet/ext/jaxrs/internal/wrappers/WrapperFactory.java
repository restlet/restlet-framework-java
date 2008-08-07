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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

/**
 * A WrapperFactory creates and caches some of the wrapper objects.
 * 
 * @author Stephan Koops
 */
public class WrapperFactory {

    private final JaxRsProviders jaxRsProviders;

    private final ExtensionBackwardMapping extensionBackwardMapping;

    private final Logger logger;

    private final Map<Class<?>, ResourceClass> resourceClasses = new HashMap<Class<?>, ResourceClass>();

    private final ThreadLocalizedContext tlContext;

    /**
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                the to log warnings and so on
     */
    public WrapperFactory(ThreadLocalizedContext tlContext,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger) {
        this.tlContext = tlContext;
        this.jaxRsProviders = jaxRsProviders;
        this.extensionBackwardMapping = extensionBackwardMapping;
        this.logger = logger;
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
                    this.jaxRsProviders, this.extensionBackwardMapping,
                    this.logger);
            synchronized (this.resourceClasses) {
                this.resourceClasses.put(jaxRsResourceClass, rc);
            }
        }
        return rc;
    }

    /**
     * Creates a new JAX-RS root resource class wrapper.
     * 
     * @param jaxRsRootResourceClass
     * @return the wrapped root resource class.
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     *                 if no valid constructor could be found.
     * @throws IllegalBeanSetterTypeException
     * @throws IllegalFieldTypeException
     * @throws IllegalConstrParamTypeException
     * @throws IllegalPathParamTypeException
     */
    public RootResourceClass getRootResourceClass(
            Class<?> jaxRsRootResourceClass) throws IllegalArgumentException,
            MissingAnnotationException, IllegalPathOnClassException,
            MissingConstructorException, IllegalConstrParamTypeException,
            IllegalFieldTypeException, IllegalBeanSetterTypeException,
            IllegalPathParamTypeException {
        return new RootResourceClass(jaxRsRootResourceClass, this.tlContext,
                this.jaxRsProviders, this.extensionBackwardMapping, this.logger);
    }
}