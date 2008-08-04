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
package org.restlet.ext.jaxrs.internal.todo;

/**
 * Here are notices for the implementation.
 * 
 * @author Stephan Koops
 */
public class Notizen {

    // REQUEST javadoc of attribute setters of CacheControl does not ease it to
    // use the class

    // REQUESTED what to do, if an ExceptionMapper throws an
    // Exception. I think rethrow (easy to implement the wished rethrow)

    // REQUESTED darf ein ExceptionMapper null zurueckgeben? K�nnte man als
    // Indikator verwenden, die Exception weiterzugeben

    // REQUEST could the implementation see, what is required to add
    // after precondition evaluating into the ResponseBuilder

    // LATER @Path(limited=false) effects only trailing values; implement when
    // @Path template matching is ready

    // TESTEN what happens, if ".." or "." in @Path?

    // REQUESTED rename @DefaultValue to @Default? it's shorter

    // TODO test again Jersey Implementation.

    // TESTEN *ExceptionWriter, see JSR311-dev-mail 1225
    // https://jsr311.dev.java.net/servlets/ReadMsg?list=dev&msgNo=1225

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