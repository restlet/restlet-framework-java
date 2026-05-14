/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.local.FileClientHelper;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

/**
 * Test {@link org.restlet.data.Range}.
 *
 * @author Jerome Louvel
 */
class RangeTestCase {

    private static final Tag ENTITY_TAG = new Tag("TestRangeGetRestlet");

    /** Internal class used for test purpose. */
    private static class TestRangeApplication extends Application {

        public TestRangeApplication() {
            super();
            getRangeService().setEnabled(true);
            setContext(new Context());
            getContext().setClientDispatcher(new Client(Protocol.FILE));
        }

        @Override
        public Restlet createInboundRoot() {
            Router router = new Router();
            router.attach("/testGet", new TestRangeGetRestlet());

            Directory directory =
                    new Directory(
                            getContext(), LocalReference.createFileReference(testDirPath.toFile()));
            directory.setModifiable(true);
            router.attach("/testPut/", directory);
            return router;
        }
    }

    /** Internal class used for test purpose. It simply returns a string 10 characters long. */
    private static class TestRangeGetRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            response.setEntity(new StringRepresentation("1234567890"));
            response.getEntity().setTag(ENTITY_TAG);
        }
    }

    // Create a temporary directory for the tests
    private static Path testDirPath;
    private static TestRangeApplication testRangeApplication;

    @BeforeAll
    static void globalSetup() throws IOException {
        Engine.getInstance().getRegisteredClients().add(new FileClientHelper(null));
        testDirPath = Files.createTempDirectory("rangeTestCase");
        testRangeApplication = new TestRangeApplication();
    }

    @AfterAll
    static void globalCleanup() {
        IoUtils.delete(testDirPath.toFile(), true);
    }

    @Nested
    class TestPartialGet {
        private Request request;

        @BeforeEach
        void setUp() {
            request = new Request(Method.GET, "/testGet");
        }

        @Test
        void noRange() throws IOException {
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("1234567890", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(10, response.getEntity().getAvailableSize());
            assertNull(response.getEntity().getRange());
        }

        @Test
        void fullRange() throws IOException {
            request.setRanges(List.of(new Range(0, 10)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("1234567890", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(10, response.getEntity().getAvailableSize());
            assertEquals(0, response.getEntity().getRange().getIndex());
            assertEquals(10, response.getEntity().getRange().getSize());
        }

        @Test
        void rangeFirst2Bytes() throws Exception {
            request.setRanges(List.of(new Range(Range.INDEX_FIRST, 2)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("12", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(2, response.getEntity().getAvailableSize());
            assertEquals(0, response.getEntity().getRange().getIndex());
            assertEquals(2, response.getEntity().getRange().getSize());
        }

        @Test
        void range2To4Bytes() throws Exception {
            request.setRanges(List.of(new Range(2, 2)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("34", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(2, response.getEntity().getAvailableSize());
            assertEquals(2, response.getEntity().getRange().getIndex());
            assertEquals(2, response.getEntity().getRange().getSize());
        }

        @Test
        void range2To9Bytes() throws Exception {
            request.setRanges(List.of(new Range(2, 7)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("3456789", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(7, response.getEntity().getAvailableSize());
            assertEquals(2, response.getEntity().getRange().getIndex());
            assertEquals(7, response.getEntity().getRange().getSize());
        }

        @Test
        void rangeLast7Bytes() throws Exception {
            request.setRanges(List.of(new Range(Range.INDEX_LAST, 7)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("4567890", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(7, response.getEntity().getAvailableSize());
            assertEquals(-1, response.getEntity().getRange().getIndex());
            assertEquals(7, response.getEntity().getRange().getSize());
        }

        @Test
        void range2toMaxBytes() throws Exception {
            request.setRanges(List.of(new Range(2, Range.SIZE_MAX)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("34567890", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(8, response.getEntity().getAvailableSize());
            assertEquals(2, response.getEntity().getRange().getIndex());
        }

        @Test
        void range2To1000Bytes() throws Exception {
            request.setRanges(List.of(new Range(2, 1000)));
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("34567890", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(8, response.getEntity().getAvailableSize());
            assertEquals(2, response.getEntity().getRange().getIndex());
        }
    }

    @Nested
    class TestConditionalRanges {
        private Request request;

        @BeforeEach
        void setUp() {
            request = new Request(Method.GET, "/testGet");
            request.setRanges(List.of(new Range(1, Range.SIZE_MAX)));
        }

        @Test
        void rangeShouldApply() throws Exception {
            request.getConditions().setRangeTag(ENTITY_TAG);

            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("234567890", response.getEntity().getText());
            assertEquals(10, response.getEntity().getSize());
            assertEquals(9, response.getEntity().getAvailableSize());
            assertEquals(1, response.getEntity().getRange().getIndex());
            assertEquals(-1, response.getEntity().getRange().getSize());
        }

        @Test
        void rangeShouldNotApply() throws Exception {
            Tag entityTag = new Tag(UUID.randomUUID().toString());
            request.getConditions().setRangeTag(entityTag);
            Response response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("1234567890", response.getEntity().getText());
            assertNull(response.getEntity().getRange());
        }
    }

    @Nested
    class TestPut {

        @Test
        void testPut() throws IOException {
            Path testFilePath = testDirPath.resolve("essai.txt");

            testFilePath.toFile().delete();

            // PUT on a file that does not exist
            Request request;
            Response response;
            request = new Request(Method.PUT, "/testPut/essai.txt");
            request.setEntity(new StringRepresentation("1234567890"));
            request.setRanges(List.of(new Range(0, 10)));
            response = testRangeApplication.handle(request);

            assertTrue(response.getStatus().isSuccess());
            assertEquals("1234567890", Files.readString(testFilePath));

            // Partial PUT on a file, the provided representation overflowed the
            // existing file
            request = new Request(Method.PUT, "/testPut/essai.txt");
            request.setEntity(new StringRepresentation("0000000000"));
            request.setRanges(List.of(new Range(1, 10)));
            response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());
            assertEquals("10000000000", Files.readString(testFilePath));

            // Partial PUT on a file, simple range
            request = new Request(Method.PUT, "/testPut/essai.txt");
            request.setEntity(new StringRepresentation("22"));
            request.setRanges(List.of(new Range(2, 2)));
            response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());
            assertEquals("10220000000", Files.readString(testFilePath));

            // Partial PUT on a file, the provided representation will be padded
            // at the very end of the file.
            request = new Request(Method.PUT, "/testPut/essai.txt");
            request.setEntity(new StringRepresentation("888"));
            request.setRanges(List.of(new Range(8, Range.SIZE_MAX)));
            response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());
            assertEquals("10220000888", Files.readString(testFilePath));

            // Partial PUT on a file, the provided representation will be padded
            // just before the end of the file.
            request = new Request(Method.PUT, "/testPut/essai.txt");
            request.setEntity(new StringRepresentation("99"));
            request.setRanges(List.of(new Range(8, Range.SIZE_MAX)));
            response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());
            assertEquals("10220000998", Files.readString(testFilePath));

            request = new Request(Method.GET, "/testPut/essai.txt");
            request.setRanges(List.of(new Range(3, Range.SIZE_MAX)));
            response = testRangeApplication.handle(request);
            assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
            assertEquals("20000998", response.getEntity().getText());

            // Partial PUT on a file, with a non-bytes range, not taken into account
            request = new Request(Method.PUT, "/testPut/essai.txt");
            request.setEntity(new StringRepresentation("1234567890"));
            request.setRanges(List.of(new Range(8, Range.SIZE_MAX, 10, "test")));
            response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());
            assertEquals("1234567890", Files.readString(testFilePath));
        }

        @Test
        void putNewFileWithRangeStartingFrom1Index() throws IOException {
            Path testFilePath = testDirPath.resolve("essai2.txt");
            testFilePath.toFile().delete();

            Request request = new Request(Method.PUT, "/testPut/essai2.txt");
            request.setEntity(new StringRepresentation("1234567890"));
            request.setRanges(List.of(new Range(1, 10)));
            Response response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());

            assertTrue(Files.readString(testFilePath).endsWith("234567890"));
        }

        @Test
        void putNewFileWithRangeWithoutSize() throws IOException {
            Path testFilePath = testDirPath.resolve("essai3.txt");
            testFilePath.toFile().delete();

            Request request = new Request(Method.PUT, "/testPut/essai3.txt");
            request.setEntity(new StringRepresentation("123"));
            request.setRanges(List.of(new Range(8, Range.SIZE_MAX)));
            Response response = testRangeApplication.handle(request);
            assertTrue(response.getStatus().isSuccess());
            assertTrue(Files.readString(testFilePath).endsWith("123"));
        }
    }

    @Test
    void testMultipleRanges() {
        Request request = new Request(Method.GET, "/testGet");
        request.setRanges(List.of(new Range(1), new Range(2)));

        Response response = testRangeApplication.handle(request);
        assertEquals(Status.SERVER_ERROR_NOT_IMPLEMENTED, response.getStatus());
    }
}
