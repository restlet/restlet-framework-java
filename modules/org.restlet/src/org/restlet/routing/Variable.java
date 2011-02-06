/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.routing;

import org.restlet.data.Reference;

/**
 * Variable descriptor for reference templates.
 * 
 * @see Template
 * @author Jerome Louvel
 */
public final class Variable {

    /** Matches all characters. */
    public static final int TYPE_ALL = 1;

    /** Matches all alphabetical characters. */
    public static final int TYPE_ALPHA = 2;

    /** Matches all alphabetical and digital characters. */
    public static final int TYPE_ALPHA_DIGIT = 3;

    /** Matches any TEXT excluding "(" and ")". */
    public static final int TYPE_COMMENT = 4;

    /** Matches any TEXT inside a comment excluding ";". */
    public static final int TYPE_COMMENT_ATTRIBUTE = 5;

    /** Matches all digital characters. */
    public static final int TYPE_DIGIT = 6;

    /** Matches any CHAR except CTLs or separators. */
    public static final int TYPE_TOKEN = 7;

    /** Matches all URI characters. */
    public static final int TYPE_URI_ALL = 8;

    /** Matches URI fragment characters. */
    public static final int TYPE_URI_FRAGMENT = 9;

    /** Matches URI path characters (not the query or the fragment parts). */
    public static final int TYPE_URI_PATH = 10;

    /** Matches URI query characters. */
    public static final int TYPE_URI_QUERY = 11;

    /** Matches URI query parameter characters (name or value). */
    public static final int TYPE_URI_QUERY_PARAM = 12;

    /** Matches URI scheme characters. */
    public static final int TYPE_URI_SCHEME = 13;

    /** Matches URI segment characters. */
    public static final int TYPE_URI_SEGMENT = 14;

    /** Matches unreserved URI characters. */
    public static final int TYPE_URI_UNRESERVED = 15;

    /** Matches all alphabetical and digital characters plus the underscore. */
    public static final int TYPE_WORD = 16;

    /** Indicates if the parsed value must be decoded. */
    private volatile boolean decodingOnParse;

    /** The default value to use if the key couldn't be found in the model. */
    private volatile String defaultValue;

    /** Indicates if the formatted value must be encoded. */
    private volatile boolean encodingOnFormat;

    /**
     * Indicates if the value is fixed, in which case the "defaultValue"
     * property is always used.
     */
    private volatile boolean fixed;

    /** Indicates if the variable is required or optional. */
    private volatile boolean required;

    /** The type of variable. See TYPE_* constants. */
    private volatile int type;

    /**
     * Default constructor. Type is TYPE_ALL, default value is "", required is
     * true and fixed is false.
     */
    public Variable() {
        this(Variable.TYPE_ALL, "", true, false);
    }

    /**
     * Constructor. Default value is "", required is true and fixed is false.
     * 
     * @param type
     *            The type of variable. See TYPE_* constants.
     */
    public Variable(int type) {
        this(type, "", true, false);
    }

    /**
     * Constructor.
     * 
     * @param type
     *            The type of variable. See TYPE_* constants.
     * @param defaultValue
     *            The default value to use if the key couldn't be found in the
     *            model.
     * @param required
     *            Indicates if the variable is required or optional.
     * @param fixed
     *            Indicates if the value is fixed, in which case the
     *            "defaultValue" property is always used.
     */
    public Variable(int type, String defaultValue, boolean required,
            boolean fixed) {
        this(type, defaultValue, required, fixed, false, false);
    }

