/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
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
