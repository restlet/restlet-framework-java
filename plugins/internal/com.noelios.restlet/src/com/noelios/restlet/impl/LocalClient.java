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

package com.noelios.restlet.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.data.FileReference;
import com.noelios.restlet.data.FileRepresentation;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ContextReference.AuthorityType;
import com.noelios.restlet.impl.util.ByteUtils;

/**
 * Connector to the local resources accessible via file system, class loaders and similar mechanisms.
 * Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>addCommonExtensions</td>
 * 		<td>boolean</td>
 * 		<td>true</td>
 * 		<td>Sets a common list of associations from extensions to metadata.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>encodingExtension</td>
 * 		<td>String</td>
 * 		<td>null</td>
 * 		<td>Set a new extension for an encoding. The value is the extension name and the encoding name
 * separated by a space</td>
 * 	</tr>
 * 	<tr>
 * 		<td>languageExtension</td>
 * 		<td>String</td>
 * 		<td>null</td>
 * 		<td>Set a new extension for a language. The value is the extension name and the language name
 * separated by a space</td>
 * 	</tr>
 * 	<tr>
 * 		<td>mediaTypeExtension</td>
 * 		<td>String</td>
 * 		<td>null</td>
 * 		<td>Set a new extension for a media type. The value is the extension name and the media type name
 * separated by a space</td>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultEncoding</td>
 * 		<td>String</td>
 * 		<td>identity</td>
 * 		<td>Default encoding used when no encoding extension is available.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultMediaType</td>
 * 		<td>String</td>
 * 		<td>text/plain</td>
 * 		<td>Default media type used when no media type extension is available.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultLanguage</td>
 * 		<td>String</td>
 * 		<td>en-us</td>
 * 		<td>Default language used when no language extension is available.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>timeToLive</td>
 * 		<td>int</td>
 * 		<td>600</td>
 * 		<td>Time to live for a file representation before it expires (in seconds).</td>
 * 	</tr>
 * 	<tr>
 * 		<td>webAppPath</td>
 * 		<td>String</td>
 * 		<td>${user.home}/restlet.war</td>
 * 		<td>Path to the Web Application WAR file or directory.</td>
 * 	</tr>
 *	</table>
 * @see com.noelios.restlet.data.ContextReference
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @author Thierry Boileau
 */
public class LocalClient extends Client
{
	/** Mappings from extensions to metadata. */
	private Map<String, Metadata> metadataMappings;

	/** The location of the Web Application archive file or directory path. */
	private String webAppPath;

	/** Indicates if the Web Application path corresponds to an archive file or a directory path. */
	private boolean webAppArchive;

	/** Cache of all the WAR file entries to improve directory listing time. */
	private List<String> warEntries;

	/**
	 * Constructor. Note that the common list of metadata associations based on extensions is added, see
	 * the addCommonExtensions() method.
	 * @param context The context.
	 */
	public LocalClient(Context context)
	{
		super(context);

		getProtocols().add(Protocol.CONTEXT);
		getProtocols().add(Protocol.FILE);

		this.metadataMappings = new TreeMap<String, Metadata>();
		this.webAppPath = null;
		this.webAppArchive = false;
		this.warEntries = null;
	}

	/** Starts the Restlet. */
	public void start() throws Exception
	{
		// Optionnaly add the common extensions
		if (isAddCommonExtensions()) addCommonExtensions();

		// Set encoding extensions
		String[] tokens;
		for (Parameter param : getContext().getParameters().subList("encodingExtension"))
		{
			tokens = param.getValue().split(" ");

			if ((tokens != null) && (tokens.length == 2))
			{
				addExtension(tokens[0], new Encoding(tokens[1]));
			}
			else
			{
				getContext().getLogger().warning(
						"Unable to parse the following parameter: encodingExtension = "
								+ param.getValue());
			}
		}

		// Set language extensions
		for (Parameter param : getContext().getParameters().subList("languageExtension"))
		{
			tokens = param.getValue().split(" ");

			if ((tokens != null) && (tokens.length == 2))
			{
				addExtension(tokens[0], new Language(tokens[1]));
			}
			else
			{
				getContext().getLogger().warning(
						"Unable to parse the following parameter: languageExtension = "
								+ param.getValue());
			}
		}

		// Set media type extensions
		for (Parameter param : getContext().getParameters().subList("mediaTypeExtension"))
		{
			tokens = param.getValue().split(" ");

			if ((tokens != null) && (tokens.length == 2))
			{
				addExtension(tokens[0], new MediaType(tokens[1]));
			}
			else
			{
				getContext().getLogger().warning(
						"Unable to parse the following parameter: mediaTypeExtension = "
								+ param.getValue());
			}
		}

		super.start();
	}

	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		try
		{
			if (!isStarted()) start();
		}
		catch (Exception e)
		{
			getLogger().log(Level.SEVERE, "Couldn't start the local client connector", e);
		}

