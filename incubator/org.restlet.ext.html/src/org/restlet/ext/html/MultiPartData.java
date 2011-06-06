package org.restlet.ext.html;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Multi-part form parameter.
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
