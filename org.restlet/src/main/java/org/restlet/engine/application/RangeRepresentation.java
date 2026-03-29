/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Objects;
import org.restlet.data.Range;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.io.RangeInputStream;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

/**
 * Representation that exposes only a range of the content of a wrapped representation.
 *
 * @author Jerome Louvel
 */
public class RangeRepresentation extends WrapperRepresentation {

    /** The range specific to this wrapper. */
    private volatile Range range;

    /**
     * Constructor.
     *
     * @param wrappedRepresentation The wrapped representation with a complete content.
     */
    public RangeRepresentation(Representation wrappedRepresentation) {
        this(wrappedRepresentation, null);
    }

    /**
     * Constructor.
     *
     * @param wrappedRepresentation The wrapped representation with a complete content.
     * @param range The range to expose.
     */
    public RangeRepresentation(Representation wrappedRepresentation, Range range) {
        super(wrappedRepresentation);
        if (wrappedRepresentation.getRange() != null) {
            throw new IllegalArgumentException(
                    "The wrapped representation must not have a range set.");
        }
        setRange(range);
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj)
                && Objects.equals(getRange(), ((RangeRepresentation) obj).getRange());
    }

    @Override
    public long getAvailableSize() {
        return IoUtils.getAvailableSize(this);
    }

    /**
     * Returns the range specific to this wrapper. The wrapped representation must not have a range
     * set itself.
     *
     * @return The range specific to this wrapper.
     */
    @Override
    public Range getRange() {
        return this.range;
    }

    @Override
    public Reader getReader() throws IOException {
        return IoUtils.getReader(getStream(), getCharacterSet());
    }

    @Override
    public InputStream getStream() throws IOException {
        return new RangeInputStream(super.getStream(), getSize(), getRange());
    }

    @Override
    public String getText() throws IOException {
        return IoUtils.getText(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Sets the range specific to this wrapper. This will not affect the wrapped representation.
     *
     * @param range The range specific to this wrapper.
     */
    @Override
    public void setRange(Range range) {
        this.range = range;
    }

    @Override
    public void write(java.io.Writer writer) throws IOException {
        OutputStream os = IoUtils.getStream(writer, getCharacterSet());
        write(os);
        os.flush();
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        IoUtils.copy(getStream(), outputStream);
    }
}
