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

package org.restlet.gwt.data;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.resource.StringRepresentation;
import org.restlet.gwt.util.Engine;
import org.restlet.gwt.util.Series;

/**
 * Form which is a specialized modifiable list of parameters.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Form extends Series<Parameter> {
    /**
     * Empty constructor.
     */
    public Form() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity
     *                The initial list capacity.
     */
    public Form(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *                The delegate list.
     */
    public Form(List<Parameter> delegate) {
        super(delegate);
    }

    /**
     * Constructor.
     * 
     * @param logger
     *                The logger to use.
     * @param representation
     *                The representation to parse (URL encoded Web form
     *                supported).
     * @throws IOException
     */
    public Form(Logger logger, Representation representation) {
        Engine.getInstance().parse(logger, this, representation);
    }

    /**
     * Constructor.
     * 
     * @param logger
     *                The logger to use.
     * @param queryString
     *                The Web form parameters as a string.
     * @param characterSet
     *                The supported character encoding.
     * @throws IOException
     */
    public Form(Logger logger, String queryString, CharacterSet characterSet) {
        this(logger, queryString, characterSet, '&');
    }

    /**
     * Constructor.
     * 
     * @param logger
     *                The logger to use.
     * @param parametersString
     *                The parameters string to parse.
     * @param characterSet
     *                The supported character encoding.
     * @param separator
     *                The separator character to append between parameters.
     * @throws IOException
     */
    public Form(Logger logger, String parametersString,
            CharacterSet characterSet, char separator) {
        Engine.getInstance().parse(logger, this, parametersString,
                characterSet, true, separator);
    }

    /**
     * Constructor.
     * 
     * @param webForm
     *                The URL encoded Web form.
     * @throws IOException
     */
    public Form(Representation webForm) {
        this(Logger.getLogger(Form.class.getCanonicalName()), webForm);
    }

    /**
     * Constructor. Uses UTF-8 as the character set for encoding non-ASCII
     * characters.
     * 
     * @param queryString
     *                The Web form parameters as a string.
     * @throws IOException
     */
    public Form(String queryString) {
        this(queryString, CharacterSet.UTF_8);
    }

    /**
     * Constructor. Uses UTF-8 as the character set for encoding non-ASCII
     * characters.
     * 
     * @param parametersString
     *                The parameters string to parse.
     * @param separator
     *                The separator character to append between parameters.
     * @throws IOException
     */
    public Form(String parametersString, char separator) {
        this(parametersString, CharacterSet.UTF_8, separator);
    }

    /**
     * Constructor.
     * 
     * @param queryString
     *                The Web form parameters as a string.
     * @param characterSet
     *                The supported character encoding.
     * @throws IOException
     */
    public Form(String queryString, CharacterSet characterSet) {
        this(queryString, characterSet, '&');
    }

    /**
     * Constructor.
     * 
     * @param parametersString
     *                The parameters string to parse.
     * @param characterSet
     *                The supported character encoding.
     * @param separator
     *                The separator character to append between parameters.
     * @throws IOException
     */
    public Form(String parametersString, CharacterSet characterSet,
            char separator) {
        this(Logger.getLogger(Form.class.getCanonicalName()), parametersString,
                characterSet, separator);
    }

    @Override
    public Parameter createEntry(String name, String value) {
        return new Parameter(name, value);
    }

    @Override
    public Series<Parameter> createSeries(List<Parameter> delegate) {
        if (delegate != null)
            return new Form(delegate);
        else
            return new Form();
    }

    /**
     * Encodes the form using the standard URI encoding mechanism and the UTF-8
     * character set.
     * 
     * @return The encoded form.
     * @throws IOException
     */
    public String encode() throws IOException {
        return encode(CharacterSet.UTF_8);
    }

    /**
     * URL encodes the form. The '&' character is used as a separator.
     * 
     * @param characterSet
     *                The supported character encoding.
     * @return The encoded form.
     * @throws IOException
     */
    public String encode(CharacterSet characterSet) throws IOException {
        return encode(characterSet, '&');
    }

    /**
     * URL encodes the form.
     * 
     * @param characterSet
     *                The supported character encoding.
     * @param separator
     *                The separator character to append between parameters.
     * @return The encoded form.
     * @throws IOException
     */
    public String encode(CharacterSet characterSet, char separator)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0)
                sb.append(separator);
            get(i).encode(sb, characterSet);
        }

        return sb.toString();
    }

    /**
     * Formats the form as a matrix path string. Uses UTF-8 as the character set
     * for encoding non-ASCII characters.
     * 
     * @return The form as a matrix string.
     * @see <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">Matrix URIs
     *      by Tim Berners Lee</a>
     */
    public String getMatrixString() {
        return getMatrixString(CharacterSet.UTF_8);
    }

    /**
     * Formats the form as a query string.
     * 
     * @param characterSet
     *                The supported character encoding.
     * @return The form as a matrix string.
     * @see <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">Matrix URIs
     *      by Tim Berners Lee</a>
     */
    public String getMatrixString(CharacterSet characterSet) {
        try {
            return encode(characterSet, ';');
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Formats the form as a query string. Uses UTF-8 as the character set for
     * encoding non-ASCII characters.
     * 
     * @return The form as a query string.
     */
    public String getQueryString() {
        return getQueryString(CharacterSet.UTF_8);
    }

    /**
     * Formats the form as a query string.
     * 
     * @param characterSet
     *                The supported character encoding.
     * @return The form as a query string.
     */
    public String getQueryString(CharacterSet characterSet) {
        try {
            return encode(characterSet);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Returns the form as a Web representation
     * (MediaType.APPLICATION_WWW_FORM). Uses UTF-8 as the character set for
     * encoding non-ASCII characters.
     * 
     * @return The form as a Web representation.
     */
    public Representation getWebRepresentation() {
        return getWebRepresentation(CharacterSet.UTF_8);
    }

    /**
     * Returns the form as a Web representation
     * (MediaType.APPLICATION_WWW_FORM).
     * 
     * @param characterSet
     *                The supported character encoding.
     * @return The form as a Web representation.
     */
    public Representation getWebRepresentation(CharacterSet characterSet) {
        return new StringRepresentation(getQueryString(characterSet),
                MediaType.APPLICATION_WWW_FORM, null, characterSet);
    }

}
