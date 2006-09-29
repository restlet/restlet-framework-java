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

package com.noelios.restlet.data;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.data.AbstractResource;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;
import org.restlet.data.Result;
import org.restlet.data.Status;

import com.noelios.restlet.DirectoryFinder;

/**
 * Resource supported by a set of context representations (from file system, class loaders and webapp context). 
 * A content negotiation mechanism (similar to Apache HTTP server) is available. It is based on path extensions 
 * to detect variants (languages, media types or character sets).
 * @see <a href="http://httpd.apache.org/docs/2.0/content-negotiation.html">Apache mod_negotiation module</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @author Thierry Boileau
 */
public class DirectoryResource extends AbstractResource
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(DirectoryResource.class
			.getCanonicalName());

	/** The handled call. */
	private Call call;

	/** The parent directory handler. */
	private DirectoryFinder handler;

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
	 * @param handler The parent directory handler.
	 * @param call The handled call.
	 * @throws IOException 
	 */
	public DirectoryResource(DirectoryFinder handler, Call call) throws IOException
	{
		// Update the member variables
		this.handler = handler;
		this.call = call;
		this.relativePart = call.getRelativePart();

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
		Call contextCall = getDirectory().getContext().get(this.targetUri);
		if ((contextCall.getOutput() != null)
				&& contextCall.getOutput().getMediaType().equals(MediaType.TEXT_URI_LIST))
		{
			this.targetDirectory = true;
			this.directoryContent = new ReferenceList(contextCall.getOutput());

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

			contextCall = getDirectory().getContext().get(this.directoryUri);
			if ((contextCall.getOutput() != null)
					&& contextCall.getOutput().getMediaType().equals(MediaType.TEXT_URI_LIST))
			{
				this.directoryContent = new ReferenceList(contextCall.getOutput());
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
	 * Returns the parent directory handler.
	 * @return The parent directory handler.
	 */
	public DirectoryFinder getDirectory()
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
	 * @param call The call to handle.
	 */
	protected void handleDelete(Call call)
	{
		// We allow the transfer of the DELETE calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			call.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// Delete all the resource's representations
			// TODO
		}
	}

	/**
	 * Handles a PUT call.
	 * @param call The call to handle.
	 */
	protected void handlePut(Call call)
	{
		// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			call.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// Remplace a similar representation or create a new one
			// TODO
		}
	}

	/**
	 * Default implementation for all the handle*() methods that simply calls the nextHandle() method. 
	 * @param call The call to handle.
	 */
	protected void defaultHandle(Call call)
	{
		call.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	/**
	 * Returns the representation variants.
	 * @return The representation variants.
	 */
	public List<Representation> getVariants()
	{
		List<Representation> result = super.getVariants();
		logger.info("Getting variants for : " + getTargetUri());

		if (this.directoryContent != null)
		{
			if (this.baseName != null)
			{
				for (Reference ref : getVariantsReferences(false))
				{
					//Add the new variant to the result list
					Call contextCall = getDirectory().getContext().get(ref.toString());
					if (contextCall.getStatus().isSuccess()
							&& (contextCall.getOutput() != null))
					{
						result.add(contextCall.getOutput());
					}
				}
			}

			if (result.size() == 0)
			{
				if (this.targetDirectory && getDirectory().isListingAllowed())
				{
					ReferenceList userList = new ReferenceList(this.directoryContent.size());

					// Compute the base reference (from a call's client point of view) 
					String baseRef = this.call.getBaseRef().toString(false, false);
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
		Call contextCall = new Call(Method.PUT, this.targetUri);
		// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			contextCall.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			contextCall.setInput(variant);
			if (targetDirectory)
			{
				contextCall.setResourceRef(this.targetUri);
				getDirectory().getContext().handle(contextCall);
			}
			else
			{
				//Try to get the unique representation of the resource, if any
				ReferenceList references = getVariantsReferences(true);
				if (!references.isEmpty())
				{
					contextCall.setResourceRef(references.get(0));
					getDirectory().getContext().handle(contextCall);
				}
				else
				{
					contextCall.setResourceRef(this.targetUri);
					getDirectory().getContext().handle(contextCall);
				}
			}
		}

		return new Result(contextCall.getStatus());
	}

	/**
	 * Asks the resource to delete itself and all its representations.
	 * @return The result information. 
	 */
	public Result delete()
	{
		Call contextCall = new Call();
		contextCall.setMethod(Method.DELETE);
		// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if (!getDirectory().isModifiable())
		{
			contextCall.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			if (targetDirectory)
			{
				contextCall.setResourceRef(this.targetUri);
				getDirectory().getContext().handle(contextCall);
			}
			else
			{
				//Try to get the unique representation of the resource
				ReferenceList references = getVariantsReferences(true);
				if (!references.isEmpty())
				{
					contextCall.setResourceRef(references.get(0));
					getDirectory().getContext().handle(contextCall);
				}
				else
				{
					contextCall.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			}
		}

		return new Result(contextCall.getStatus());
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
			Call contextCall = new Call(Method.GET, this.targetUri);
			contextCall.getClient().getAcceptedMediaTypes().add(
					new Preference<MediaType>(MediaType.TEXT_URI_LIST));
			getDirectory().getContext().handle(contextCall);
			if (contextCall.getOutput() != null)
			{
				ReferenceList listVariants = new ReferenceList(contextCall.getOutput());
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
			logger.log(Level.WARNING, "Unable to get resource variants", ioe);
		}

		return result;
	}
}
