/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import org.restlet.data.Metadata;

/**
 * Metadata header writer.
 *
 * @author Jerome Louvel
 */
public class MetadataWriter<M extends Metadata> extends HeaderWriter<M> {

    @Override
    public MetadataWriter<M> append(M metadata) {
        return (MetadataWriter<M>) append(metadata.getName());
    }
}
