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

package org.restlet.data;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.util.Engine;
import org.restlet.util.Series;

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
        Engine.getInstance().parse(logger, this, queryString, characterSet);
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
        this(Logger.getLogger(Form.class.getCanonicalName()), queryString,
                CharacterSet.UTF_8);
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
        this(Logger.getLogger(Form.class.getCanonicalName()), queryString,
                characterSet);
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
     * URL encodes the form.
     * 
     * @param characterSet
     *                The supported character encoding.
     * @return The encoded form.
     * @throws IOException
     */
    public String encode(CharacterSet characterSet) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0)
                sb.append('&');
            get(i).encode(sb, characterSet);
        }
        return sb.toString();
    }

}
