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

/**
 * <p>
 * This package contains the default providers for this JAX-RS runtime
 * environment and other classes belonging to the providers.
 * </p>
 * <p>
 * This extension as well as the JAX-RS specification are currently under
 * development. You should only use this extension for experimental purpose.
 * </p>
 * 
 * @see MessageBodyWriter
 * @see MessageBodyReader
 * @author Stephan Koops
 */
package org.restlet.ext.jaxrs.internal.provider;

import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.MessageBodyReader;

// TODO JsonProvider for JAXB and all providers supporting XML.