/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.local;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Local entity based on an entry in a Zip archive.
 * 
 * @author Remi Dewitte remi@gide.net
 */
public class ZipEntryEntity extends Entity {

	/** The Zip entry. */
	protected final ZipEntry entry;

	/** The Zip file. */
	protected final ZipFile zipFile;

	/**
	 * Constructor.
	 * 
	 * @param zipFile         The Zip file.
	 * @param entryName       The Zip entry name.
	 * @param metadataService The metadata service to use.
	 */
	public ZipEntryEntity(ZipFile zipFile, String entryName, MetadataService metadataService) {
		super(metadataService);
		this.zipFile = zipFile;
		ZipEntry entry = zipFile.getEntry(entryName);
		if (entry == null)
			this.entry = new ZipEntry(entryName);
		else {
			// Checking we don't have a directory
			ZipEntry entryDir = zipFile.getEntry(entryName + "/");
			if (entryDir != null)
				this.entry = entryDir;
			else
				this.entry = entry;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param zipFile         The Zip file.
	 * @param entry           The Zip entry.
	 * @param metadataService The metadata service to use.
	 */
	public ZipEntryEntity(ZipFile zipFile, ZipEntry entry, MetadataService metadataService) {
		super(metadataService);
		this.zipFile = zipFile;
		this.entry = entry;
	}

	@Override
	public boolean exists() {
		if ("".equals(getName()))
			return true;
		// ZipEntry re = zipFile.getEntry(entry.getName());
		// return re != null;
		return entry.getSize() != -1;
	}

	@Override
	public List<Entity> getChildren() {
		List<Entity> result = null;

		if (isDirectory()) {
			result = new ArrayList<Entity>();
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			String n = entry.getName();
			while (entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();
				if (e.getName().startsWith(n) && e.getName().length() != n.length())
					result.add(new ZipEntryEntity(zipFile, e, getMetadataService()));
			}
		}

		return result;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public Entity getParent() {
		if (entry.getName().isEmpty())
			return null;

		String n = entry.getName();
		String pn = n.substring(0, n.lastIndexOf('/') + 1);
		return new ZipEntryEntity(zipFile, zipFile.getEntry(pn), getMetadataService());
	}

	@Override
	public Representation getRepresentation(MediaType defaultMediaType, int timeToLive) {
		return new ZipEntryRepresentation(defaultMediaType, zipFile, entry, timeToLive);
	}

	@Override
	public boolean isDirectory() {
		if (entry.getName().isEmpty())
			return true;
		return entry.isDirectory();
	}

	@Override
	public boolean isNormal() {
		return !entry.isDirectory();
	}

}
