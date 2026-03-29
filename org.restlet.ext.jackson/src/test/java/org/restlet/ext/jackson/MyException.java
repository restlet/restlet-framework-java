/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"cause", "localizedMessage", "suppressed", "message"})
public class MyException extends Exception {

    private static final long serialVersionUID = 1L;

    private Customer customer;

    private String errorCode;

    public MyException() {}

    public MyException(Customer customer, String errorCode) {
        this(customer, errorCode, "Customer exception detected", null, true, true);
    }

    public MyException(
            Customer customer,
            String errorCode,
            String message,
            Throwable cause,
            boolean enableSuppression,
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
