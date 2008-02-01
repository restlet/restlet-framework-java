/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.DateUtils;
import org.restlet.util.Variable;

/**
 * Copy of {@link org.restlet.util.Template}, but some methods public instead
 * of private.
 */
@SuppressWarnings("all")
public class Template extends org.restlet.util.Template {

    /**
     * Resolves variable values.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    public interface VariableResolver {
        /**
         * Resolves a variable name into a value.
         * 
         * @param variableName
         *                The variable name to resolve.
         * @return The resolved value.
         */
        public String resolve(String variableName);
    }

    public Template(Logger logger, String pattern, int matchingMode,
            int defaultType, String defaultDefaultValue,
            boolean defaultRequired, boolean defaultFixed) {
        super(logger, pattern, matchingMode, defaultType, defaultDefaultValue,
                defaultRequired, defaultFixed);
    }

    public Template(Logger logger, String pattern, int matchingMode) {
        super(logger, pattern, matchingMode);
    }

    public Template(String pattern, int matchingMode, int defaultType,
            String defaultDefaultValue, boolean defaultRequired,
            boolean defaultFixed) {
        super(pattern, matchingMode, defaultType, defaultDefaultValue,
                defaultRequired, defaultFixed);
    }

    public Template(String pattern, int matchingMode) {
        super(pattern, matchingMode);
    }

    public Template(String pattern) {
        super(pattern);
    }

    public Template(Logger logger, String pattern) {
        super(logger, pattern);
    }

    @Override
    protected String resolve(Object userObject, String variableName) {
        VariableResolver variableResolver = (VariableResolver) userObject;
        return variableResolver.resolve(variableName);
    }

}
