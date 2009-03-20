/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.script;

import java.io.IOException;
import java.io.Writer;

import javax.script.ScriptEngineManager;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.script.internal.ScriptUtils;
import org.restlet.ext.script.internal.ScriptedResourceContainer;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.EmbeddedScript;
import com.threecrickets.scripturian.ScriptContextController;
import com.threecrickets.scripturian.ScriptSource;

/**
 * A Restlet resource which delegates functionality to an {@link EmbeddedScript}
 * with well-defined entry points. The entry points must be global functions, or
 * closures, or whatever other technique the scripting engine uses to make entry
 * point available to Java. They entry points are:
 * <ul>
 * <li><b>initializeResource()</b>: This function is called when the resource is
 * initialized. We will use it set general characteristics for the resource.</li>
 * <li>represent(): This function is called for the GET verb, which is expected
 * to behave as a logical "read" of the resource's state. The expectation is
 * that it return one representation, out of possibly many, of the resource's
 * state. Returned values can be of any explicit sub-class of
 * {@link Representation}. Other types will be automatically converted to string
 * representation using the client's requested media type and character set.
 * These, and the language of the representation (defaulting to null), can be
 * read and changed via container.mediaType, container.characterSet, and
 * container.language. Additionally, you can use container.variant to
 * interrogate the client's provided list of supported languages and encoding.</li>
 * <li><b>acceptRepresentation()</b>: This function is called for the POST verb,
 * which is expected to behave as a logical "update" of the resource's state.
 * The expectation is that container.entity represents an update to the state,
 * that will affect future calls to represent(). As such, it may be possible to
 * accept logically partial representations of the state. You may optionally
 * return a representation, in the same way as represent(). Because many
 * scripting languages functions return the last statement's value by default,
 * you must explicitly return a null if you do not want to return a
 * representation to the client.</li>
 * <li><b>storeRepresentation()</b>: This function is called for the PUT verb,
 * which is expected to behave as a logical "create" of the resource's state.
 * The expectation is that container.entity represents an entirely new state,
 * that will affect future calls to represent(). Unlike acceptRepresentation(),
 * it is expected that the representation be logically complete. You may
 * optionally return a representation, in the same way as represent(). Because
 * JavaScript functions return the last statement's value by default, you must
 * explicitly return a null if you do not want to return a representation to the
 * client.</li>
 * <li><b>removeRepresentations()</b>: This function is called for the DELETE
 * verb, which is expected to behave as a logical "delete" of the resource's
 * state. The expectation is that subsequent calls to represent() will fail. As
 * such, it doesn't make sense to return a representation, and any returned
 * value will ignored. Still, it's a good idea to return null to avoid any
 * passing of value.</li>
 * </ul>
 * <p>
 * Names of these entry point can be changed via
 * {@link #initializeResourceEntryPointName}, {@link #representEntryPointName},
 * {@link #acceptRepresentationEntryPointName},
 * {@link #storeRepresentationEntryPointName} and
 * {@link #removeRepresentationsEntryPointName}.
 * <p>
 * Before using this resource, make sure to set {@link ScriptSource} to a valid
 * source.
 * <p>
 * Note that the embedded script's output is sent to the system's standard
 * output. Most likely, you will not want to output anything from the script.
 * However, this redirection is provided as a convenience, which may be useful
 * for certain debugging situations.
 * <p>
 * A special container environment is created for scripts, with some useful
 * services. It is available to the script as a global variable named
 * "container". This name can be changed via {@link #containerVariableName},
 * though if you want the embedded script include tag to work, you must also set
 * {@link EmbeddedScript#containerVariableName} to be the same. For some other
 * global variables available to scripts, see {@link EmbeddedScript}.
 * <p>
 * Operations:
 * <ul>
 * <li><b>container.include(name)</b>: This powerful method allows scripts to
 * execute other scripts in place, and is useful for creating large,
 * maintainable applications based on scripts. Included scripts can act as a
 * library or toolkit and can even be shared among many applications. The
 * included script does not have to be in the same language or use the same
 * engine as the calling script. However, if they do use the same engine, then
 * methods, functions, modules, etc., could be shared. It is important to note
 * that how this works varies a lot per scripting platform. For example, in
 * JRuby, every script is run in its own scope, so that sharing would have to be
 * done explicitly in the global scope. See the included embedded Ruby script
 * example for a discussion of various ways to do this.</li>
 * <li><b>container.include(name, scriptEngineName)</b>: As the above, except
 * that the script is not embedded. As such, you must explicitly specify the
 * name of the scripting engine that should evaluate it.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><b>container.resource</b>: The instance of this resource. Acts as a
 * "this" reference for the script. For example, during a call to
 * initializeResource(), this can be used to change the characteristics of the
 * resource. Otherwise, you can use it to access the request and response.</li>
 * <li><b>container.variant</b>: The {@link Variant} of this request. Useful for
 * interrogating the client's preferences. This is available only in
 * represent(), acceptRepresentation() and storeRepresentation().</li>
 * <li><b>container.entity</b>: The {@link Representation} of an entity provided
 * with this request. Available only in acceptRepresentation() and
 * storeRepresentation(). Note that container.variant is identical to
 * container.entity when available.</li>
 * <li><b>container.writer</b>: Allows the script direct access to the
 * {@link Writer}. This should rarely be necessary, because by default the
 * standard output for your scripting engine would be directed to it, and the
 * scripting platform's native method for printing should be preferred. However,
 * some scripting platforms may not provide adequate access or may otherwise be
 * broken.</li>
 * <li><b>container.errorWriter</b>: Same as above, for standard error.</li>
 * <li><b>container.scriptEngineManager</b>: This is the
 * {@link ScriptEngineManager} used to create the script engine. Scripts may use
 * it to get information about what other engines are available.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><b>container.mediaType</b>: The {@link MediaType} that will be used if
 * you return an arbitrary type for represent(), acceptRepresentation() and
 * storeRepresentation(). Defaults to what the client requested (in
 * container.variant).</li>
 * <li><b>container.characterSet</b>: The {@link CharacterSet} that will be used
 * if you return an arbitrary type for represent(), acceptRepresentation() and
 * storeRepresentation(). Defaults to what the client requested (in
 * container.variant), or to the value of {@link #defaultCharacterSet} if the
 * client did not specify it.</li>
 * <li><b>container.language</b>: The {@link Language} that will be used if you
 * return an arbitrary type for represent(), acceptRepresentation() and
 * storeRepresentation(). Defaults to null.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link #scriptContextController} can be set to
 * add your own global variables to each embedded script.
 * 
 * @author Tal Liron
 * @see EmbeddedScript
 * @see ScriptedTextResource
 */