    /**
     * Constructor.
     * 
     * @param type
     *            The type of variable. See TYPE_* constants.
     * @param defaultValue
     *            The default value to use if the key couldn't be found in the
     *            model.
     * @param required
     *            Indicates if the variable is required or optional.
     * @param fixed
     *            Indicates if the value is fixed, in which case the
     *            "defaultValue" property is always used.
     * @param decodingOnParse
     *            Indicates if the parsed value must be decoded.
     * @param encodingOnFormat
     *            Indicates if the formatted value must be encoded.
     */
    public Variable(int type, String defaultValue, boolean required,
            boolean fixed, boolean decodingOnParse, boolean encodingOnFormat) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
        this.fixed = fixed;
        this.decodingOnParse = decodingOnParse;
        this.encodingOnFormat = encodingOnFormat;
    }

    /**
     * According to the type of the variable, encodes the value given in
     * parameters.
     * 
     * @param value
     *            The value to encode.
     * @return The encoded value, according to the variable type.
     */
    public String encode(String value) {
        switch (this.type) {
        case Variable.TYPE_URI_ALL:
            return Reference.encode(value);
        case Variable.TYPE_URI_UNRESERVED:
            return Reference.encode(value);
        case Variable.TYPE_URI_FRAGMENT:
            return Reference.encode(value);
        case Variable.TYPE_URI_PATH:
            return Reference.encode(value);
        case Variable.TYPE_URI_QUERY:
            return Reference.encode(value);
        case Variable.TYPE_URI_SEGMENT:
            return Reference.encode(value);
        default:
            return value;
        }
    }

    /**
     * Returns the default value to use if the key couldn't be found in the
     * model.
     * 
     * @return The default value to use if the key couldn't be found in the
     *         model.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns the type of variable. See TYPE_* constants.
     * 
     * @return The type of variable. See TYPE_* constants.
     */
    public int getType() {
        return this.type;
    }

    /**
     * Indicates if the parsed value must be decoded.
     * 
     * @return True if the parsed value must be decoded, false otherwise.
     * @deprecated Use {@link #isDecodingOnParse()} instead.
     */
    @Deprecated
    public boolean isDecodedOnParse() {
        return this.decodingOnParse;
    }

    /**
     * Indicates if the parsed value must be decoded.
     * 
     * @return True if the parsed value must be decoded, false otherwise.
     */
    public boolean isDecodingOnParse() {
        return isDecodedOnParse();
    }

    /**
     * Indicates if the formatted value must be encoded.
     * 
     * @return True if the formatted value must be encoded, false otherwise.
     * @deprecated Use {@link #isEncodingOnFormat()} instead.
     */
    @Deprecated
    public boolean isEncodedOnFormat() {
        return this.encodingOnFormat;
    }

    /**
     * Indicates if the formatted value must be encoded.
     * 
     * @return True if the formatted value must be encoded, false otherwise.
     */
    public boolean isEncodingOnFormat() {
        return isEncodedOnFormat();
    }

    /**
     * Returns true if the value is fixed, in which case the "defaultValue"
     * property is always used.
     * 
     * @return True if the value is fixed, in which case the "defaultValue"
     *         property is always used.
     */
    public boolean isFixed() {
        return this.fixed;
    }

    /**
     * Returns true if the variable is required or optional.
     * 
     * @return True if the variable is required or optional.
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Indicates if the parsed value must be decoded.
     * 
     * @param decodingOnParse
     *            True if the parsed value must be decoded, false otherwise.
     * @deprecated Use {@link #setDecodingOnParse(boolean)} instead.
     */
    @Deprecated
    public void setDecodedOnParse(boolean decodingOnParse) {
        this.decodingOnParse = decodingOnParse;
    }

    /**
     * Indicates if the parsed value must be decoded.
     * 
     * @param decodingOnParse
     *            True if the parsed value must be decoded, false otherwise.
     */
    public void setDecodingOnParse(boolean decodingOnParse) {
        setDecodedOnParse(decodingOnParse);
    }

    /**
     * Sets the default value to use if the key couldn't be found in the model.
     * 
     * @param defaultValue
     *            The default value to use if the key couldn't be found in the
     *            model.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Indicates if the formatted value must be encoded.
     * 
     * @param encodingOnFormat
     *            True if the formatted value must be encoded, false otherwise.
     * @deprecated Use {@link #setEncodingOnFormat(boolean)} instead.
     */
    @Deprecated
    public void setEncodedOnFormat(boolean encodingOnFormat) {
        this.encodingOnFormat = encodingOnFormat;
    }

    /**
     * Indicates if the formatted value must be encoded.
     * 
     * @param encodingOnFormat
     *            True if the formatted value must be encoded, false otherwise.
     */
    public void setEncodingOnFormat(boolean encodingOnFormat) {
        setEncodedOnFormat(encodingOnFormat);
    }

    /**
     * Indicates if the value is fixed
     * 
     * @param fixed
     *            True if the value is fixed
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Indicates if the variable is required or optional.
     * 
     * @param required
     *            True if the variable is required or optional.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Sets the type of variable. See TYPE_* constants.
     * 
     * @param type
     *            The type of variable.
     */
    public void setType(int type) {
        this.type = type;
    }

}
