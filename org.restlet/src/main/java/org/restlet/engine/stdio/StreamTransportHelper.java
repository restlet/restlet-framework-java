/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.stdio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/** Internal implementation of the supported {@link org.restlet.util.StreamTransport} seam. */
public final class StreamTransportHelper {

    private final List<String> command;
    private final Runnable cancelIo;
    private final ReentrantLock readLock = new ReentrantLock();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final Set<Thread> operations = new HashSet<>();
    private final Set<Thread> lockWaiters = new HashSet<>();
    private InputStream input;
    private OutputStream output;
    private Process process;
    private Thread stderrReader;
    private IOException stderrFailure;
    private byte[] stderrTail;
    private int stderrSize;
    private int stderrPosition;
    private int maxOperationBytes = 65536;
    private int maxStderrBytes = 8192;
    private Duration shutdownTimeout = Duration.ofSeconds(5);
    private Duration processExitGrace = Duration.ofMillis(250);
    private boolean started;
    private boolean stopped;
    private CompletableFuture<Void> stopping;

    public StreamTransportHelper(InputStream input, OutputStream output, Runnable cancelIo) {
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
        this.cancelIo = Objects.requireNonNull(cancelIo, "cancelIo");
        command = null;
    }

    public StreamTransportHelper(List<String> command) {
        this.command = List.copyOf(command);
        if (this.command.isEmpty() || this.command.getFirst().isBlank()) {
            throw new IllegalArgumentException("An explicit executable is required");
        }
        cancelIo = null;
    }

    public synchronized int getMaxOperationBytes() {
        return maxOperationBytes;
    }

    public synchronized void setMaxOperationBytes(int bytes) {
        requireConfigurable();
        if (bytes <= 0) {
            throw new IllegalArgumentException("maxOperationBytes must be positive");
        }
        maxOperationBytes = bytes;
    }

    public synchronized int getMaxStderrBytes() {
        return maxStderrBytes;
    }

    public synchronized void setMaxStderrBytes(int bytes) {
        requireConfigurable();
        if (bytes <= 0) {
            throw new IllegalArgumentException("maxStderrBytes must be positive");
        }
        maxStderrBytes = bytes;
    }