		if (isStarted())
		{
			String scheme = request.getResourceRef().getScheme();

			// Ensure that all ".." and "." are normalized into the path
			// to preven unauthorized access to user directories.
			request.getResourceRef().normalize();

			if (scheme.equalsIgnoreCase("file"))
			{
				handleFile(request, response, request.getResourceRef().getPath());
			}
			else if (scheme.equalsIgnoreCase("context"))
			{
				ContextReference cr = new ContextReference(request.getResourceRef());

				if (cr.getAuthorityType() == AuthorityType.CLASS)
				{
					handleClassLoader(request, response, getClass().getClassLoader());
				}
				else if (cr.getAuthorityType() == AuthorityType.SYSTEM)
				{
					handleClassLoader(request, response, ClassLoader.getSystemClassLoader());
				}
				else if (cr.getAuthorityType() == AuthorityType.THREAD)
				{
					handleClassLoader(request, response, Thread.currentThread()
							.getContextClassLoader());
				}
				else if (cr.getAuthorityType() == AuthorityType.WEB_APPLICATION)
				{
					handleWebApp(request, response);
				}
			}
			else
			{
				throw new IllegalArgumentException(
						"Protocol \""
								+ scheme
								+ "\" not supported by the connector. Only FILE and CONTEXT are supported.");
			}
		}
	}

	/**
	 * Handles a call for the FILE protocol.
	 * @param request The request to handle.
	 * @param response The response to update.
	 * @param path The file or directory path.
	 */
	protected void handleFile(Request request, Response response, String path)
	{
		File file = new File(FileReference.localizePath(path));

		if (request.getMethod().equals(Method.GET)
				|| request.getMethod().equals(Method.HEAD))
		{
			Representation output = null;

			// TBoi : Get variants for a resource
			boolean found = false;
			Iterator<Preference<MediaType>> iterator = request.getClientInfo()
					.getAcceptedMediaTypes().iterator();
			while (iterator.hasNext() && !found)
			{
				Preference<MediaType> pref = iterator.next();
				found = pref.getMetadata().equals(MediaType.TEXT_URI_LIST);
			}
			if (found)
			{
				//1- set up base name as the longest part of the name without known extensions (beginning from the left)
				String[] result = file.getName().split("\\.");
				String baseName = result[0];
				boolean extensionFound = false;
				for (int i = 1; i < result.length && !extensionFound; i++)
				{
					extensionFound = getMetadata(result[i]) != null;
					if (!extensionFound)
					{
						baseName += "." + result[i];
					}
				}
				//2- loooking for resources with the same base name
				File[] files = file.getParentFile().listFiles();
				ReferenceList rl = new ReferenceList(files.length);
				rl.setListRef(request.getResourceRef());

				for (File entry : files)
				{
					try
					{
						if (entry.getName().startsWith(baseName))
						{
							rl.add(new FileReference(entry));
						}
					}
					catch (IOException ioe)
					{
						getLogger().log(Level.WARNING, "Unable to create file reference", ioe);
					}
				}
				output = rl.getRepresentation();
			}
			else
			{
				if ((file != null) && file.exists())
				{
					if (file.isDirectory())
					{
						// Return the directory listing
						File[] files = file.listFiles();
						ReferenceList rl = new ReferenceList(files.length);
						rl.setListRef(request.getResourceRef());

						for (File entry : files)
						{
							try
							{
								rl.add(new FileReference(entry));
							}
							catch (IOException ioe)
							{
								getLogger().log(Level.WARNING, "Unable to create file reference",
										ioe);
							}
						}

						output = rl.getRepresentation();
					}
					else
					{
						// Return the file content
						output = new FileRepresentation(file, getDefaultMediaType(),
								getTimeToLive());
						updateMetadata(file.getName(), output);
					}
				}
			}

			if (output == null)
			{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
			else
			{
				response.setEntity(output);
				response.setStatus(Status.SUCCESS_OK);
			}
		}
		else if (request.getMethod().equals(Method.PUT))
		{
			File tmp = null;

			if (file.exists())
			{
				if (file.isDirectory())
				{
					response.setStatus(new Status(Status.CLIENT_ERROR_FORBIDDEN,
							"Can't put a new representation of a directory"));
				}
				else
				{
					// Replace the content of the file
					// First, create a temporary file
					try
					{
						tmp = File.createTempFile("restlet-upload", "bin");

						if (request.isEntityAvailable())
						{
							FileOutputStream fos = new FileOutputStream(tmp);
							ByteUtils.write(request.getEntity().getStream(), fos);
							fos.close();
						}
					}
					catch (IOException ioe)
					{
						getLogger().log(Level.WARNING, "Unable to create the temporary file",
								ioe);
						response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
								"Unable to create a temporary file"));
					}

					// Then delete the existing file
					if (file.delete())
					{
						// Finally move the temporary file to the existing file location
						if (tmp.renameTo(file))
						{
							if (request.getEntity() == null)
							{
								response.setStatus(Status.SUCCESS_NO_CONTENT);
							}
							else
							{
								response.setStatus(Status.SUCCESS_OK);
							}
						}
						else
						{
							getLogger()
									.log(Level.WARNING,
											"Unable to move the temporary file to replace the existing file");
							response
									.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
											"Unable to move the temporary file to replace the existing file"));
						}
					}
					else
					{
						getLogger().log(Level.WARNING, "Unable to delete the existing file");
						response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
								"Unable to delete the existing file"));
					}
				}
			}
			else
			{
				// No existing file or directory found
				if (path.endsWith("/"))
				{
					// Create a new directory and its necessary parents
					if (file.mkdirs())
					{
						response.setStatus(Status.SUCCESS_NO_CONTENT);
					}
					else
					{
						getLogger().log(Level.WARNING, "Unable to create the new directory");
						response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
								"Unable to create the new directory"));
					}
				}
				else
				{
					File parent = file.getParentFile();
					if ((parent != null) && parent.isDirectory())
					{
						if (!parent.exists())
						{
							// Create the parent directories then the new file
							if (!parent.mkdirs())
							{
								getLogger().log(Level.WARNING,
										"Unable to create the parent directory");
								response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
										"Unable to create the parent directory"));
							}
						}
					}

					// Create the new file
					try
					{
						if (file.createNewFile())
						{
							if (request.getEntity() == null)
							{
								response.setStatus(Status.SUCCESS_NO_CONTENT);
							}
							else
							{
								FileOutputStream fos = new FileOutputStream(file);
								ByteUtils.write(request.getEntity().getStream(), fos);
								fos.close();
								response.setStatus(Status.SUCCESS_OK);
							}
						}
						else
						{
							getLogger().log(Level.WARNING, "Unable to create the new file");
							response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
									"Unable to create the new file"));
						}
					}
					catch (FileNotFoundException fnfe)
					{
						getLogger().log(Level.WARNING, "Unable to create the new file", fnfe);
						response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
								"Unable to create the new file"));
					}
					catch (IOException ioe)
					{
						getLogger().log(Level.WARNING, "Unable to create the new file", ioe);
						response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
								"Unable to create the new file"));
					}
				}
			}
		}
		else if (request.getMethod().equals(Method.DELETE))
		{
			if (file.delete())
			{
				response.setStatus(Status.SUCCESS_NO_CONTENT);
			}
			else
			{
				if (file.isDirectory())
				{
					if (file.listFiles().length == 0)
					{
						response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
								"Couldn't delete the empty directory"));
					}
					else
					{
						response.setStatus(new Status(Status.CLIENT_ERROR_FORBIDDEN,
								"Couldn't delete the non-empty directory"));
					}
				}
				else
				{
					response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
							"Couldn't delete the file"));
				}
			}
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Handles a call with a given class loader.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleClassLoader(Request request, Response response,
			ClassLoader classLoader)
	{
		if (request.getMethod().equals(Method.GET)
				|| request.getMethod().equals(Method.HEAD))
		{
			URL url = classLoader.getResource(request.getResourceRef().getPath());

			if (url != null)
			{
				try
				{
					response.setEntity(new InputRepresentation(url.openStream(), null));
					response.setStatus(Status.SUCCESS_OK);
				}
				catch (IOException ioe)
				{
					getLogger().log(Level.WARNING,
							"Unable to open the representation's input stream", ioe);
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Handles a call using the current Web Application.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleWebApp(Request request, Response response)
	{
		if (this.webAppArchive)
		{
			try
			{
				String path = request.getResourceRef().getPath();
				JarFile war = new JarFile(getWebAppPath());
				JarEntry entry = war.getJarEntry(path);

				if (entry.isDirectory())
				{
					if (warEntries == null)
					{
						// Cache of all the WAR file entries to improve directory listing time.
						warEntries = new ArrayList<String>();
						for (Enumeration<JarEntry> entries = war.entries(); entries
								.hasMoreElements();)
						{
							warEntries.add(entries.nextElement().getName());
						}
					}

					// Return the directory listing
					ReferenceList rl = new ReferenceList();
					rl.setListRef(request.getResourceRef());

					for (String warEntry : warEntries)
					{
						if (warEntry.startsWith(path))
						{
							rl.add(new Reference(warEntry));
						}
					}

					response.setEntity(rl.getRepresentation());
					response.setStatus(Status.SUCCESS_OK);
				}
				else
				{
					// Return the file content
					Representation output = new InputRepresentation(war.getInputStream(entry),
							null);
					updateMetadata(path, output);
					response.setEntity(output);
					response.setStatus(Status.SUCCESS_OK);
				}
			}
			catch (IOException e)
			{
				getLogger().log(Level.WARNING, "Unable to access to the WAR file", e);
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}

		}
		else
		{
			String path = request.getResourceRef().getPath();

			if (path.toUpperCase().startsWith("/WEB-INF/"))
			{
				getLogger().warning(
						"Forbidden access to the WEB-INF directory detected. Path requested: "
								+ path);
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
			else if (path.toUpperCase().startsWith("/META-INF/"))
			{
				getLogger().warning(
						"Forbidden access to the META-INF directory detected. Path requested: "
								+ path);
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
			else
			{
				path = getWebAppPath() + path;
				handleFile(request, response, path);
			}
		}
	}

	/**
	 * Maps an extension to some metadata (media type, language or character set) to an extension.
	 * @param extension The extension name.
	 * @param metadata The metadata to map.
	 */
	public void addExtension(String extension, Metadata metadata)
	{
		this.metadataMappings.put(extension, metadata);
	}

	/**
	 * Adds a common list of associations from extensions to metadata.
	 * The list of languages extensions:<br/>
	 * <ul>
	 *  <li>en: English</li>
	 *  <li>es: Spanish</li>
	 *  <li>fr: French</li>
	 * </ul><br/>
	 * The list of media type extensions:<br/>
	 * <ul>
	 *  <li>css: CSS stylesheet</li>
	 *  <li>doc: Microsoft Word document</li>
	 *  <li>gif: GIF image</li>
	 *  <li>html: HTML document</li>
	 *  <li>ico: Windows icon (Favicon)</li>
	 *  <li>jpeg, jpg: JPEG image</li>
	 *  <li>js: Javascript document</li>
	 *  <li>pdf: Adobe PDF document</li>
	 *  <li>png: PNG image</li>
	 *  <li>ppt: Microsoft Powerpoint document</li>
	 *  <li>rdf:  Description Framework document</li>
	 *  <li>txt: Plain text</li>
	 *  <li>swf: Shockwave Flash object</li>
	 *  <li>xhtml: XHTML document</li>
	 *  <li>xml: XML document</li>
	 *  <li>zip: Zip archive</li>
	 * </ul>
	 */
	public void addCommonExtensions()
	{
		addExtension("en", Language.ENGLISH);
		addExtension("es", Language.SPANISH);
		addExtension("fr", Language.FRENCH);

		addExtension("css", MediaType.TEXT_CSS);
		addExtension("doc", MediaType.APPLICATION_WORD);
		addExtension("gif", MediaType.IMAGE_GIF);
		addExtension("html", MediaType.TEXT_HTML);
		addExtension("ico", MediaType.IMAGE_ICON);
		addExtension("jpeg", MediaType.IMAGE_JPEG);
		addExtension("jpg", MediaType.IMAGE_JPEG);
		addExtension("js", MediaType.APPLICATION_JAVASCRIPT);
		addExtension("pdf", MediaType.APPLICATION_PDF);
		addExtension("png", MediaType.IMAGE_PNG);
		addExtension("ppt", MediaType.APPLICATION_POWERPOINT);
		addExtension("rdf", MediaType.APPLICATION_RDF);
		addExtension("txt", MediaType.TEXT_PLAIN);
		addExtension("svg", MediaType.IMAGE_SVG);
		addExtension("swf", MediaType.APPLICATION_FLASH);
		addExtension("xhtml", MediaType.APPLICATION_XHTML_XML);
		addExtension("xml", MediaType.TEXT_XML);
		addExtension("zip", MediaType.APPLICATION_ZIP);
	}

	/**
	 * Returns the metadata mapped to an extension.
	 * @param extension The extension name.
	 * @return The mapped metadata.
	 */
	public Metadata getMetadata(String extension)
	{
		return this.metadataMappings.get(extension);
	}

	/**
	 * Updates some representation metadata based on a given entry name with extensions. 
	 * @param entryName The entry name with extensions.
	 * @param representation The representation to update.
	 */
	public void updateMetadata(String entryName, Representation representation)
	{
		String[] tokens = entryName.split("\\.");
		Metadata current;

		// We found a potential variant
		for (int j = 1; j < tokens.length; j++)
		{
			current = getMetadata(tokens[j]);
			if (current != null)
			{
				// Metadata extension detected 
				if (current instanceof MediaType)
					representation.setMediaType((MediaType) current);
				if (current instanceof CharacterSet)
					representation.setCharacterSet((CharacterSet) current);
				if (current instanceof Encoding)
					representation.setEncoding((Encoding) current);
				if (current instanceof Language)
					representation.setLanguage((Language) current);
			}

			int dashIndex = tokens[j].indexOf('-');
			if ((representation != null) && (dashIndex != -1))
			{
				// We found a language extension with a region area specified
				// Try to find a language matching the primary part of the extension
				String primaryPart = tokens[j].substring(0, dashIndex);
				current = getMetadata(primaryPart);
				if (current instanceof Language)
					representation.setLanguage((Language) current);
			}
		}
	}

	/**
	 * Returns the default encoding.
	 * Used when no encoding extension is available.
	 * @return The default encoding.
	 */
	public Encoding getDefaultEncoding()
	{
		return new Encoding(getContext().getParameters().getFirstValue("defaultEncoding",
				"identity"));
	}

	/**
	 * Returns the default media type.
	 * Used when no media type extension is available.
	 * @return The default media type.
	 */
	public MediaType getDefaultMediaType()
	{
		return new MediaType(getContext().getParameters().getFirstValue("defaultMediaType",
				"text/plain"));
	}

	/**
	 * Returns the default language.
	 * Used when no language extension is available.
	 * @return The default language.
	 */
	public Language getDefaultLanguage()
	{
		return new Language(getContext().getParameters().getFirstValue("defaultLanguage",
				"en-us"));
	}

	/**
	 * Returns the time to live for a file representation before it expires (in seconds).
	 * @return The time to live for a file representation before it expires (in seconds).
	 */
	public int getTimeToLive()
	{
		return Integer.parseInt(getContext().getParameters().getFirstValue("timeToLive",
				"600"));
	}

	/**
	 * Indicates if a common list of associations from extensions to metadata should be set.
	 * @return True if a common list of associations from extensions to metadata should be set.
	 */
	public boolean isAddCommonExtensions()
	{
		return Boolean.parseBoolean(getContext().getParameters().getFirstValue(
				"addCommonExtensions", "true"));
	}

	/**
	 * Returns the Web Application archive file or directory path.
	 * @return The Web Application archive file or directory path.
	 */
	public String getWebAppPath()
	{
		if (this.webAppPath == null)
		{
			this.webAppPath = getContext().getParameters().getFirstValue("webAppPath",
					System.getProperty("user.home") + File.separator + "restlet.war");
			File file = new File(this.webAppPath);

			if (file.exists())
			{
				if (file.isDirectory())
				{
					this.webAppArchive = false;

					// Adjust the archive directory path if necessary
					if (webAppPath.endsWith("/"))
						this.webAppPath = this.webAppPath.substring(0,
								this.webAppPath.length() - 1);
				}
				else
				{
					this.webAppArchive = true;
				}
			}
			else
			{
				getLogger().warning(
						"Unable to find an existing directory or archive at: "
								+ this.webAppPath);
			}
		}

		return this.webAppPath;
	}

}
