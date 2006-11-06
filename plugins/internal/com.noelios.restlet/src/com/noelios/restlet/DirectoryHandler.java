/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Handler;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Result;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.util.ReferenceList;
import org.restlet.util.Dispatcher;

/**
 * Handler supported by a directory of resource (from the file system, the web application context or 
 * class loaders). An automatic content negotiation mechanism (similar to the one in Apache HTTP server) is 
 * used to select the best representation of a resource based on the available variants and on the client 
 * capabilities and preferences.
 * @see <a href="http://www.restlet.org/tutorial#part06">Tutorial: Serving context resources</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @deprecated Use standard org.restlet.Directory class instead.
 */
@Deprecated
public class DirectoryHandler extends Handler
{
	/** If no file name is specified, use the (optional) index name. */
	private String indexName;

	/** Indicates if the subdirectories are deeply accessible (true by default). */
	private boolean deeplyAccessible;

	/** The absolute root URI, including the "file://" or "context://" scheme. */
	private String rootUri;

	/** Indicates if modifications to context resources are allowed (false by default). */
	private boolean modifiable;

	/** Indicates if the display of directory listings is allowed when no index file is found. */
	private boolean listingAllowed;

	/** Indicates if content negotation should be enabled (false by default). */
	private boolean negotiationEnabled;

	/**
	 * Constructor.
	 * @param context The context.
	 * @param rootUri The absolute root Uri, including the "file://" or "context://" scheme.
	 * @param indexName If no file name is specified, use the (optional) index name.
	 */
	public DirectoryHandler(Context context, String rootUri, String indexName)
	{
		super(context);
		this.indexName = indexName;

		if (rootUri.endsWith("/"))
		{
			this.rootUri = rootUri;
		}
		else
		{
			// We don't take the risk of exposing directory "file:///C:/AA" 
			// if only "file:///C:/A" was intended
			this.rootUri = rootUri + "/";
		}

		this.deeplyAccessible = true;
		this.modifiable = false;
		this.listingAllowed = false;
		this.negotiationEnabled = false;
	}

	/**
	 * Finds the target Resource if available.
	 * @param request The request to filter.
	 * @param response The response to filter.
	 * @return The target resource if available or null.
	 */
	public Resource findTarget(Request request, Response response)
	{
		try
		{
			return new DirectoryResource(getLogger(), this, request);
		}
		catch (IOException ioe)
		{
			getLogger().log(Level.WARNING, "Unable to find the directory's resource", ioe);
			return null;
		}
	}

	/**
	 * Indicates if the display of directory listings is allowed when no index file is found.
	 * @return True if the display of directory listings is allowed when no index file is found.
	 */
	public boolean isListingAllowed()
	{
		return this.listingAllowed;
	}

	/**
	 * Indicates if the display of directory listings is allowed when no index file is found.
	 * @param listingAllowed True if the display of directory listings is allowed when no index file is found.
	 */
	public void setListingAllowed(boolean listingAllowed)
	{
		this.listingAllowed = listingAllowed;
	}

	/** 
	 * Indicates if content negotation should be enabled.
	 * @return True if content negotation should be enabled.
	 */
	public boolean isNegotiationEnabled()
	{
		return this.negotiationEnabled;
	}

	/** 
	 * Indicates if content negotation should be enabled.
	 * @param negotiationEnabled True if content negotation should be enabled.
	 */
	public void setNegotiationEnabled(boolean negotiationEnabled)
	{
		this.negotiationEnabled = negotiationEnabled;
	}

	/** 
	 * Indicates if modifications to context resources are allowed.
	 * @return True if modifications to context resources are allowed.
	 */
	public boolean isModifiable()
	{
		return this.modifiable;
	}

	/** 
	 * Indicates if modifications to context resources are allowed.
	 * @param modifiable True if modifications to context resources are allowed.
	 */
	public void setModifiable(boolean modifiable)
	{
		this.modifiable = modifiable;
	}

	/**
	 * Returns the index name.
	 * @return The index name.
	 */
	public String getIndexName()
	{
		return indexName;
	}

	/**
	 * Sets the index name.
	 * @param indexName The index name.
	 */
	public void setIndexName(String indexName)
	{
		this.indexName = indexName;
	}

	/**
	 * Returns the root URI.
	 * @return The root URI.
	 */
	public String getRootUri()
	{
		return rootUri;
	}

	/**
	 * Indicates if the subdirectories are deeply accessible (true by default).
	 * @return True if the subdirectories are deeply accessible.
	 */
	public boolean isDeeplyAccessible()
	{
		return deeplyAccessible;
	}

