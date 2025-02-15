/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.resource;

/**
 *
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public interface ExceptionHandler<E extends Throwable> {
    void handle(E throwable);
}