public class ScriptedResource extends Resource {
    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * scripts. Uses a default instance, but can be set to something else.
     */
    public static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    /**
     * Whether or not compilation is attempted for script engines that support
     * it. Defaults to true.
     */
    public static boolean allowCompilation = true;

    /**
     * The {@link ScriptSource} used to fetch scripts. This must be set to a
     * valid value before this class is used!
     */
    public static ScriptSource<EmbeddedScript> scriptSource;

    /**
     * Files with this extension can have the extension omitted from the URL,
     * allowing for nicer URLs. Defaults to "script".
     */
    public static String extension = "script";

    /**
     * If the URL points to a directory rather than a file, and that directory
     * contains a file with this name, then it will be used. This allows you to
     * use the directory structure to create nice URLs without relying on
     * filenames. Defaults to "default.script".
     */
    public static String defaultName = "default.script";

    /**
     * The default script engine name to be used if the script doesn't specify
     * one. Defaults to "js".
     */
    public static String defaultEngineName = "js";

    /**
     * The default character set to be used if the client does not specify it.
     * Defaults to {@link CharacterSet#UTF_8}.
     */
    public static CharacterSet defaultCharacterSet = CharacterSet.UTF_8;

    /**
     * The default variable name for the {@link ScriptedResourceContainer}
     * instance. Defaults to "container".
     */
    public static String containerVariableName = "container";

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     * Useful for adding your own global variables to the script.
     */
    public static ScriptContextController scriptContextController;

    /**
     * The name of the initializeResource entry point in the script. Defaults to
     * "initialize".
     */
    public static String initializeResourceEntryPointName = "initializeResource";