	/**
	 * Indicates if the subdirectories are deeply accessible (true by default).
	 * @param deeplyAccessible True if the subdirectories are deeply accessible.
	 */
	public void setDeeplyAccessible(boolean deeplyAccessible)
	{
		this.deeplyAccessible = deeplyAccessible;
	}

	/**
	 * Returns the variant representations of a directory. This method can be subclassed in order to provide
	 * alternative representations.
	 * @param directoryContent The list of references contained in the directory.
	 * @return The variant representations of a directory.
	 */
	public List<Representation> getDirectoryVariants(ReferenceList directoryContent)
	{
		// Create a simple HTML list
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>\n");

		sb.append("<h1>Listing of directory \"" + directoryContent.getListRef().getPath()
				+ "\"</h1>\n");

		Reference parentRef = directoryContent.getListRef().getParentRef();

		if (!parentRef.equals(directoryContent.getListRef()))
		{
			sb.append("<a href=\"" + parentRef + "\">..</a><br/>\n");
		}

		for (Reference ref : directoryContent)
		{
			sb.append("<a href=\"" + ref.toString() + "\">"
					+ ref.getRelativeRef(directoryContent.getListRef()) + "</a><br/>\n");
		}
		sb.append("</body></html>\n");

		// Create the variants list
		List<Representation> result = new ArrayList<Representation>();
		result.add(new StringRepresentation(sb.toString(), MediaType.TEXT_HTML));
		return result;
	}

}

/**
 * Resource supported by a set of context representations (from file system, class loaders and webapp context). 
 * A content negotiation mechanism (similar to Apache HTTP server) is available. It is based on path extensions 
 * to detect variants (languages, media types or character sets).
 * @see <a href="http://httpd.apache.org/docs/2.0/content-negotiation.html">Apache mod_negotiation module</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @author Thierry Boileau
 */
final class DirectoryResource extends Resource
{
	/** The handled request. */
	private Request request;

	/** The parent directory handler. */
	private DirectoryHandler handler;

	/** The resource path relative to the directory URI. */
	private String relativePart;

	/** The context's target URI. For example, "file:///c:/dir/foo.en" or "context://webapp/dir/foo.en". */
	private String targetUri;

	/** Indicates if the target resource is a directory or a file. */
	private boolean targetDirectory;

	/** The context's directory URI. For example, "file:///c:/dir/" or "context://webapp/dir/". */
	private String directoryUri;

	/** The local base name of the resource. For example, "foo.en" and "foo.en-GB.html" return "foo". */
	private String baseName;

	/** The base set of extensions. */
	private Set<String> baseExtensions;

	/** If the resource is a directory, this contains its content. */
	private ReferenceList directoryContent;

	/**
	 * Constructor.
	 * @param logger The logger to use.
	 * @param handler The parent directory handler.
	 * @param request The handled call.
	 * @throws IOException 
	 */
	public DirectoryResource(Logger logger, DirectoryHandler handler, Request request)
			throws IOException
	{
		super(logger);

		// Update the member variables
		this.handler = handler;
		this.request = request;
		this.relativePart = request.getRelativePart();

		if (this.relativePart.startsWith("/"))
		{
			// We enforce the leading slash on the root URI
			this.relativePart = this.relativePart.substring(1);
		}

		this.targetUri = new Reference(handler.getRootUri() + this.relativePart)
				.normalize().toString();
		if (!this.targetUri.startsWith(handler.getRootUri()))
		{
			// Prevent the client from accessing resources in upper directories
			this.targetUri = handler.getRootUri();
		}

		// Try to detect the presence of a directory
		Response contextResponse = getClient().get(this.targetUri);
		if ((contextResponse.getEntity() != null)
				&& contextResponse.getEntity().getMediaType().equals(MediaType.TEXT_URI_LIST))
		{
			this.targetDirectory = true;
			this.directoryContent = new ReferenceList(contextResponse.getEntity());

			if (!this.targetUri.endsWith("/"))
			{
				this.targetUri += "/";
				this.relativePart += "/";
			}

			// Append the index name
			if (getDirectory().getIndexName() != null)
			{
				this.directoryUri = this.targetUri;
				this.baseName = getDirectory().getIndexName();
				this.targetUri = this.directoryUri + this.baseName;
			}
			else
			{
				this.directoryUri = this.targetUri;
				this.baseName = null;
			}
		}
		else
		{
			this.targetDirectory = false;
			int lastSlashIndex = targetUri.lastIndexOf('/');
			if (lastSlashIndex == -1)
			{
				this.directoryUri = "";
				this.baseName = targetUri;
			}
			else
			{
				this.directoryUri = targetUri.substring(0, lastSlashIndex + 1);
				this.baseName = targetUri.substring(lastSlashIndex + 1);
			}

			contextResponse = getClient().get(this.directoryUri);
			if ((contextResponse.getEntity() != null)
					&& contextResponse.getEntity().getMediaType().equals(
							MediaType.TEXT_URI_LIST))
			{
				this.directoryContent = new ReferenceList(contextResponse.getEntity());
			}
		}

		if (this.handler.isNegotiationEnabled())
		{
			// Remove the extensions from the base name
			int firstDotIndex = this.baseName.indexOf('.');
			if (firstDotIndex != -1)
			{
				// Store the set of extensions
				this.baseExtensions = getExtensions(this.baseName);

				// Remove stored extensions from the base name
				this.baseName = this.baseName.substring(0, firstDotIndex);
			}
		}

		// Log results
		logger.info("Converted base path: " + this.targetUri);
		logger.info("Converted base name: " + this.baseName);
	}

