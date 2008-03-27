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
    // LATER An implementation MUST allow other runtime exceptions to propagate
    // to the underlying container. This allows existing container facilities
    // (e.g. a Servlet filter) to be used to handle the error if desired.

    // LATER alle LATERs in Masterarbeit uebernehmen

    // TODO look for warnings in tests and put them away.

    // TODO When writing responses, implementations SHOULD respect
    // application-supplied character set metadata and SHOULD use UTF-8 if a
    // character set is not specified by the application or if the application
    // specifies a character set that is unsupported.

    // TODO JaxRsComponent
    // http://restlet.tigris.org/issues/show_bug.cgi?id=464#desc17
    // http://restlet.tigris.org/issues/show_bug.cgi?id=464#desc19
    
    // TODO @Context and so on only in objects created by the runtime.
    // support and warn about this.
    
    // TESTEN what happens, if ".." or "." in @Path?
    
    // REQUEST Spec, section 5.1.3. (Content Negotiation and Preconditions):
    // request.evaluatePRECONDITIONS(...); null is not necessary
    
    // REQUESTED @Context on type PathSegment ?
    
    // REQUEST disallow entity on sub resource locator?
    
    // REQUEST javadoc of PathParam: Template -> Path
    
    // TODO read javadoc of PathSegment again. 
}