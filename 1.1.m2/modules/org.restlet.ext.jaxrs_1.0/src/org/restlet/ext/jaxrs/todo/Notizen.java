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

package org.restlet.ext.jaxrs.todo;

/**
 * Here are notices for the implementation.
 */
public class Notizen
{
    // LATER An implementation MUST allow other runtime exceptions to propagate to the underlying container. This allows existing container facilities (e.g. a Servlet filter) to be used to handle the error if desired.
    // LATER alle LATERs in Masterarbeit uebernehmen
    
    // REQUESTED JSR311: acceptedMediaTypes: IE and Firefox requests XML before HTML
    // - Firefox 2.0.0.12: [text/xml:1.0, application/xml:1.0, application/xhtml+xml:1.0, text/html:0.9, text/plain:0.8, image/png:1.0, */*:0.5]
    // - IE 7.0.5730.11:   [text/xml:1.0, application/xml:1.0, application/xhtml+xml:1.0, text/html:0.9, text/plain:0.8, image/png:1.0, */*:0.5]
    // - Opera 9.1:        [text/html:1.0, application/xml:0.9, application/xhtml+xml:1.0, image/png:1.0, image/jpeg:1.0, image/gif:1.0, image/x-xbitmap:1.0, */*:0.1]
}
