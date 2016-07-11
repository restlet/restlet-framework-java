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

package org.restlet.data;

import java.util.Date;

import org.restlet.engine.util.DateUtils;
import org.restlet.util.Series;

/**
 * Describes the presentation of a single entity especially in the case of
 * multipart documents. This is an equivalent of the HTTP "Content-Disposition"
 * header.
 * 
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1"
 *      >Content-Disposition header</a>
 * @see <a href="http://tools.ietf.org/html/rfc2183" >The Content-Disposition
 *      Header Field</a>
 * 
 * @author Thierry Boileau
 */
public class Disposition {

    /** The creation date parameter name as presented by the RFC 2183. */
    public static final String NAME_CREATION_DATE = "creation-date";

    /** The filename parameter name as presented by the RFC 2183. */
    public static final String NAME_FILENAME = "filename";

    /** The modification date parameter name as presented by the RFCc 2183. */
    public static final String NAME_MODIFICATION_DATE = "modification-date";

    /** The read date parameter name as presented by the RFC 2183. */
    public static final String NAME_READ_DATE = "read-date";

    /** The size parameter name as presented by the RFC 2183. */
    public static final String NAME_SIZE = "size";

    /**
     * Indicates that the part is intended to be separated from the full
     * message.
     */
    public static final String TYPE_ATTACHMENT = "attachment";

    /**
     * Indicates that the part is intended to be displayed automatically upon
     * display of the full message.
     */
    public static final String TYPE_INLINE = "inline";

    /** Indicates that the part is not intended to be displayed. */
    public static final String TYPE_NONE = "none";

    /** The list of disposition parameters. */
    private Series<Parameter> parameters;

    /** The disposition type. */
    private String type;

    /**
     * Constructor. Instantiated with the TYPE_NONE type.
     */
    public Disposition() {
        this(Disposition.TYPE_NONE);
    }

    /**
     * Constructor.
     * 
     * @param type
     *            The disposition type.
     */
    public Disposition(String type) {
        super();
        this.type = type;
    }

    /**
     * Constructor.
     * 
     * @param type
     *            The disposition type.
     * @param parameters
     *            The list of disposition parameters.
     */
    public Disposition(String type, Series<Parameter> parameters) {
        this(type);
        this.parameters = parameters;
    }

    /**
     * Adds a Date parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param value
     *            Its value as a date.
     */
    public void addDate(String name, Date value) {
        getParameters().add(name,
                DateUtils.format(value, DateUtils.FORMAT_RFC_822.get(0)));
    }

    /**
     * Returns the value of the "filename" parameter.
     * 
     * @return The value of the "filename" parameter.
     */
    public String getFilename() {
        return getParameters().getFirstValue(NAME_FILENAME, true);
    }

    /**
     * Returns the list of disposition parameters.
     * 
     * @return The list of disposition parameters.
     */
    public Series<Parameter> getParameters() {
        if (this.parameters == null) {
            // [ifndef gwt] instruction
            this.parameters = new Series<Parameter>(Parameter.class);
            // [ifdef gwt] instruction uncomment
            // this.parameters = new org.restlet.engine.util.ParameterSeries();
        }

        return this.parameters;
    }

    /**
     * Returns the disposition type.
     * 
     * @return The disposition type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the creation date parameter.
     * 
     * @param value
     *            The creation date.
     */
    public void setCreationDate(Date value) {
        setDate(NAME_CREATION_DATE, value);
    }

    /**
     * Sets a Date parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param value
     *            Its value as a date.
     */
    public void setDate(String name, Date value) {
        getParameters().set(name,
                DateUtils.format(value, DateUtils.FORMAT_RFC_822.get(0)), true);
    }

    /**
     * Sets the value of the "filename" parameter.
     * 
     * @param fileName
     *            The file name value.
     */
    public void setFilename(String fileName) {
        getParameters().set(Disposition.NAME_FILENAME, fileName, true);
    }

    /**
     * Sets the modification date parameter.
     * 
     * @param value
     *            The modification date.
     */
    public void setModificationDate(Date value) {
        setDate(NAME_MODIFICATION_DATE, value);
    }

    /**
     * Sets the list of disposition parameters.
     * 
     * @param parameters
     *            The list of disposition parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the read date parameter.
     * 
     * @param value
     *            The read date.
     */
    public void setReadDate(Date value) {
        setDate(NAME_READ_DATE, value);
    }

    /**
     * Sets the value of the "size" parameter.
     * 
     * @param size
     *            The size.
     */
    public void setSize(long size) {
        getParameters().set(Disposition.NAME_SIZE, Long.toString(size), true);
    }

    /**
     * Sets the disposition type.
     * 
     * @param type
     *            The disposition type.
     */
    public void setType(String type) {
        this.type = type;
    }

}
