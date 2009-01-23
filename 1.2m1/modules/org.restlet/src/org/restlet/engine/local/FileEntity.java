/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;

/**
 * Local entity based on a regular {@link File}.
 */
public class FileEntity extends Entity {

    /** The underlying regular file. */
    private final File file;

    /**
     * Constructor.
     * 
     * @param file
     *            The underlying file.
     */
    public FileEntity(File file) {
        this.file = file;
    }

    @Override
    public boolean exists() {
        return getFile().exists();
    }

    @Override
    public List<Entity> getChildren() {
        List<Entity> result = null;

        if (getFile().isDirectory()) {
            result = new ArrayList<Entity>();

            for (File f : getFile().listFiles()) {
                result.add(new FileEntity(f));
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
        return (parentFile == null) ? null : new FileEntity(parentFile);
    }

    @Override
    public Representation getRepresentation(MediaType defaultMediaType,
            int timeToLive) {
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
