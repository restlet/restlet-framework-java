/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.resource;

// [ifdef gwt] javadocs
/**
 * Callback interface for asynchronous tasks. This is an equivalent to the
 * {@link com.google.gwt.user.client.rpc.AsyncCallback} interface used by the
 * GWT-RPC mechanism.
 * 
 * @param <T>
 *            The class of the result object returned in case of success.
 * @author Jerome Louvel
 */
// [ifndef gwt] javadocs
/**
 * Callback interface for asynchronous tasks.
 * 
 * @param <T>
 *            The class of the result object returned in case of success.
 * @author Jerome Louvel
 */
public interface Result<T> {

    // [ifdef gwt] javadocs
    /**
     * Method called back by the associated
     * {@link org.restlet.engine.resource.GwtClientProxy} object when a failure
     * is detected.
     * 
     * @param caught
     *            The exception or error caught.
     */
    // [ifndef gwt] javadocs
    /**
     * Method called back by the associated object when a failure is detected.
     * 
     * @param caught
     *            The exception or error caught.
     */
    void onFailure(Throwable caught);

    // [ifdef gwt] javadocs
    /**
     * Method called back by the associated
     * {@link org.restlet.engine.resource.GwtClientProxy} object in case of
     * success.
     * 
     * @param result
     *            The result object.
     */
    // [ifndef gwt] javadocs
    /**
     * Method called back by the associated object in case of success.
     * 
     * @param result
     *            The result object.
     */
    void onSuccess(T result);

}
