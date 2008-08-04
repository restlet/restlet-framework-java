/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.ext.jaxrs.internal.exceptions;

/**
 * This exception is thrown, if a root resource class or a provider has no valid
 * contructor
 * 
 * @author Stephan Koops
 */
public class MissingConstructorException extends JaxRsException {

    private static final long serialVersionUID = 8213720039895185212L;

    /**
     * @param jaxRsClass
     *            the root resource or provider class.
     * @param rrcOrProvider
     *            "root resource class" or "provider"
     */
    public MissingConstructorException(Class<?> jaxRsClass, String rrcOrProvider) {
        super("the " + rrcOrProvider + " " + jaxRsClass.getName()
                + " has no valid constructor");
    }
}