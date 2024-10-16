/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.engine.Edition;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Local entity based on a regular {@link File}.
 */
public class FileEntity extends Entity {

	/** The underlying regular file. */
	private final File file;

	/**
	 * Constructor.
	 * 
	 * @param file            The underlying file.
	 * @param metadataService The metadata service to use.
	 */
	public FileEntity(File file, MetadataService metadataService) {
		super(metadataService);
		this.file = file;
	}

	@Override
	public boolean exists() {
		return getFile().exists();
	}

	@Override
	public List<Entity> getChildren() {
		List<Entity> result = null;

		if (isDirectory()) {
			result = new ArrayList<Entity>();

			for (File f : getFile().listFiles()) {
				result.add(new FileEntity(f, getMetadataService()));
			}
		}

		return result;
	}

	/**
	 * Returns the underlying regular file.
	 * 
	 * @return The underlying regular file.
	 */
	public File getFile() {
		return file;
	}

	@Override
	public String getName() {
		return getFile().getName();
	}

	@Override
	public Entity getParent() {
		File parentFile = getFile().getParentFile();
		return (parentFile == null) ? null : new FileEntity(parentFile, getMetadataService());
	}

	@Override
	public Representation getRepresentation(MediaType defaultMediaType, int timeToLive) {
		return new FileRepresentation(getFile(), defaultMediaType, timeToLive);
	}

	@Override
	public boolean isDirectory() {
		return getFile().isDirectory();
	}

	@Override
	public boolean isNormal() {
		return getFile().isFile();
	}
}
