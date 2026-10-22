/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import org.restlet.engine.stdio.StreamTransportHelper;

/**
 * Protocol-neutral, blocking byte transport for borrowed streams or an owned subprocess.
 *
 * <p>Reads are on demand, with no background reader or input prefetch. Writes are serialized and
 * flushed individually. A write is an opaque byte sequence, not a message or a framing convention.
 * Configure limits before {@link #start()}; a stopped transport cannot be restarted.
 *
 * <p>Borrowed streams are never closed. Their owner must provide a prompt cancellation action that
 * unblocks active I/O without closing borrowed standard streams. Thread interruption is also
 * attempted. Arbitrary {@link InputStream}s need not be interruptible: if cancellation does not
 * release I/O, {@link #stop()} fails within its timeout rather than claiming successful cleanup. No
 * JVM-global standard stream is redirected.
 */
public final class StreamTransport implements AutoCloseable {

    private final StreamTransportHelper helper;

    /**
     * Creates a transport borrowing both streams.
     *
     * @param input The protocol input.
     * @param output The protocol output.
     * @param cancelIo Prompt action to release blocked borrowed I/O on stop.
     */
    public StreamTransport(InputStream input, OutputStream output, Runnable cancelIo) {
        helper = new StreamTransportHelper(input, output, cancelIo);
    }

    /**
     * Creates a transport owning a subprocess and its pipes. No shell is used and no command is
     * inferred from a URI. Stderr is drained separately into a bounded tail.
     *
     * @param command Executable followed by individual arguments.
     */
    public StreamTransport(List<String> command) {
        helper = new StreamTransportHelper(command);
    }

    /** Returns the maximum bytes read or written by one operation (default 65536). */
    public int getMaxOperationBytes() {
        return helper.getMaxOperationBytes();
    }

    /** Sets the positive per-operation byte limit before start. */
    public void setMaxOperationBytes(int bytes) {
        helper.setMaxOperationBytes(bytes);
    }

    /** Returns the maximum retained subprocess stderr bytes (default 8192). */
    public int getMaxStderrBytes() {
        return helper.getMaxStderrBytes();
    }

    /** Sets the positive stderr tail capacity before start. */
    public void setMaxStderrBytes(int bytes) {
        helper.setMaxStderrBytes(bytes);
    }

    /** Returns the maximum shutdown duration (default five seconds). */
    public Duration getShutdownTimeout() {
        return helper.getShutdownTimeout();
    }

    /** Sets the positive shutdown timeout before start. */
    public void setShutdownTimeout(Duration timeout) {
        helper.setShutdownTimeout(timeout);
    }

    /** Returns the subprocess exit grace period (default 250 milliseconds). */
    public Duration getProcessExitGrace() {
        return helper.getProcessExitGrace();
    }

    /** Sets a nonnegative process exit grace period shorter than the shutdown timeout. */
    public void setProcessExitGrace(Duration grace) {
        helper.setProcessExitGrace(grace);
    }

    /** Starts admission and, when configured, the owned subprocess. */
    public void start() throws IOException {
        helper.start();
    }

    /** Indicates whether new I/O operations are accepted. */
    public boolean isStarted() {
        return helper.isStarted();
    }

    /**
     * Reads at most the configured operation limit, returning -1 on clean EOF. Only one reader
     * executes at a time; a chunk has no application-level framing significance.
     */
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return helper.read(buffer, offset, length);
    }

    /** Writes and flushes one opaque sequence, rejecting values over the operation limit. */
    public void write(byte[] bytes) throws IOException {
        helper.write(bytes);
    }

    /** Returns a snapshot of the bounded stderr tail; borrowed streams have no stderr pipe. */
    public byte[] getStderrTail() {
        return helper.getStderrTail();
    }

    /** Returns the owned subprocess PID, or throws when no subprocess is running. */
    public long getProcessId() {
        return helper.getProcessId();
    }

    /**
     * Stops admission, cancels active I/O and waits within the shutdown timeout. Flushes, but never
     * closes, borrowed output. Closes owned pipes and terminates only the owned process. Reports
     * I/O, stderr-drain, process-exit and timeout failures.
     */
    public void stop() throws IOException {
        helper.stop();
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}
