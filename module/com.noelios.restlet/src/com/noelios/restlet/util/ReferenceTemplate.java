/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.util;

import java.util.List;

import org.restlet.util.Model;
import org.restlet.util.StringTemplate;

/**
 * Reference template capable of converting a URI template into a Regex pattern.
 * 
 * @see java.util.regex.Pattern
 * @see <a
 *      href="http://bitworking.org/projects/URI-Templates/draft-gregorio-uritemplate-00.html">INTERNET
 *      DRAFT - URI Template</a>
 * @author Jerome Louvel (contact@noelios.com)
 * @deprecated
 */
@Deprecated
public class ReferenceTemplate extends StringTemplate {
    private List<String> variables;

    public ReferenceTemplate(CharSequence pattern, List<String> variables) {
        super(pattern, "{", "}", "#[", "]");
        this.variables = variables;
    }

    /**
     * Converts the URI template into a Regex pattern.
     * 
     * @return The Regex pattern.
     */
    public String toRegex() {
        return format(null);
    }

    /**
     * Returns the list of variables.
     * 
     * @return The list of variables.
     */
    public List<String> getVariables() {
        return this.variables;
    }

    /**
     * Processes a variable token.
     * 
     * @param textState
     *            The current text state. (see TEXT_* constants).
     * @param variable
     *            The variable.
     * @param buffer
     *            The string buffer containing the template result.
     * @param model
     *            The template model to use.
     * @return The state after processing.
     */
    protected int processVariable(int textState, String variable,
            StringBuilder buffer, Model model) {
        if (textState == TEXT_APPEND) {
            getVariables().add(variable);
            buffer.append("([a-zA-Z0-9\\-\\.\\_\\~]*)");
        }

        return textState;
    }

}
