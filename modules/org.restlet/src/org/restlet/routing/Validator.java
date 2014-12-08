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

package org.restlet.routing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;

/**
 * Filter validating attributes from a call. Validation is verified based on
 * regex pattern matching.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 * @see Pattern
 */
public class Validator extends Filter {

    /** Internal class holding validation information. */
    private static final class ValidateInfo {
        /** Name of the attribute to look for. */
        protected String attribute;

        /** Format of the attribute value, using Regex pattern syntax. */
        protected String format;

        /** Indicates if the attribute presence is required. */
        protected boolean required;

        /**
         * Constructor.
         * 
         * @param attribute
         *            Name of the attribute to look for.
         * @param required
         *            Indicates if the attribute presence is required.
         * @param format
         *            Format of the attribute value, using Regex pattern syntax.
         */
        public ValidateInfo(String attribute, boolean required, String format) {
            this.attribute = attribute;
            this.required = required;
            this.format = format;
        }
    }

    /** The list of attribute validations. */
    private volatile List<ValidateInfo> validations;

    /**
     * Constructor.
     */
    public Validator() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Validator(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     */
    public Validator(Context context, Restlet next) {
        super(context, next);
    }

    /**
     * Allows filtering before its handling by the target Restlet. By default it
     * parses the template variable, adjust the base reference, then extracts
     * the attributes from form parameters (query, cookies, entity) and finally
     * tries to validate the variables as indicated by the
     * {@link #validate(String, boolean, String)} method.
     * 
     * @param request
     *            The request to filter.
     * @param response
     *            The response to filter.
     * @return The {@link Filter#CONTINUE} status.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (this.validations != null) {
            for (ValidateInfo validate : getValidations()) {
                if (validate.required
                        && !request.getAttributes().containsKey(
                                validate.attribute)) {
                    response.setStatus(
                            Status.CLIENT_ERROR_BAD_REQUEST,
                            "Unable to find the \""
                                    + validate.attribute
                                    + "\" attribute in the request. Please check your request.");
                } else if (validate.format != null) {
                    Object value = request.getAttributes().get(
                            validate.attribute);

                    if ((value != null)
                            && !Pattern.matches(validate.format,
                                    value.toString())) {
                        response.setStatus(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Unable to validate the value of the \""
                                        + validate.attribute
                                        + "\" attribute. The expected format is: "
                                        + validate.format
                                        + " (Java Regex). Please check your request.");
                    }
                }
            }
        }

        return CONTINUE;
    }

    /**
     * Returns the list of attribute validations.
     * 
     * @return The list of attribute validations.
     */
    private List<ValidateInfo> getValidations() {
        // Lazy initialization with double-check.
        List<ValidateInfo> v = this.validations;
        if (v == null) {
            synchronized (this) {
                v = this.validations;
                if (v == null) {
                    this.validations = v = new CopyOnWriteArrayList<ValidateInfo>();
                }
            }
        }
        return v;
    }

    /**
     * Checks the request attributes for presence or format. If the check fails,
     * then a response status CLIENT_ERROR_BAD_REQUEST is returned with the
     * proper status description.
     * 
     * @param attribute
     *            Name of the attribute to look for.
     * @param required
     *            Indicates if the attribute presence is required.
     * @param format
     *            Format of the attribute value, using Regex pattern syntax.
     */
    public void validate(String attribute, boolean required, String format) {
        getValidations().add(new ValidateInfo(attribute, required, format));
    }

    /**
     * Checks the request attributes for format only. If the check fails, then a
     * response status CLIENT_ERROR_BAD_REQUEST is returned with the proper
     * status description.
     * 
     * @param attribute
     *            Name of the attribute to look for.
     * @param format
     *            Format of the attribute value, using Regex pattern syntax.
     */
    public void validateFormat(String attribute, String format) {
        getValidations().add(new ValidateInfo(attribute, false, format));
    }

    /**
     * Checks the request attributes for presence only. If the check fails, then
     * a response status CLIENT_ERROR_BAD_REQUEST is returned with the proper
     * status description.
     * 
     * @param attribute
     *            Name of the attribute to look for.
     */
    public void validatePresence(String attribute) {
        getValidations().add(new ValidateInfo(attribute, true, null));
    }
}
