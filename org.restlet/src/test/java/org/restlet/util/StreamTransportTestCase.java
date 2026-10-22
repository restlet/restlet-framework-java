/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class StreamTransportTestCase {

    @Test
    void boundsOpaqueReadsAndWritesWithoutClosingBorrowedStreams() throws Exception {
        var input = new TrackedInput(new byte[] {0, 10, (byte) 255, 0, 9});
        var output = new TrackedOutput();
        try (var transport = new StreamTransport(input, output, () -> {})) {
            transport.setMaxOperationBytes(4);
            assertThrows(IOException.class, () -> transport.read(new byte[8], 0, 8));
            transport.start();
            assertEquals(0, input.reads.get(), "Start must not read or prefetch protocol bytes");
            assertThrows(IllegalStateException.class, () -> transport.setMaxOperationBytes(5));
            byte[] bytes = new byte[8];
            assertEquals(4, transport.read(bytes, 0, bytes.length));
            assertArrayEquals(new byte[] {0, 10, (byte) 255, 0}, Arrays.copyOf(bytes, 4));
            assertEquals(1, transport.read(bytes, 0, bytes.length));
            assertEquals(-1, transport.read(bytes, 0, bytes.length));
            transport.write(new byte[] {0, 1, 10, (byte) 255});
            assertThrows(IllegalArgumentException.class, () -> transport.write(new byte[5]));
            assertArrayEquals(new byte[] {0, 1, 10, (byte) 255}, output.toByteArray());
            assertEquals(0, transport.getStderrTail().length);
        }
        assertFalse(input.closed);
        assertFalse(output.closed);
        assertTrue(output.flushes.get() >= 2);
    }

    @Test
    void serializesCompleteWritesFromConcurrentCallers() throws Exception {
        var output =
                new ByteArrayOutputStream() {
                    @Override
                    public void write(byte[] bytes) {
                        for (byte value : bytes) {
                            write(value);
                            Thread.yield();
                        }
                    }
                };
        try (var transport =
                        new StreamTransport(
                                new ByteArrayInputStream(new byte[0]), output, () -> {});
                var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            transport.start();
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 16; i++) {
                byte value = (byte) i;
                futures.add(
                        executor.submit(
                                () -> {
                                    byte[] sequence = new byte[1024];
                                    Arrays.fill(sequence, value);
                                    transport.write(sequence);
                                    return null;
                                }));
            }
            for (var future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            byte[] bytes = output.toByteArray();
            assertEquals(16 * 1024, bytes.length);
            for (int offset = 0; offset < bytes.length; offset += 1024) {
                byte value = bytes[offset];
                for (int i = offset; i < offset + 1024; i++) {
                    assertEquals(value, bytes[i]);
                }
            }
        }
    }

    @Test
    void stopsBlockedBorrowedInputWithoutClosingIt() throws Exception {
        CountDownLatch reading = new CountDownLatch(1);
        AtomicInteger cancelled = new AtomicInteger();
        InputStream input =
                new InputStream() {
                    @Override
                    public int read() throws IOException {
                        reading.countDown();
                        try {
                            new CountDownLatch(1).await();
                            throw new AssertionError("Unreachable");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new InterruptedIOException("Cancelled read");
                        }
                    }

                    @Override
                    public void close() {
                        fail("Borrowed input must not be closed");
                    }
                };
        var output = new TrackedOutput();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
                var transport = new StreamTransport(input, output, cancelled::incrementAndGet)) {
            transport.start();
            var read =
                    executor.submit(
                            () ->
                                    assertThrows(
                                            IOException.class,
                                            () -> transport.read(new byte[1], 0, 1)));
            assertTrue(reading.await(5, TimeUnit.SECONDS));
            transport.stop();
            read.get(5, TimeUnit.SECONDS);
            assertEquals(1, cancelled.get());
            assertFalse(transport.isStarted());
            assertThrows(IOException.class, () -> transport.write(new byte[1]));
            assertThrows(IllegalStateException.class, transport::start);
        }
        assertFalse(output.closed);
    }

    @Test
    void stopsBlockedAndQueuedWritesWithoutClosingBorrowedOutput() throws Exception {
        CountDownLatch writing = new CountDownLatch(1);
        OutputStream output =
                new OutputStream() {
                    @Override
                    public void write(int value) throws IOException {
                        writing.countDown();
                        try {
                            new CountDownLatch(1).await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new InterruptedIOException("Cancelled write");
                        }
                    }

                    @Override
                    public void close() {
                        fail("Borrowed output must not be closed");
                    }
                };
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
                var transport =
                        new StreamTransport(
                                new ByteArrayInputStream(new byte[0]), output, () -> {})) {
            transport.start();
            var active =
                    executor.submit(
                            () ->
                                    assertThrows(
                                            IOException.class,
                                            () -> transport.write(new byte[] {1})));
            assertTrue(writing.await(5, TimeUnit.SECONDS));
            var queued =
                    executor.submit(
                            () ->
                                    assertThrows(
                                            IOException.class,
                                            () -> transport.write(new byte[] {2})));
            transport.stop();
            active.get(5, TimeUnit.SECONDS);
            queued.get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void reportsUncancellableBorrowedIoInsteadOfClaimingSuccessfulStop() throws Exception {
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        InputStream input =
                new InputStream() {
                    @Override
                    public int read() {
                        reading.countDown();
                        boolean done = false;
                        while (!done) {
                            try {
                                release.await();
                                done = true;
                            } catch (InterruptedException e) {
                                // Deliberately models an input that does not support interruption.
                            }
                        }
                        return -1;
                    }
                };
        var transport = new StreamTransport(input, new ByteArrayOutputStream(), () -> {});
        transport.setProcessExitGrace(Duration.ZERO);
        transport.setShutdownTimeout(Duration.ofMillis(100));
        transport.start();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var read = executor.submit(() -> transport.read(new byte[1], 0, 1));
            try {
                assertTrue(reading.await(5, TimeUnit.SECONDS));
                assertTimeoutPreemptively(
                        Duration.ofSeconds(2),
                        () -> assertThrows(IOException.class, transport::stop));
                assertFalse(transport.isStarted());
            } finally {
                release.countDown();
            }
            assertEquals(-1, read.get(5, TimeUnit.SECONDS));
            transport.stop();
        }
    }

    @Test
    void propagatesIoAndCancellationFailures() throws Exception {
        var transport =
                new StreamTransport(
                        new InputStream() {
                            @Override
                            public int read() throws IOException {
                                throw new IOException("input-failure");
                            }
                        },
                        new OutputStream() {
                            @Override
                            public void write(int value) throws IOException {
                                throw new IOException("output-failure");
                            }
                        },
                        () -> {
                            throw new IllegalStateException("cancel-failure");
                        });
        transport.start();
        assertEquals(
                "input-failure",
                assertThrows(IOException.class, () -> transport.read(new byte[1], 0, 1))
                        .getMessage());
        assertEquals(
                "output-failure",
                assertThrows(IOException.class, () -> transport.write(new byte[1])).getMessage());
        assertInstanceOf(
                IllegalStateException.class,
                assertThrows(IOException.class, transport::stop).getCause().getCause());
    }

    @Test
    void launchesOwnedProcessAndDrainsBoundedStderrSeparately() throws Exception {
        var transport = new StreamTransport(command("echo"));
        transport.setMaxStderrBytes(257);
        transport.start();
        long pid = transport.getProcessId();
        try {
            byte[] bytes = new byte[] {0, 10, 13, (byte) 255, 42};
            transport.write(bytes);
            byte[] received = new byte[bytes.length];
            int offset = 0;
            while (offset < received.length) {
                int count = transport.read(received, offset, received.length - offset);
                assertTrue(count > 0);
                offset += count;
            }
            assertArrayEquals(bytes, received);
            assertEquals(257, transport.getStderrTail().length);
            assertTrue(Arrays.equals(new byte[257], transport.getStderrTail()));
        } finally {
            transport.stop();
        }
        assertFalse(ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false));
    }

    @Test
    void terminatesOwnedProcessWhileStdoutIsBlocked() throws Exception {
        var transport = new StreamTransport(command("idle"));
        transport.start();
        long pid = transport.getProcessId();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var read =
                    executor.submit(
                            () -> {
                                try {
                                    return transport.read(new byte[1], 0, 1);
                                } catch (IOException e) {
                                    assertFalse(transport.isStarted());
                                    return -1;
                                }
                            });
            transport.stop();
            assertEquals(-1, read.get(5, TimeUnit.SECONDS));
        }
        assertFalse(ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false));
    }

    @Test
    void surfacesProcessExitAndStartupFailure() throws Exception {
        var failed = new StreamTransport(command("fail"));
        failed.start();
        try {
            IOException error =
                    assertThrows(IOException.class, () -> failed.read(new byte[1], 0, 1));
            assertTrue(error.getMessage().contains("23"));
        } finally {
            assertThrows(IOException.class, failed::stop);
        }
        var unread = new StreamTransport(command("fail"));
        unread.start();
        ProcessHandle.of(unread.getProcessId()).orElseThrow().onExit().get(5, TimeUnit.SECONDS);
        assertTrue(
                assertThrows(IOException.class, unread::stop)
                        .getCause()
                        .getMessage()
                        .contains("23"));
        try (var transport =
                new StreamTransport(
                        List.of(Path.of("missing-r1-executable").toAbsolutePath().toString()))) {
            assertThrows(IOException.class, transport::start);
        }
        assertThrows(IllegalArgumentException.class, () -> new StreamTransport(List.of()));
    }

    private static List<String> command(String mode) {
        String executable =
                Path.of(
                                System.getProperty("java.home"),
                                "bin",
                                System.getProperty("os.name").toLowerCase().contains("win")
                                        ? "java.exe"
                                        : "java")
                        .toString();
        return List.of(
                executable,
                "-cp",
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path")),
                Child.class.getName(),
                mode);
    }

    public static class Child {
        public static void main(String[] args) throws Exception {
            if ("fail".equals(args[0])) {
                System.exit(23);
            }
            if ("idle".equals(args[0])) {
                Thread.sleep(60000);
                return;
            }
            System.err.write(new byte[200000]);
            System.err.flush();
            byte[] bytes = new byte[1024];
            int count;
            while ((count = System.in.read(bytes)) != -1) {
                System.out.write(bytes, 0, count);
                System.out.flush();
            }
        }
    }

    private static final class TrackedInput extends ByteArrayInputStream {
        private boolean closed;
        private final AtomicInteger reads = new AtomicInteger();

        private TrackedInput(byte[] bytes) {
            super(bytes);
        }

        @Override
        public synchronized int read(byte[] buffer, int offset, int length) {
            reads.incrementAndGet();
            return super.read(buffer, offset, length);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class TrackedOutput extends ByteArrayOutputStream {
        private boolean closed;
        private final AtomicInteger flushes = new AtomicInteger();

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public void flush() {
            flushes.incrementAndGet();
        }
    }
}
