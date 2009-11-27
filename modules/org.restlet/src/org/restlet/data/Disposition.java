/**
 * Copyright 2005-2009 Noelios Technologies.
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

    /** The creation date parameter as presented by the rfc 2183. */
    public static final String DISPOSITION_PARAMETER_CREATION_DATE = "creation-date";

    /** The filename parameter as presented by the rfc 2183. */
    public static final String DISPOSITION_PARAMETER_FILENAME = "filename";

    /** The modification date parameter as presented by the rfc 2183. */
    public static final String DISPOSITION_PARAMETER_MODIFICATION_DATE = "modification-date";

    /** The read date parameter as presented by the rfc 2183. */
    public static final String DISPOSITION_PARAMETER_READ_DATE = "read-date";

    /** The size parameter as presented by the rfc 2183. */
    public static final String DISPOSITION_PARAMETER_SIZE = "size";

    /**
     * Indicates that the part is intended to be separated from the full
     * message.
     */
    public static final String DISPOSITION_TYPE_ATTACHMENT = "attachment";

    /**
     * Indicates that the part is intended to be displayed automatically upon
     * display of the full message.
     */
    public static final String DISPOSITION_TYPE_INLINE = "inline";

    /** The list of disposition parameters. */
    private Series<Parameter> parameters;

    /** The disposition type. */
    private String type;

    /**
     * Constructor. Instantiates by default an inline element.
     */
    public Disposition() {
        this(Disposition.DISPOSITION_TYPE_INLINE);
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
     * Returns the value of the "filename" parameter.
     * 
     * @return The value of the "filename" parameter.
     */
    public String getFilename() {
        return getParameters().getFirstValue(DISPOSITION_PARAMETER_FILENAME,
                true);
    }

    /**
     * Returns the list of disposition parameters.
     * 
     * @return The list of disposition parameters.
     */
    public Series<Parameter> getParameters() {
        if (this.parameters == null) {
            this.parameters = new Form();
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
     * Adds the creation date parameter.
     * 
     * @param value
     *            The creation date.
     */
    public void putCreationDate(Date value) {
        putDate(DISPOSITION_PARAMETER_CREATION_DATE, value);
    }

    /**
     * Add a Date parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param value
     *            Its value as a date.
     */
    public void putDate(String name, Date value) {
        getParameters().add(name,
                DateUtils.format(value, DateUtils.FORMAT_RFC_822.get(0)));
    }

    /**
     * Sets the value of the "filename" parameter.
     * 
     * @param fileName
     *            The file name value.
     */
    public void putFilename(String fileName) {
        getParameters().set(Disposition.DISPOSITION_PARAMETER_FILENAME,
                fileName, true);
    }

    /**
     * Adds the modification date parameter.
     * 
     * @param value
     *            The modification date.
     */
    public void putModificationDate(Date value) {
        putDate(DISPOSITION_PARAMETER_MODIFICATION_DATE, value);
    }

    /**
     * Adds the read date parameter.
     * 
     * @param value
     *            The read date.
     */
    public void putReadDate(Date value) {
        putDate(DISPOSITION_PARAMETER_READ_DATE, value);
    }

    /**
     * Sets the value of the "size" parameter.
     * 
     * @param size
     *            The size.
     */
    public void putSize(long size) {
        getParameters().set(Disposition.DISPOSITION_PARAMETER_SIZE,
                Long.toString(size), true);
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
     * Sets the disposition type.
     * 
     * @param type
     *            The disposition type.
     */
    public void setType(String type) {
        this.type = type;
    }

}
