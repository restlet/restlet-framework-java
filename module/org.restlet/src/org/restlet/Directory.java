/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.util.Engine;

/**
 * Finder mapping a directory of local resources. Those resources have
 * representations accessed by the file system, the WAR context or the class
 * loaders. An automatic content negotiation mechanism (similar to the one in
 * Apache HTTP server) is used to select the best representation of a resource
 * based on the available variants and on the client capabilities and
 * preferences.
 * 
 * @see <a href="http://www.restlet.org/tutorial#part06">Tutorial: Serving
 *      context resources</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Directory extends Finder {
    /** Indicates if the best content is automatically negotiated. */
    private boolean negotiateContent;

    /** Indicates if the subdirectories are deeply accessible (true by default). */
    private boolean deeplyAccessible;

    /** The absolute root reference (file, clap or war URI). */
    private Reference rootRef;

    /**
     * Indicates if modifications to context resources are allowed (false by
     * default).
     */
    private boolean modifiable;

    /**
     * Indicates if the display of directory listings is allowed when no index
     * file is found.
     */
    private boolean listingAllowed;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param rootUri
     *            The absolute root URI. <br>
     *            <br>
     *            If you serve files from the file system, use file:// URIs and
     *            make sure that you register a FILE connector with your parent
     *            Component. <br>
     *            <br>
     *            If you serve files from a class loader, use clap:// URIs and
     *            make sure that you register a CLAP connector with your parent
     *            Component.<br>
     *            <br>
     *            If you serve files from the application's WAR (from a zipped
     *            file or unzipped in a directory), use war:// URIs and then you
     *            shouldn't have to register a WAR connector as it is
     *            automatically provided for applications. However, for WAR
     *            URIs, you have to define, in the application context, a
     *            parameter named "warPath" that contains either the location of
     *            your zipped WAR file or the root directory of your unzipped
     *            WAR. If you don't define this parameter, it will use the
     *            ${user.home}/restlet.war value by default. Note that this
     *            parameter shouldn't directly be set by the Application but by
     *            the parent Component so that the Application can be portable
     *            to new environments.
     */
    public Directory(Context context, String rootUri) {
        super(context);

        if (rootUri.endsWith("/")) {
            this.rootRef = new Reference(rootUri);
        } else {
            // We don't take the risk of exposing directory "file:///C:/AA"
            // if only "file:///C:/A" was intended
            this.rootRef = new Reference(rootUri + "/");
        }

        this.deeplyAccessible = true;
        this.modifiable = false;
        this.listingAllowed = false;
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param rootLocalReference
     *            The root Uri.
     */
    public Directory(Context context, LocalReference rootLocalReference) {
        super(context);

        if (rootLocalReference.getIdentifier().endsWith("/")) {
            this.rootRef = new Reference(rootLocalReference.getIdentifier());
        } else {
            // We don't take the risk of exposing directory "file:///C:/AA"
            // if only "file:///C:/A" was intended
            this.rootRef = new Reference(rootLocalReference.getIdentifier()
                    + "/");
        }

        this.deeplyAccessible = true;
        this.modifiable = false;
        this.listingAllowed = false;
    }

    /**
     * Finds the target Resource if available.
     * 
     * @param request
     *            The request to filter.
     * @param response
     *            The response to filter.
     * @return The target resource if available or null.
     */
    public Resource findTarget(Request request, Response response) {
        try {
            return Engine.getInstance().createDirectoryResource(this, request,
                    response);
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING,
                    "Unable to find the directory's resource", ioe);
            return null;
        }
    }

    /**
     * Returns the variant representations of a directory index. This method can
     * be subclassed in order to provide alternative representations. By default
     * it returns a simple HTML document and a textual URI list as variants.
     * 
     * @param indexContent
     *            The list of references contained in the directory index.
     * @return The variant representations of a directory.
     */
    public List<Variant> getIndexVariants(ReferenceList indexContent) {
        List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.TEXT_URI_LIST));
        result.add(new Variant(MediaType.TEXT_HTML));
        return result;
    }

    /**
     * Returns an actual index representation for a given variant.
     * 
     * @param variant
     *            The selected variant.
     * @param indexContent
     *            The directory index to represent.
     * @return The actual index representation.
     */
    public Representation getIndexRepresentation(Variant variant,
            ReferenceList indexContent) {
        Representation result = null;
        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            result = indexContent.getWebRepresentation();
        } else if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
            result = indexContent.getTextRepresentation();
        }
        return result;
    }

    /**
     * Returns the root URI.
     * 
     * @return The root URI.
     */
    public Reference getRootRef() {
        return this.rootRef;
    }

    /**
     * Indicates if the subdirectories are deeply accessible (true by default).
     * 
     * @return True if the subdirectories are deeply accessible.
     */
    public boolean isDeeplyAccessible() {
        return deeplyAccessible;
    }

    /**
     * Indicates if the display of directory listings is allowed when no index
     * file is found.
     * 
     * @return True if the display of directory listings is allowed when no
     *         index file is found.
     */
    public boolean isListingAllowed() {
        return this.listingAllowed;
    }

    /**
     * Indicates if modifications to context resources are allowed.
     * 
     * @return True if modifications to context resources are allowed.
     */
    public boolean isModifiable() {
        return this.modifiable;
    }

    /**
     * Indicates if the best content is automatically negotiated. Default value
     * is true.
     * 
     * @return True if the best content is automatically negotiated.
     */
    public boolean isNegotiateContent() {
        return this.negotiateContent;
    }

    /**
     * Indicates if the subdirectories are deeply accessible (true by default).
     * 
     * @param deeplyAccessible
     *            True if the subdirectories are deeply accessible.
     */
    public void setDeeplyAccessible(boolean deeplyAccessible) {
        this.deeplyAccessible = deeplyAccessible;
    }

    /**
     * Indicates if the display of directory listings is allowed when no index
     * file is found.
     * 
     * @param listingAllowed
     *            True if the display of directory listings is allowed when no
     *            index file is found.
     */
    public void setListingAllowed(boolean listingAllowed) {
        this.listingAllowed = listingAllowed;
    }

    /**
     * Indicates if modifications to context resources are allowed.
     * 
     * @param modifiable
     *            True if modifications to context resources are allowed.
     */
    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    /**
     * Indicates if the best content is automatically negotiated. Default value
     * is true.
     * 
     * @param negotiateContent
     *            True if the best content is automatically negotiated.
     */
    public void setNegotiateContent(boolean negotiateContent) {
        this.negotiateContent = negotiateContent;
    }

}
