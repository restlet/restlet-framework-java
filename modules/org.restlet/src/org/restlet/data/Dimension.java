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

package org.restlet.data;

/**
 * Dimension on which the representations of a resource may vary.
 *
 * @author Jerome Louvel (contact@noelios.com)
 * @author Piyush Purang (ppurang@gmail.com)
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.44">HTTP
 *      Vary header</a>
 */
public enum Dimension {
    AUTHORIZATION,
    CHARACTER_SET,
    CLIENT_ADDRESS,
    CLIENT_AGENT,
    UNSPECIFIED,
    ENCODING,
    LANGUAGE,
    MEDIA_TYPE,
    TIME,
}
