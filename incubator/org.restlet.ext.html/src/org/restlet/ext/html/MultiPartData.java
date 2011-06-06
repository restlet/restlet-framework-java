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

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Multi-part form data. Also known as a form parameter. Its value is a
 * representation, either textual or binary.
 * 
 * @author Jerome Louvel
 */
public class MultiPartData {

    private volatile String name;

    private volatile Representation value;

    public MultiPartData(Parameter parameter) {
        this(parameter.getName(), parameter.getValue());
    }

    public MultiPartData(String name, CharSequence text) {
        this(name, new StringRepresentation(text));
    }

    public MultiPartData(String name, Representation value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Representation getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(CharSequence text) {
        setValue(new StringRepresentation(text));
    }

    public void setValue(Representation value) {
        this.value = value;
    }

    public String getFilename() {
        return getDisposition() == null ? "" : getDisposition().getFilename();
    }

    public Disposition getDisposition() {
        return getValue() == null ? null : getValue().getDisposition();
    }

    public MediaType getMediaType() {
        return getValue() == null ? null : getValue().getMediaType();
    }

}
