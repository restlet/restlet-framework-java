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
package org.restlet.ext.jaxrs.internal.todo;

/**
 * Here are notices for the implementation.
 * 
 * @author Stephan Koops
 */
public class Notizen {

    // TESTEN what happens, if ".." or "." in @Path?
    
    // TESTEN what happens, if e.g. a JaX-RS MediaType is given to the headers
    // in a JAX-RS Response?

    // REQUESTED rename @DefaultValue to @Default? it's shorter

    // LATER test again Jersey Implementation.

    // NICE Provider for File-Upload by a browser (multipart/form-data)

    // NICE warn if primitve on @*Param: perhaps null?

    // NICE look for warnings in tests and put them away.

    // NICE When writing responses, implementations SHOULD respect
    // application-supplied character set metadata and SHOULD use UTF-8 if a
    // character set is not specified by the application or if the application
    // specifies a character set that is unsupported.

    // NICE JaxRsComponent
    // http://restlet.tigris.org/issues/show_bug.cgi?id=464#desc17
    // http://restlet.tigris.org/issues/show_bug.cgi?id=464#desc19

    // NICE Entity-Provider for org.restlet.data.Representation
    // NICE ResourceException-ExceptionMapper as WebAppExcMapper ?
}