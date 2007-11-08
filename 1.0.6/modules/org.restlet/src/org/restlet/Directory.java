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
 * representations accessed by the file system or the class loaders.<br/>
 * 
 * An automatic content negotiation mechanism (similar to the one in Apache HTTP
 * server) is used to select the best representation of a resource based on the
 * available variants and on the client capabilities and preferences.<br/>
 * 
 * The directory can be used in read-only or modifiable mode. In the latter
 * case, you just need to set the "modifiable" property to true. The currently
 * supported methods are PUT and DELETE.
 * 
 * @see <a href="http://www.restlet.org/tutorial#part06">Tutorial: Serving
 *      static files</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Directory extends Finder {
	/** Indicates if the best content is automatically negotiated. */
	private boolean negotiateContent;

	/** Indicates if the subdirectories are deeply accessible (true by default). */
	private boolean deeplyAccessible;

	/** The index name, without extensions (ex: "index" or "home"). */
	private String indexName;

	/** The absolute root reference (file, clap URI). */
	private Reference rootRef;

	/**
	 * Indicates if modifications to local resources are allowed (false by
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
	 * @param rootLocalReference
	 *            The root URI.
	 */
	public Directory(Context context, LocalReference rootLocalReference) {
		super(context);

		// First, let's normalize the root reference to prevent any issue with
		// relative paths inside the reference leading to listing issues.
		String rootIdentifier = rootLocalReference.getTargetRef()
				.getIdentifier();

		if (rootIdentifier.endsWith("/")) {
			this.rootRef = new Reference(rootIdentifier);
		} else {
			// We don't take the risk of exposing directory "file:///C:/AA"
			// if only "file:///C:/A" was intended
			this.rootRef = new Reference(rootIdentifier + "/");
		}

		this.deeplyAccessible = true;
		this.indexName = "index";
		this.listingAllowed = false;
		this.modifiable = false;
	}

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
	 *            Component. On Windows, make sure that you add enough slash
	 *            characters at the beginning, for example: file:///c:/dir/file<br>
	 *            <br>
	 *            If you serve files from a class loader, use clap:// URIs and
	 *            make sure that you register a CLAP connector with your parent
	 *            Component.<br>
	 *            <br>
	 */
	public Directory(Context context, String rootUri) {
		this(context, new LocalReference(rootUri));
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
	 * Returns the index name, without extensions. Returns "index" by default.
	 * 
	 * @return The index name.
	 */
	public String getIndexName() {
		return this.indexName;
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
		result.add(new Variant(MediaType.TEXT_HTML));
		result.add(new Variant(MediaType.TEXT_URI_LIST));
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
	 * Indicates if modifications to local resources (most likely files) are
	 * allowed. Returns false by default.
	 * 
	 * @return True if modifications to local resources are allowed.
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
	 * Sets the index name, without extensions.
	 * 
	 * @param indexName
	 *            The index name.
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
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
	 * Indicates if modifications to local resources are allowed.
	 * 
	 * @param modifiable
	 *            True if modifications to local resources are allowed.
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
