/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.resource;

import org.restlet.resource.Get;

/**
 * Annotated interface that declares a single "Get" method.
 * 
 * @author Thierry Boileau
 * 
 */
public interface AnnotatedInterface03_01 {

    @Get
    String asText();

}
