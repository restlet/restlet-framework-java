/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.Range;
import org.restlet.resource.Representation;
import org.restlet.util.ByteUtils;
import org.restlet.util.WrapperRepresentation;

/**
 * Representation that exposes only a range of the content of a wrapped
 * representation.
 * 
 * @author Jerome Louvel
 */
public class RangeRepresentation extends WrapperRepresentation {

    /**
     * Constructor.
     * 
     * @param wrappedRepresentation
     *            The wrapped representation with a complete content.
     */
    public RangeRepresentation(Representation wrappedRepresentation) {
        this(wrappedRepresentation, null);
    }

    /**
     * Constructor.
     * 
     * @param wrappedRepresentation
     *            The wrapped representation with a complete content.
     * @param range
     *            The range to expose.
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
    public InputStream getStream() throws IOException {
        return new RangeInputStream(super.getStream(), getSize(), getRange());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        ByteUtils.write(getStream(), outputStream);
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        write(ByteUtils.getStream(writableChannel));
    }

}