	/**
	 * Indicates if it is allowed to delete the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowDelete()
	{
		return getDirectory().isModifiable();
	}

	/**
	 * Indicates if it is allowed to put to the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowPut()
	{
		return getDirectory().isModifiable();
	}

	/**
	 * Returns the client interface.
	 * @return The client interface.
	 */
	private Dispatcher getClient()
	{
		return getDirectory().getContext().getDispatcher();
	}

	/**
	 * Returns the parent directory handler.
	 * @return The parent directory handler.
	 */
	public DirectoryHandler getDirectory()
	{
		return this.handler;
	}

	/**
	 * Returns the context's target URI. For example, "file:///c:/dir/foo.en" or "context://webapp/dir/foo.en".
	 * @return The context's target URI.
	 */
	public String getTargetUri()
	{
		return this.targetUri;
	}

	/**
	 * Sets the context's target URI. For example, "file:///c:/dir/foo.en" or "context://webapp/dir/foo.en".
	 * @param baseUri The context's target URI.
	 */
	public void setTargetUri(String baseUri)
	{
		this.targetUri = baseUri;
	}

	/**
	 * Returns the context's directory URI. For example, "file:///c:/dir/" or "context://webapp/dir/".
	 * @return The context's directory URI. For example, "file:///c:/dir/" or "context://webapp/dir/".
	 */
	public String getDirectoryUri()
	{
		return this.directoryUri;
	}

	/**
	 * Returns the local base name of the file. For example, "foo.en" and "foo.en-GB.html" return "foo".
	 * @return The local name of the file.
	 */
	public String getBaseName()
	{
		return this.baseName;
	}

