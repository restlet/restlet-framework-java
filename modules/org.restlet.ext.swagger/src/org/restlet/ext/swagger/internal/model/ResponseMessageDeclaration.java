package org.restlet.ext.swagger.internal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResponseMessageDeclaration {

    private int code;

    private String message;

    private String responseModel;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseModel() {
        return responseModel;
    }

    public void setResponseModel(String responseModel) {
        this.responseModel = responseModel;
    }
}
