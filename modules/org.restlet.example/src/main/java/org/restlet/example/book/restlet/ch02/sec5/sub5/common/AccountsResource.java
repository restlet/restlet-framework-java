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

package org.restlet.example.book.restlet.ch02.sec5.sub5.common;

import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Collection resource containing user accounts.
 */
public interface AccountsResource {

    /**
     * Returns the list of accounts, each one on a separate line.
     * 
     * @return The list of accounts.
     */
    @Get("txt")
    String represent();

    /**
     * Add the given account to the list and returns its position as an
     * identifier.
     * 
     * @param account
     *            The account to add.
     * @return The account identifier.
     */
    @Post("txt")
    String add(String account);

}