    /**
     * The name of the represent entry point in the script. Defaults to
     * "represent".
     */
    public static String representEntryPointName = "represent";

    /**
     * The name of the acceptRepresentation entry point in the script. Defaults
     * to "acceptRepresentation".
     */
    public static String acceptRepresentationEntryPointName = "acceptRepresentation";

    /**
     * The name of the storeRepresentation entry point in the script. Defaults
     * to "storeRepresentation".
     */
    public static String storeRepresentationEntryPointName = "storeRepresentation";

    /**
     * The name of the removeRepresentations entry point in the script. Defaults
     * to "removeRepresentations".
     */
    public static String removeRepresentationsEntryPointName = "removeRepresentations";

    /**
     * This is so we can see the source code for scripts by adding ?source=true
     * to the URL. You probably wouldn't want this for most applications.
     * Defaults to false.
     */
    public static boolean sourceViewable = false;

    private static final String SOURCE = "source";

    private static final String TRUE = "true";

    /**
     * Constructs the resource, and delegates to the initializeResource entry
     * point in the script.
     * 
     * @param context
     *            The Restlet context
     * @param request
     *            The request
     * @param response
     *            The response
     * @see #initializeResourceEntryPointName
     * @see org.restlet.resource.Resource#Resource(Context, Request, Response)
     */
    public ScriptedResource(Context context, Request request, Response response) {
        super(context, request, response);

        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this);
        try {
            container.invoke(initializeResourceEntryPointName);
        } catch (ResourceException x) {
            x.printStackTrace();
        }
    }

    /**
     * Delegates to the acceptRepresentation entry point in the script.
     * 
     * @param entity
     * @see #acceptRepresentationEntryPointName
     * @see org.restlet.resource.Resource#acceptRepresentation(org.restlet.resource.Representation)
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this, entity);

        Object r = container.invoke(acceptRepresentationEntryPointName);
        if (r != null) {
            if (r instanceof Representation) {
                getResponse().setEntity((Representation) r);
            } else {
                getResponse().setEntity(
                        new StringRepresentation(r.toString(), container
                                .getMediaType(), container.getLanguage(),
                                container.getCharacterSet()));
            }
        }
    }

    /**
     * Delegates to the removeRepresentations entry point in the script.
     * 
     * @see #removeRepresentationsEntryPointName
     * @see org.restlet.resource.Resource#removeRepresentations()
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this);

        container.invoke(removeRepresentationsEntryPointName);
    }

    /**
     * Delegates to the represent entry point in the script.
     * 
     * @param variant
     * @return A representation of the resource's state
     * @see #representEntryPointName
     * @see org.restlet.resource.Resource#represent(org.restlet.resource.Variant)
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this, variant);

        Request request = getRequest();
        if (sourceViewable
                && TRUE.equals(request.getResourceRef().getQueryAsForm()
                        .getFirstValue(SOURCE))) {
            // Represent script source
            String name = ScriptUtils.getRelativePart(request, defaultName);
            try {
                return new StringRepresentation(scriptSource
                        .getScriptDescriptor(name).getText());
            } catch (IOException x) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, x);
            }
        } else {
            Object r = container.invoke(representEntryPointName);
            if (r == null) {
                return null;
            }
            if (r instanceof Representation) {
                return (Representation) r;
            } else {
                return new StringRepresentation(r.toString(), container
                        .getMediaType(), container.getLanguage(), container
                        .getCharacterSet());
            }
        }
    }

    /**
     * Delegates to the storeRepresentation entry point in the script.
     * 
     * @param entity
     * @see #storeRepresentationEntryPointName
     * @see org.restlet.resource.Resource#storeRepresentation(org.restlet.resource.Representation)
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this, entity);

        Object r = container.invoke(storeRepresentationEntryPointName);
        if (r != null) {
            if (r instanceof Representation) {
                getResponse().setEntity((Representation) r);
            } else {
                getResponse().setEntity(
                        new StringRepresentation(r.toString(), container
                                .getMediaType(), container.getLanguage(),
                                container.getCharacterSet()));
            }
        }
    }
}
