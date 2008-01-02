/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.ext.jaxrs.core;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * The mplementation of the JAX-RS interface {@link PathSegment}
 * @author Stephan Koops
 *
 */
public class JaxRsPathSegment implements PathSegment {
    public MultivaluedMap<String, String> getMatrixParameters() {
        throw new NotYetImplementedException(
                "Die Matrix-Auswertung soll später in die Referenz");
    }

    public String getPath() {
        throw new NotYetImplementedException(
                "Die Matrix-Auswertung soll später in die Referenz");
    }
}