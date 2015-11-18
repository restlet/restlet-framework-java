package org.restlet.test.engine.header;

import org.junit.Assert;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderReader;
import org.restlet.test.RestletTestCase;

import java.io.IOException;

/**
 * Test case for reading headers
 *
 * @author Timur.Gasrataliev
 */
public class HeaderReaderTestCase extends RestletTestCase {

    /**
     * Reading normal header
     */
    public void testRreadHeader() throws IOException {
        Header result = HeaderReader.readHeader("My-Header: my-header-value");
        Assert.assertNotNull(result);
        Assert.assertEquals("My-Header", result.getName());
        Assert.assertEquals("my-header-value", result.getValue());
    }

    /**
     * Reading empty header
     * Should not fail with StringIndexOutOfBoundsException (issue #1167)
     */
    public void testRreadHeaderEmptyValue() throws IOException {
        Header result = HeaderReader.readHeader("My-Header: ");
        Assert.assertNotNull(result);
        Assert.assertEquals("My-Header", result.getName());
        Assert.assertNull(result.getValue());
    }
}
