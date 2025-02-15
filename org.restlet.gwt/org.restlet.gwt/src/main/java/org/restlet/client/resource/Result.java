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
 * Callback interface for asynchronous tasks. This is an equivalent to the
 * {@link com.google.gwt.user.client.rpc.AsyncCallback} interface used by the
 * GWT-RPC mechanism.
 * 
 * @param <T>
 *            The class of the result object returned in case of success.
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public interface Result<T> {

    /**
     * Method called back by the associated
     * {@link org.restlet.client.engine.resource.GwtClientProxy} object when a failure
     * is detected.
     * 
     * @param caught
     *            The exception or error caught.
     */
    void onFailure(Throwable caught);

    /**
     * Method called back by the associated
     * {@link org.restlet.client.engine.resource.GwtClientProxy} object in case of
     * success.
     * 
     * @param result
     *            The result object.
     */
    void onSuccess(T result);

}
