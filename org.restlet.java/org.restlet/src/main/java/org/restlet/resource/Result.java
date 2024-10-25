/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
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
