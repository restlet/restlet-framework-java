/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.local;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Local entity based on an entry in a Zip archive.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class ZipEntryEntity extends Entity {

    /** The Zip entry. */
    protected final ZipEntry entry;

    /** The Zip file. */
    protected final ZipFile zipFile;

    /**
     * Constructor.
     * 
     * @param zipFile
     *            The Zip file.
     * @param entryName
     *            The Zip entry name.
     * @param metadataService
     *            The metadata service to use.
     */
    public ZipEntryEntity(ZipFile zipFile, String entryName,
            MetadataService metadataService) {
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
     * @param zipFile
     *            The Zip file.
     * @param entry
     *            The Zip entry.
     * @param metadataService
     *            The metadata service to use.
     */
    public ZipEntryEntity(ZipFile zipFile, ZipEntry entry,
            MetadataService metadataService) {
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
                if (e.getName().startsWith(n)
                        && e.getName().length() != n.length())
                    result.add(new ZipEntryEntity(zipFile, e,
                            getMetadataService()));
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
        if ("".equals(entry.getName()))
            return null;

        String n = entry.getName();
        String pn = n.substring(0, n.lastIndexOf('/') + 1);
        return new ZipEntryEntity(zipFile, zipFile.getEntry(pn),
                getMetadataService());
    }

    @Override
    public Representation getRepresentation(MediaType defaultMediaType,
            int timeToLive) {
        return new ZipEntryRepresentation(defaultMediaType, zipFile, entry,
                timeToLive);
    }

    @Override
    public boolean isDirectory() {
        if ("".equals(entry.getName()))
            return true;
        return entry.isDirectory();
    }

    @Override
    public boolean isNormal() {
        return !entry.isDirectory();
    }

}
