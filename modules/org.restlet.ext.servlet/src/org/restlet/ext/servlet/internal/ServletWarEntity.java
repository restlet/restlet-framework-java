/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.servlet.internal;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletContext;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.local.Entity;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Local entity based on a Servlet context's resource file.
 * 
 * @author Thierry Boileau
 */
public class ServletWarEntity extends Entity {
    /**
     * List of children files if it is a directory. We suppose that in a WAR
     * entity, this list does not change, and thus can be cached during the
     * request processing.
     */
    private List<Entity> children = null;

    /** Is this file a directory? */
    private final boolean directory;

    /** The full name of the file (without trailing "/"). */
    private final String fullName;

    /** The relative path of the file inside the context. */
    private final String path;

    /** The Servlet context to use. */
    private final ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param servletContext
     *            The parent Servlet context.
     * @param path
     *            The entity path.
     * @param metadataService
     *            The metadata service to use.
     */
    public ServletWarEntity(ServletContext servletContext, String path,
            MetadataService metadataService) {
        super(metadataService);
        this.children = null;
        this.servletContext = servletContext;
        this.path = path;

        if (path.endsWith("/")) {
            this.directory = true;
            this.fullName = path.substring(0, path.length() - 1);
            Set<?> childPaths = getServletContext().getResourcePaths(path);

            if (childPaths != null && !childPaths.isEmpty()) {
                this.children = new ArrayList<Entity>();

                for (Object childPath : childPaths) {
                    if (!childPath.equals(this.path)) {
                        this.children.add(new ServletWarEntity(
                                this.servletContext, (String) childPath,
                                metadataService));
                    }
                }
            }
        } else {
            this.fullName = path;
            Set<?> childPaths = getServletContext().getResourcePaths(path);

            if (childPaths != null && !childPaths.isEmpty()) {
                this.directory = true;
                this.children = new ArrayList<Entity>();

                for (Object childPath : childPaths) {
                    if (!childPath.equals(this.path)) {
                        this.children.add(new ServletWarEntity(
                                this.servletContext, (String) childPath,
                                metadataService));
                    }
                }
            } else {
                this.directory = false;
            }
        }
    }

    @Override
    public boolean exists() {
        boolean result = false;

        try {
            result = (isDirectory() && getChildren() != null)
                    || (isNormal() && getServletContext()
                            .getResource(this.path) != null);
        } catch (MalformedURLException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to test the existence of the WAR resource", e);
        }

        return result;
    }

    @Override
    public List<Entity> getChildren() {
        return this.children;
    }

    @Override
    public String getName() {
        int index = this.fullName.lastIndexOf("/");

        if (index != -1) {
            return this.fullName.substring(index + 1);
        }

        return this.fullName;
    }

    @Override
    public Entity getParent() {
        Entity result = null;
        int index = this.fullName.lastIndexOf("/");

        if (index != -1) {
            result = new ServletWarEntity(getServletContext(), this.fullName
                    .substring(0, index + 1), getMetadataService());
        }

        return result;
    }

    @Override
    public Representation getRepresentation(MediaType defaultMediaType,
            int timeToLive) {
        Representation result = null;

        InputStream ris = getServletContext().getResourceAsStream(path);
        if (ris != null) {
            result = new InputRepresentation(ris, defaultMediaType);
            // Sets the modification date
            String realPath = getServletContext().getRealPath(path);
            if (realPath != null) {
                File file = new File(realPath);
                if (file != null) {
                    result.setModificationDate(new Date(file.lastModified()));
                }
            }
        }
        return result;
    }

    /**
     * Returns the Servlet context to use.
     * 
     * @return The Servlet context to use.
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public boolean isDirectory() {
        return this.directory;
    }

    @Override
    public boolean isNormal() {
        return !isDirectory();
    }

}
