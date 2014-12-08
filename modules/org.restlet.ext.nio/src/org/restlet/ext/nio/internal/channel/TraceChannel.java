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

package org.restlet.ext.nio.internal.channel;

import java.io.OutputStream;
import java.nio.channels.SelectableChannel;

import org.restlet.engine.io.SelectionChannel;

/**
 * Filter byte channel that sends a copy of all data on the trace output stream.
 * It is important to inherit from {@link SelectableChannel} as some framework
 * classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class TraceChannel<T extends SelectionChannel> extends
        WrapperSelectionChannel<T> {

    /** The trace output stream to use if tracing is enabled. */
    private OutputStream traceStream;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     */
    public TraceChannel(T wrappedChannel) {
        this(wrappedChannel, System.out);
    }

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param traceStream
     *            The trace stream.
     */
    public TraceChannel(T wrappedChannel, OutputStream traceStream) {
        super(wrappedChannel);
        this.traceStream = traceStream;
    }

    /**
     * Returns the trace output stream to use if tracing is enabled.
     * 
     * @return The trace output stream to use if tracing is enabled.
     */
    public OutputStream getTraceStream() {
        return this.traceStream;
    }

}