	/**
	 * Handles a DELETE call.
	 * @param request The call to handle.
	 */
	protected void handleDelete(Request request, Response response)
	{
		// We allow the transfer of the DELETE calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// Delete all the resource's representations
			// TODO
		}
	}

	/**
	 * Handles a PUT call.
	 * @param request The call to handle.
	 */
	protected void handlePut(Request request, Response response)
	{
		// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// Remplace a similar representation or create a new one
			// TODO
		}
	}

	/**
	 * Default implementation for all the handle*() methods that simply calls the nextHandle() method. 
	 * @param request The call to handle.
	 */
	protected void defaultHandle(Request request, Response response)
	{
		response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	/**
	 * Returns the representation variants.
	 * @return The representation variants.
	 */
	public List<Representation> getVariants()
	{
		List<Representation> result = super.getVariants();
		getLogger().info("Getting variants for : " + getTargetUri());

		if (this.directoryContent != null)
		{
			if (this.baseName != null)
			{
				for (Reference ref : getVariantsReferences(false))
				{
					//Add the new variant to the result list
					Response contextResponse = getClient().get(ref.toString());
					if (contextResponse.getStatus().isSuccess()
							&& (contextResponse.getEntity() != null))
					{
						result.add(contextResponse.getEntity());
					}
				}
			}

			if (result.size() == 0)
			{
				if (this.targetDirectory && getDirectory().isListingAllowed())
				{
					ReferenceList userList = new ReferenceList(this.directoryContent.size());

					// Compute the base reference (from a call's client point of view) 
					String baseRef = this.request.getBaseRef().toString(false, false);
					if (!baseRef.endsWith("/"))
					{
						baseRef += "/";
					}
					baseRef += this.relativePart;

					// Set the list base reference
					userList.setListRef(baseRef);

					String filePath;
					int rootLength = getDirectoryUri().length();

					for (Reference ref : this.directoryContent)
					{
						filePath = ref.toString(false, false).substring(rootLength);
						userList.add(baseRef + filePath);
					}

					result = getDirectory().getDirectoryVariants(userList);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the set of extensions contained in a given directory entry name.
	 * @param entryName The directory entry name.
	 * @return The set of extensions.
	 */
	public static Set<String> getExtensions(String entryName)
	{
		Set<String> result = new TreeSet<String>();
		String[] tokens = entryName.split("\\.");
		for (int i = 1; i < tokens.length; i++)
		{
			result.add(tokens[i]);
		}
		return result;
	}

	/**
	 * Puts a variant representation in the resource.
	 * @param variant A new or updated variant representation. 
	 * @return The result information.
	 */
	public Result put(Representation variant)
	{
		Status status;

		// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			status = Status.CLIENT_ERROR_FORBIDDEN;
		}
		else
		{
			Request contextRequest = new Request(Method.PUT, this.targetUri);
			Response contextResponse = new Response(contextRequest);

			contextRequest.setEntity(variant);
			if (targetDirectory)
			{
				contextRequest.setResourceRef(this.targetUri);
				getClient().handle(contextRequest, contextResponse);
			}
			else
			{
				//Try to get the unique representation of the resource, if any
				ReferenceList references = getVariantsReferences(true);
				if (!references.isEmpty())
				{
					contextRequest.setResourceRef(references.get(0));
					getClient().handle(contextRequest, contextResponse);
				}
				else
				{
					contextRequest.setResourceRef(this.targetUri);
					getClient().handle(contextRequest, contextResponse);
				}
			}

			status = contextResponse.getStatus();
		}

		return new Result(status);
	}

	/**
	 * Asks the resource to delete itself and all its representations.
	 * @return The result information. 
	 */
	public Result delete()
	{
		Status status;

		// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			status = Status.CLIENT_ERROR_FORBIDDEN;
		}
		else
		{
			Request contextRequest = new Request(Method.DELETE, this.targetUri);
			Response contextResponse = new Response(contextRequest);

			if (targetDirectory)
			{
				contextRequest.setResourceRef(this.targetUri);
				getClient().handle(contextRequest, contextResponse);
			}
			else
			{
				//Try to get the unique representation of the resource
				ReferenceList references = getVariantsReferences(true);
				if (!references.isEmpty())
				{
					contextRequest.setResourceRef(references.get(0));
					getClient().handle(contextRequest, contextResponse);
				}
				else
				{
					contextResponse.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			}

			status = contextResponse.getStatus();
		}

		return new Result(status);
	}

	/**
	 * Returns the references of the representations of the target resource
	 * according to the directory handler property
	 * @param unique tells wether looking or not for the unique reference
	 * @return The list of variants references
	 */
	private ReferenceList getVariantsReferences(boolean unique)
	{
		ReferenceList result = new ReferenceList(0);
		try
		{
			Request contextCall = new Request(Method.GET, this.targetUri);
			contextCall.getClientInfo().getAcceptedMediaTypes().add(
					new Preference<MediaType>(MediaType.TEXT_URI_LIST));
			Response contextResponse = getClient().handle(contextCall);
			if (contextResponse.getEntity() != null)
			{
				ReferenceList listVariants = new ReferenceList(contextResponse.getEntity());
				Set<String> extensions = null;
				String entryUri;
				String fullEntryName;
				String baseEntryName;
				int lastSlashIndex;
				int firstDotIndex;
				for (Reference ref : listVariants)
				{
					entryUri = ref.toString();
					lastSlashIndex = entryUri.lastIndexOf('/');
					fullEntryName = (lastSlashIndex == -1) ? entryUri : entryUri
							.substring(lastSlashIndex + 1);
					baseEntryName = fullEntryName;

					if (this.handler.isNegotiationEnabled())
					{
						// Remove the extensions from the base name
						firstDotIndex = fullEntryName.indexOf('.');
						if (firstDotIndex != -1)
						{
							baseEntryName = fullEntryName.substring(0, firstDotIndex);
						}
					}

					// Check if the current file is a valid variant
					if (baseEntryName.equals(this.baseName))
					{
						boolean validVariant = true;

						if (this.handler.isNegotiationEnabled())
						{
							// Verify that the extensions are compatible
							extensions = getExtensions(fullEntryName);
							validVariant = (((extensions == null) && (this.baseExtensions == null))
									|| (this.baseExtensions == null) || extensions
									.containsAll(this.baseExtensions));
							if (unique && validVariant)
							{
								validVariant = this.baseExtensions.containsAll(extensions);
							}
						}

						if (validVariant)
						{
							result.add(ref);
						}
					}
				}
			}
		}
		catch (IOException ioe)
		{
			getLogger().log(Level.WARNING, "Unable to get resource variants", ioe);
		}

		return result;
	}
}
