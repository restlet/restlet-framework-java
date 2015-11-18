package org.restlet.test.engine.header;

import org.junit.Assert;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderReader;
import org.restlet.test.RestletTestCase;

import java.io.IOException;

public class HeaderReaderTestCase extends RestletTestCase {

    public void testRreadHeader() throws IOException {
        Header result = HeaderReader.readHeader("My-Header: my-header-value");
        Assert.assertNotNull(result);
        Assert.assertEquals("My-Header", result.getName());
        Assert.assertEquals("my-header-value", result.getValue());
    }

    public void testRreadHeaderEmptyValue() throws IOException {
        Header result = HeaderReader.readHeader("My-Header: ");
        Assert.assertNotNull(result);
        Assert.assertEquals("My-Header", result.getName());
        Assert.assertNull(result.getValue());
    }
}
