/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

/**
 * Callback interface for asynchronous tasks.
 *
 * @param <T> The class of the result object returned in case of success.
 * @author Jerome Louvel
 */
public interface Result<T> {

    /**
     * Method called back by the associated object when a failure is detected.
     *
     * @param caught The exception or error caught.
     */
    void onFailure(Throwable caught);

    /**
     * Method called back by the associated object in case of success.
     *
     * @param result The result object.
     */
    void onSuccess(T result);
}