    public synchronized Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public synchronized void setShutdownTimeout(Duration timeout) {
        requireConfigurable();
        if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(processExitGrace) <= 0) {
            throw new IllegalArgumentException("shutdownTimeout must exceed processExitGrace");
        }
        shutdownTimeout = Duration.ofNanos(timeout.toNanos());
    }

    public synchronized Duration getProcessExitGrace() {
        return processExitGrace;
    }

    public synchronized void setProcessExitGrace(Duration grace) {
        requireConfigurable();
        if (grace.isNegative() || grace.compareTo(shutdownTimeout) >= 0) {
            throw new IllegalArgumentException(
                    "processExitGrace must be nonnegative and below shutdownTimeout");
        }
        processExitGrace = Duration.ofNanos(grace.toNanos());
    }

    private void requireConfigurable() {
        if (started || stopped) {
            throw new IllegalStateException("Transport configuration is fixed after start or stop");
        }
    }

    public synchronized void start() throws IOException {
        if (stopped) {
            throw new IllegalStateException("A stopped transport cannot be restarted");
        }
        if (started) {
            return;
        }
        if (command != null) {
            stderrTail = new byte[maxStderrBytes];
            stderrReader =
                    Thread.ofVirtual().name("restlet-stdio-stderr").unstarted(this::drainStderr);
            process = new ProcessBuilder(command).start();
            input = process.getInputStream();
            output = process.getOutputStream();
            try {
                stderrReader.start();
            } catch (RuntimeException e) {
                stopped = true;
                process.destroyForcibly();
                IOException closeFailure = closePipes(null);
                if (closeFailure != null) {
                    e.addSuppressed(closeFailure);
                }
                try {
                    process.getErrorStream().close();
                } catch (IOException closeError) {
                    e.addSuppressed(closeError);
                }
                throw e;
            }
        }
        started = true;
    }

    public synchronized boolean isStarted() {
        return started && !stopped;
    }

    private synchronized void enter() throws IOException {
        if (!isStarted()) {
            throw new IOException("Stream transport is not accepting I/O");
        }
        if (stderrFailure != null) {
            throw new IOException("Subprocess stderr drain failed", stderrFailure);
        }
        operations.add(Thread.currentThread());
    }

    private synchronized void leave() {
        operations.remove(Thread.currentThread());
        notifyAll();
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, buffer.length);
        lock(readLock);
        try {
            enter();
            try {
                int count = input.read(buffer, offset, Math.min(length, maxOperationBytes));
                // EOF does not imply termination: only report an exit failure seen within grace.
                if (count == -1
                        && process != null
                        && process.waitFor(processExitGrace.toNanos(), TimeUnit.NANOSECONDS)
                        && process.exitValue() != 0
                        && !isStopped()) {
                    throw new IOException("Subprocess exited with code " + process.exitValue());
                }
                return count;
            } catch (InterruptedException e) {
                throw interrupted("Interrupted awaiting subprocess exit", e);
            } finally {
                leave();
            }
        } finally {
            readLock.unlock();
        }
    }

    public void write(byte[] bytes) throws IOException {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length > getMaxOperationBytes()) {
            throw new IllegalArgumentException("Write exceeds maxOperationBytes");
        }
        lock(writeLock);
        try {
            enter();
            try {
                output.write(bytes);
                output.flush();
            } finally {
                leave();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /** Acquires an I/O lock, unless the transport stops while waiting for it. */
    private void lock(ReentrantLock lock) throws IOException {
        synchronized (this) {
            if (!isStarted()) {
                throw new IOException("Stream transport is not accepting I/O");
            }
            lockWaiters.add(Thread.currentThread());
        }
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            if (isStopped()) {
                throw new IOException("Stream transport is not accepting I/O");
            }
            throw interrupted("Interrupted awaiting stream I/O", e);
        } finally {
            synchronized (this) {
                lockWaiters.remove(Thread.currentThread());
            }
        }
    }

    private static InterruptedIOException interrupted(String message, InterruptedException cause) {
        Thread.currentThread().interrupt();
        var failure = new InterruptedIOException(message);
        failure.initCause(cause);
        return failure;
    }

    private synchronized boolean isStopped() {
        return stopped;
    }

    private void drainStderr() {
        try (InputStream errors = process.getErrorStream()) {
            byte[] chunk = new byte[Math.min(4096, maxStderrBytes)];
            int count;
            while ((count = errors.read(chunk)) != -1) {
                synchronized (this) {
                    for (int i = 0; i < count; i++) {
                        stderrTail[stderrPosition] = chunk[i];
                        stderrPosition = (stderrPosition + 1) % stderrTail.length;
                        stderrSize = Math.min(stderrSize + 1, stderrTail.length);
                    }
                }
            }
        } catch (IOException e) {
            synchronized (this) {
                stderrFailure = e;
            }
        }
    }

    public synchronized byte[] getStderrTail() {
        byte[] result = new byte[stderrSize];
        for (int i = 0; i < result.length; i++) {
            result[i] =
                    stderrTail[
                            (stderrPosition - stderrSize + i + stderrTail.length)
                                    % stderrTail.length];
        }
        return result;
    }

    public synchronized long getProcessId() {
        if (process == null) {
            throw new IllegalStateException("No owned subprocess has been started");
        }
        return process.pid();
    }

    public void stop() throws IOException {
        CompletableFuture<Void> completion;
        synchronized (this) {
            if (stopping == null) {
                stopped = true;
                stopping = new CompletableFuture<>();
                if (!started) {
                    stopping.complete(null);
                } else {
                    Thread.startVirtualThread(this::cleanup);
                }
            }
            completion = stopping;
        }
        try {
            completion.get(shutdownTimeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw interrupted("Interrupted stopping stream transport", e);
        } catch (TimeoutException e) {
            throw new IOException(
                    "Stream transport shutdown timed out; borrowed I/O must support cancellation",
                    e);
        } catch (ExecutionException e) {
            throw new IOException("Stream transport shutdown failed", e.getCause());
        }
    }

    private void cleanup() {
        try {
            long deadline = System.nanoTime() + shutdownTimeout.toNanos();
            synchronized (this) {
                operations.forEach(Thread::interrupt);
                lockWaiters.forEach(Thread::interrupt);
            }
            IOException failure = process != null ? stopProcess(deadline) : cancelBorrowedIo();
            failure = awaitOperations(failure);
            if (process == null) {
                failure = flushBorrowedOutput(failure);
            }
            if (failure != null) {
                throw failure;
            }
            stopping.complete(null);
        } catch (IOException | RuntimeException e) {
            stopping.completeExceptionally(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stopping.completeExceptionally(e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Delivers EOF on the child's stdin, then terminates it if it does not exit within the grace
     * period. The forcible kill happens halfway between the grace period and the shutdown deadline,
     * leaving the rest of the shutdown timeout for pipe cleanup.
     *
     * @param deadline The {@link System#nanoTime()} shutdown deadline.
     */
    private IOException stopProcess(long deadline) throws InterruptedException {
        long grace = processExitGrace.toNanos();
        long killDeadline = deadline - (shutdownTimeout.toNanos() - grace) / 2;
        // Closing may block behind a stuck write, which the process termination releases.
        CompletableFuture<IOException> stdinClosed =
                CompletableFuture.supplyAsync(() -> close(output), Thread.ofVirtual()::start);
        boolean exited = waitForExit(grace, killDeadline);
        IOException failure =
                exited && process.exitValue() != 0
                        ? new IOException("Subprocess exited with code " + process.exitValue())
                        : null;
        if (!exited) {
            process.destroy();
            if (!waitForExit(grace, killDeadline)) {
                process.destroyForcibly();
            }
        }
        process.waitFor();
        failure = append(failure, stdinClosed.join());
        failure = append(failure, close(input));
        stderrReader.join();
        return failure;
    }

    /** Waits for the process exit for at most the given duration and before the deadline. */
    private boolean waitForExit(long nanos, long deadline) throws InterruptedException {
        long remaining = Math.min(nanos, deadline - System.nanoTime());
        return process.waitFor(Math.max(0, remaining), TimeUnit.NANOSECONDS);
    }

    private IOException cancelBorrowedIo() {
        try {
            cancelIo.run();
            return null;
        } catch (RuntimeException e) {
            return new IOException("Borrowed I/O cancellation failed", e);
        }
    }

    private synchronized IOException awaitOperations(IOException failure)
            throws InterruptedException {
        while (!operations.isEmpty()) {
            wait();
        }
        return stderrFailure == null
                ? failure
                : append(failure, new IOException("Subprocess stderr drain failed", stderrFailure));
    }

    private IOException flushBorrowedOutput(IOException failure) {
        try {
            output.flush();
            return failure;
        } catch (IOException e) {
            return append(failure, e);
        }
    }

    private IOException closePipes(IOException failure) {
        failure = append(failure, close(input));
        return append(failure, close(output));
    }

    private static IOException close(java.io.Closeable pipe) {
        try {
            pipe.close();
            return null;
        } catch (IOException e) {
            return e;
        }
    }

    private static IOException append(IOException failure, IOException next) {
        if (failure == null || next == null) {
            return failure == null ? next : failure;
        }
        failure.addSuppressed(next);
        return failure;
    }
}
