/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.List;
import org.restlet.data.Encoding;

/**
 * Encoding header writer.
 *
 * @author Jerome Louvel
 */
public class EncodingWriter extends MetadataWriter<Encoding> {

    /**
     * Writes a list of encodings.
     *
     * @param encodings The encodings to write.
     * @return This writer.
     */
    public static String write(List<Encoding> encodings) {
        return new EncodingWriter().append(encodings).toString();
    }

    @Override
    protected boolean canWrite(Encoding encoding) {
        return !encoding.equals(Encoding.IDENTITY);
    }
}
