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

package org.restlet.ext.html;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.NamedValue;

/**
 * HTML form data composed of a name and a value. The value is typically a
 * string but can also be a full fledged representation for multipart form (such
 * as a binary file uploaded).
 * 
 * @author Jerome Louvel
 */
public class FormData implements NamedValue<String> {

    /** The name of the associated form control. */
    private volatile String name;

    /**
     * The value of the associated form control as a full fledged
     * representation.
     */
    private volatile Representation valueRepresentation;

    /**
     * Constructor.
     * 
     * @param namedValue
     */
    public FormData(NamedValue<String> namedValue) {
        this(namedValue.getName(), namedValue.getValue());
    }

    /**
     * Constructor.
     * 
     * @param name
     * @param valueRepresentation
     */
    public FormData(String name, Representation valueRepresentation) {
        this.name = name;
        this.valueRepresentation = valueRepresentation;
    }

    /**
     * Constructor.
     * 
     * @param name
     * @param value
     */
    public FormData(String name, String value) {
        this.name = name;
        setValue(value);
    }

    /**
     * Encodes the parameter into the target buffer.
     * 
     * @param buffer
     *            The target buffer.
     * @param queryString
     *            True if the target is a query string.
     * @throws IOException
     */
    public void encode(Appendable buffer, boolean queryString)
            throws IOException {
        if (getName() != null) {
            buffer.append(Reference.encode(getName(), queryString));

            if (getValue() != null) {
                buffer.append('=');
                buffer.append(Reference.encode(getValue(), queryString));
            }
        }
    }

    /**
     * Encodes the parameter as a string.
     * 
     * @param queryString
     *            True if the target is a query string.
     * @return The encoded string.
     * @throws IOException
     */
    public String encode(boolean queryString) throws IOException {
        StringBuilder sb = new StringBuilder();
        encode(sb, queryString);
        return sb.toString();
    }

    /**
     * Returns the content disposition of the value representation.
     * 
     * @return The content disposition of the value representation.
     */
    public Disposition getDisposition() {
        return getValueRepresentation() == null ? null
                : getValueRepresentation().getDisposition();
    }

    /**
     * Returns the file name of the value representation. To get this
     * information, the {@link Disposition#getFilename()} method is invoked.
     * 
     * @return The file name of the value representation.
     */
    public String getFilename() {
        return getDisposition() == null ? "" : getDisposition().getFilename();
    }

    /**
     * Returns the media type of the value representation.
     * 
     * @return The media type of the value representation.
     */
    public MediaType getMediaType() {
        return getValueRepresentation() == null ? null
                : getValueRepresentation().getMediaType();
    }

    /**
     * Returns the name of the associated form control.
     * 
     * @return The name of the associated form control.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the textual value of the associated form control.
     * 
     * @return The textual value of the associated form control.
     */
    public String getValue() {
        try {
            return getValueRepresentation().getText();
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(Level.INFO,
                            "Unable to get the textual value of the form data value representation",
                            e);
            return null;
        }
    }

    /**
     * Returns the value of the associated form control, either textual or
     * binary.
     * 
     * @return The value of the associated form control.
     */
    public Representation getValueRepresentation() {
        return valueRepresentation;
    }

    /**
     * Sets the name of the associated form control.
     * 
     * @param name
     *            The name of the associated form control.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the textual value of the associated form control.
     * 
     * @param value
     *            The textual value of the associated form control.
     */
    public void setValue(String value) {
        if (getValueRepresentation() instanceof StringRepresentation) {
            ((StringRepresentation) getValueRepresentation()).setText(value);
        } else {
            setValueRepresentation(new StringRepresentation(value));
        }
    }

    /**
     * Sets the value of the associated form control as a full fledged
     * representation.
     * 
     * @param valueRepresentation
     *            The value of the associated form control.
     */
    public void setValueRepresentation(Representation valueRepresentation) {
        this.valueRepresentation = valueRepresentation;
    }

    @Override
    public String toString() {
        return "(" + getName() + "," + getValueRepresentation() + ")";
    }

}
