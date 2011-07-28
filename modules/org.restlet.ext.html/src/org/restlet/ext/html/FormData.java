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

package org.restlet.ext.html;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.NamedValue;

/**
 * HTML form field composed of a name and a value. The value is typically a
 * string but can also be a full fledged representation for multipart form (such
 * as a binary file uploaded).
 * 
 * @author Jerome Louvel
 */
public class FormData implements NamedValue {

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
    public FormData(NamedValue namedValue) {
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
     * @param characterSet
     *            The character set to use.
     * @throws IOException
     */
    public void encode(Appendable buffer, CharacterSet characterSet)
            throws IOException {
        if (getName() != null) {
            buffer.append(Reference.encode(getName(), characterSet));

            if (getValue() != null) {
                buffer.append('=');
                buffer.append(Reference.encode(getValue(), characterSet));
            }
        }
    }

    /**
     * Encodes the parameter as a string.
     * 
     * @param characterSet
     *            The character set to use.
     * @return The encoded string?
     * @throws IOException
     */
    public String encode(CharacterSet characterSet) throws IOException {
        final StringBuilder sb = new StringBuilder();
        encode(sb, characterSet);
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

}
