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
package org.restlet.ext.jaxrs.internal.exceptions;

/**
 * A RequestHandledException is thrown when an this request is already handled,
 * for example because of an handled exception resulting in an error while
 * method invocation. The Exception or whatever was handled and the necessary
 * data in {@link org.restlet.data.Response} were set, so that the JaxRsRouter
 * must not do anything. <br>
 * This Exception only indicates this.
 * 
 * @author Stephan Koops
 */
public class RequestHandledException extends Exception {
    private static final long serialVersionUID = 2765454873472711005L;
}