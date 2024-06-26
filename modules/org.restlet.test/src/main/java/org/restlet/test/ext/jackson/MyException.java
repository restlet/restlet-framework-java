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

package org.restlet.test.ext.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "cause", "localizedMessage", "suppressed", "message" })
public class MyException extends Exception {

    private static final long serialVersionUID = 1L;

    private Customer customer;

    private String errorCode;

    public MyException() {
    }

    public MyException(Customer customer, String errorCode) {
        this(customer, errorCode, "Customer exception detected", null, true,
                true);
    }

    public MyException(Customer customer, String errorCode, String message,
            Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.customer = customer;
        this.errorCode = errorCode;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}
