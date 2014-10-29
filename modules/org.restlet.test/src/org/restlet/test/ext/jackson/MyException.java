package org.restlet.test.ext.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "cause", "localizedMessage", "suppressed" })
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
