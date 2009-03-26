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
import java.util.concurrent.ConcurrentMap;

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
 * <li><code>initializeResource()</code>: This function is called when the
 * resource is initialized. We will use it set general characteristics for the
 * resource.</li>
 * <li><code>represent()</code>: This function is called for the GET verb, which
 * is expected to behave as a logical "read" of the resource's state. The
 * expectation is that it return one representation, out of possibly many, of
 * the resource's state. Returned values can be of any explicit sub-class of
 * {@link Representation}. Other types will be automatically converted to string
 * representation using the client's requested media type and character set.
 * These, and the language of the representation (defaulting to null), can be
 * read and changed via container.mediaType, container.characterSet, and
 * container.language. Additionally, you can use container.variant to
 * interrogate the client's provided list of supported languages and encoding.</li>
 * <li><code>acceptRepresentation()</code>: This function is called for the POST
 * verb, which is expected to behave as a logical "update" of the resource's
 * state. The expectation is that container.entity represents an update to the
 * state, that will affect future calls to represent(). As such, it may be
 * possible to accept logically partial representations of the state. You may
 * optionally return a representation, in the same way as represent(). Because
 * many scripting languages functions return the last statement's value by
 * default, you must explicitly return a null if you do not want to return a
 * representation to the client.</li>
 * <li><code>storeRepresentation()</code>: This function is called for the PUT
 * verb, which is expected to behave as a logical "create" of the resource's
 * state. The expectation is that container.entity represents an entirely new
 * state, that will affect future calls to represent(). Unlike
 * acceptRepresentation(), it is expected that the representation be logically
 * complete. You may optionally return a representation, in the same way as
 * represent(). Because JavaScript functions return the last statement's value
 * by default, you must explicitly return a null if you do not want to return a
 * representation to the client.</li>
 * <li><code>removeRepresentations()</code>: This function is called for the
 * DELETE verb, which is expected to behave as a logical "delete" of the
 * resource's state. The expectation is that subsequent calls to represent()
 * will fail. As such, it doesn't make sense to return a representation, and any
 * returned value will ignored. Still, it's a good idea to return null to avoid
 * any passing of value.</li>
 * </ul>
 * <p>
 * Names of these entry point can be configured via attributes in the
 * application's {@link Context}. See
 * {@link #getInitializeResourceEntryPointName()},
 * {@link #getRepresentEntryPointName()},
 * {@link #getAcceptRepresentationEntryPointName()},
 * {@link #getStoreRepresentationEntryPointName()} and
 * {@link #getRemoveRepresentationsEntryPointName()}.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getScriptSource()}.
 * <p>
 * Note that the embedded script's output is sent to the system's standard
 * output. Most likely, you will not want to output anything from the script.
 * However, this redirection is provided as a convenience, which may be useful
 * for certain debugging situations.
 * <p>
 * A special container environment is created for scripts, with some useful
 * services. It is available to the script as a global variable named
 * "container". This name can be configured via the application's
 * {@link Context} (see {@link #getContainerVariableName()}), though if you want
 * the embedded script include tag to work, you must also set
 * {@link EmbeddedScript#containerVariableName} to be the same. For some other
 * global variables available to scripts, see {@link EmbeddedScript}.
 * <p>
 * Operations:
 * <ul>
 * <li><code>container.include(name)</code>: This powerful method allows scripts
 * to execute other scripts in place, and is useful for creating large,
 * maintainable applications based on scripts. Included scripts can act as a
 * library or toolkit and can even be shared among many applications. The
 * included script does not have to be in the same language or use the same
 * engine as the calling script. However, if they do use the same engine, then
 * methods, functions, modules, etc., could be shared. It is important to note
 * that how this works varies a lot per scripting platform. For example, in
 * JRuby, every script is run in its own scope, so that sharing would have to be
 * done explicitly in the global scope. See the included embedded Ruby script
 * example for a discussion of various ways to do this.</li>
 * <li><code>container.include(name, scriptEngineName)</code>: As the above,
 * except that the script is not embedded. As such, you must explicitly specify
 * the name of the scripting engine that should evaluate it.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>container.resource</code>: The instance of this resource. Acts as a
 * "this" reference for the script. For example, during a call to
 * initializeResource(), this can be used to change the characteristics of the
 * resource. Otherwise, you can use it to access the request and response.</li>
 * <li><code>container.variant</code>: The {@link Variant} of this request.
 * Useful for interrogating the client's preferences. This is available only in
 * represent(), acceptRepresentation() and storeRepresentation().</li>
 * <li><code>container.entity</code>: The {@link Representation} of an entity
 * provided with this request. Available only in acceptRepresentation() and
 * storeRepresentation(). Note that container.variant is identical to
 * container.entity when available.</li>
 * <li><code>container.writer</code>: Allows the script direct access to the
 * {@link Writer}. This should rarely be necessary, because by default the
 * standard output for your scripting engine would be directed to it, and the
 * scripting platform's native method for printing should be preferred. However,
 * some scripting platforms may not provide adequate access or may otherwise be
 * broken.</li>
 * <li><code>container.errorWriter</code>: Same as above, for standard error.</li>
 * <li><code>container.scriptEngineManager</code>: This is the
 * {@link ScriptEngineManager} used to create the script engine. Scripts may use
 * it to get information about what other engines are available.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>container.mediaType</code>: The {@link MediaType} that will be used
 * if you return an arbitrary type for represent(), acceptRepresentation() and
 * storeRepresentation(). Defaults to what the client requested (in
 * container.variant).</li>
 * <li><code>container.characterSet</code>: The {@link CharacterSet} that will
 * be used if you return an arbitrary type for represent(),
 * acceptRepresentation() and storeRepresentation(). Defaults to what the client
 * requested (in container.variant), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it.</li>
 * <li><code>container.language</code>: The {@link Language} that will be used
 * if you return an arbitrary type for represent(), acceptRepresentation() and
 * storeRepresentation(). Defaults to null.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link ScriptContextController} can be set to add
 * your own global variables to each embedded script. See
 * {@link #getScriptContextController()}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.acceptRepresentationEntryPointName:</code>
 * {@link String}, defaults to "acceptRepresentation". See
 * {@link #getAcceptRepresentationEntryPointName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.allowCompilation:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowCompilation()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.containerVariableName:</code>
 * {@link String}, defaults to "container". See
 * {@link #getContainerVariableName()}.</li>
 * <li><code>org.restlet.ext.script.ScriptedResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li><code>org.restlet.ext.script.ScriptedResource.defaultName:</code>
 * {@link String}, defaults to "default.script". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.defaultScriptEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultScriptEngineName()}.</li>
 * <li><code>org.restlet.ext.script.ScriptedResource.extension:</code>
 * {@link String} , defaults to "script". See {@link #getExtension()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.initializeResourceEntryPointName:</code>
 * {@link String}, defaults to "initializeResource". See
 * {@link #getInitializeResourceEntryPointName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.removeRepresentationsEntryPointName:</code>
 * {@link String}, defaults to "removeRepresentations". See
 * {@link #getRemoveRepresentationsEntryPointName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.representEntryPointName:</code>
 * {@link String}, defaults to "represent". See
 * {@link #getRepresentEntryPointName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.scriptContextController:</code>
 * {@link ScriptContextController}. See {@link #getScriptContextController()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.scriptEngineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getScriptEngineManager()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.scriptSource:</code>
 * {@link ScriptSource}. <b>Required.</b> See {@link #getScriptSource()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedResource.storeRepresentationEntryPointName:</code>
 * {@link String}, defaults to "storeRepresentation". See
 * {@link #getStoreRepresentationEntryPointName()}.</li>
 * </ul>
 * 
 * @author Tal Liron
 * @see EmbeddedScript
 * @see ScriptedTextResource
 */
public class ScriptedResource extends Resource {
    private ScriptEngineManager scriptEngineManager;

    private Boolean allowCompilation;

    private ScriptSource<EmbeddedScript> scriptSource;

    private String extension;

    private String defaultName;

    private String defaultScriptEngineName;

    private CharacterSet defaultCharacterSet;

    private String containerVariableName;

    private ScriptContextController scriptContextController;

    private String initializeResourceEntryPointName;

    private String representEntryPointName;

    private String acceptRepresentationEntryPointName;

    private String storeRepresentationEntryPointName;

    private String removeRepresentationsEntryPointName;

    private Boolean sourceViewable;

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
     * @see #getInitializeResourceEntryPointName()
     * @see org.restlet.resource.Resource#Resource(Context, Request, Response)
     */
    public ScriptedResource(Context context, Request request, Response response) {
        super(context, request, response);

        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this);
        try {
            container.invoke(getInitializeResourceEntryPointName());
        } catch (ResourceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delegates to the acceptRepresentation entry point in the script.
     * 
     * @param entity
     * @see #getAcceptRepresentationEntryPointName()
     * @see Resource#acceptRepresentation(Representation)
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this, entity);

        Object r = container.invoke(getAcceptRepresentationEntryPointName());
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
     * The name of the acceptRepresentation entry point in the script. Defaults
     * to "acceptRepresentation".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.acceptRepresentationEntryPointName"
     * in the application's {@link Context}.
     * 
     * @return The name of the "acceptRepresentation" entry point
     */
    public String getAcceptRepresentationEntryPointName() {
        if (this.acceptRepresentationEntryPointName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.acceptRepresentationEntryPointName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.acceptRepresentationEntryPointName");
            if (this.acceptRepresentationEntryPointName == null) {
                this.acceptRepresentationEntryPointName = "acceptRepresentation";
            }
        }

        return this.acceptRepresentationEntryPointName;
    }

    /**
     * The default variable name for the container instance. Defaults to
     * "container".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.containerVariableName" in the
     * application's {@link Context}.
     * 
     * @return The container variable name
     */
    public String getContainerVariableName() {
        if (this.containerVariableName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.containerVariableName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.containerVariableName");
            if (this.containerVariableName == null) {
                this.containerVariableName = "container";
            }
        }

        return this.containerVariableName;
    }

    /**
     * The default character set to be used if the client does not specify it.
     * Defaults to {@link CharacterSet#UTF_8}.
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.defaultCharacterSet" in the
     * application's {@link Context}.
     * 
     * @return The default character set
     */
    public CharacterSet getDefaultCharacterSet() {
        if (this.defaultCharacterSet == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.defaultCharacterSet = (CharacterSet) attributes
                    .get("org.restlet.ext.script.ScriptedResource.defaultCharacterSet");
            if (this.defaultCharacterSet == null) {
                this.defaultCharacterSet = CharacterSet.UTF_8;
            }
        }

        return this.defaultCharacterSet;
    }

    /**
     * If the URL points to a directory rather than a file, and that directory
     * contains a file with this name, then it will be used. This allows you to
     * use the directory structure to create nice URLs without relying on
     * filenames. Defaults to "default.script".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.defaultName" in the
     * application's {@link Context}.
     * 
     * @return The default name
     */
    public String getDefaultName() {
        if (this.defaultName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.defaultName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.defaultName");
            if (this.defaultName == null) {
                this.defaultName = "default.script";
            }
        }

        return this.defaultName;
    }

    /**
     * The default script engine name to be used if the script doesn't specify
     * one. Defaults to "js".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.defaultScriptEngineName" in the
     * application's {@link Context}.
     * 
     * @return The default script engine name
     */
    public String getDefaultScriptEngineName() {
        if (this.defaultScriptEngineName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.defaultScriptEngineName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.defaultScriptEngineName");
            if (this.defaultScriptEngineName == null) {
                this.defaultScriptEngineName = "js";
            }
        }

        return this.defaultScriptEngineName;
    }

    /**
     * Files with this extension can have the extension omitted from the URL,
     * allowing for nicer URLs. Defaults to "script".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.extension" in the application's
     * {@link Context}.
     * 
     * @return The extension
     */
    public String getExtension() {
        if (this.extension == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.extension = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.extension");
            if (this.extension == null) {
                this.extension = "script";
            }
        }

        return this.extension;
    }

    /**
     * The name of the initializeResource entry point in the script. Defaults to
     * "initialize".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.initializeResourceEntryPointName"
     * in the application's {@link Context}.
     * 
     * @return The name of the "initializeResource" entry point
     */
    public String getInitializeResourceEntryPointName() {
        if (this.initializeResourceEntryPointName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.initializeResourceEntryPointName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.initializeResourceEntryPointName");
            if (this.initializeResourceEntryPointName == null) {
                this.initializeResourceEntryPointName = "initializeResource";
            }
        }

        return this.initializeResourceEntryPointName;
    }

    /**
     * The name of the removeRepresentations entry point in the script. Defaults
     * to "removeRepresentations".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.removeRepresentationsEntryPointName"
     * in the application's {@link Context}.
     * 
     * @return The name of the "removeRepresentations" entry point
     */
    public String getRemoveRepresentationsEntryPointName() {
        if (this.removeRepresentationsEntryPointName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.removeRepresentationsEntryPointName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.removeRepresentationsEntryPointName");
            if (this.removeRepresentationsEntryPointName == null) {
                this.removeRepresentationsEntryPointName = "removeRepresentations";
            }
        }

        return this.removeRepresentationsEntryPointName;
    }

    /**
     * The name of the represent entry point in the script. Defaults to
     * "represent".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.representEntryPointName" in the
     * application's {@link Context}.
     * 
     * @return The name of the "represent" entry point
     */
    public String getRepresentEntryPointName() {
        if (this.representEntryPointName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.representEntryPointName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.representEntryPointName");
            if (this.representEntryPointName == null) {
                this.representEntryPointName = "represent";
            }
        }

        return this.representEntryPointName;
    }

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     * Useful for adding your own global variables to the script.
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.scriptContextController" in the
     * application's {@link Context}.
     * 
     * @return The script context controller
     */
    public ScriptContextController getScriptContextController() {
        if (this.scriptContextController == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.scriptContextController = (ScriptContextController) attributes
                    .get("org.restlet.ext.script.ScriptedResource.scriptContextController");
        }

        return this.scriptContextController;
    }

    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * scripts. Uses a default instance, but can be set to something else.
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.scriptEngineManager" in the
     * application's {@link Context}.
     * 
     * @return The script engine manager
     */
    public ScriptEngineManager getScriptEngineManager() {
        if (this.scriptEngineManager == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.scriptEngineManager = (ScriptEngineManager) attributes
                    .get("org.restlet.ext.script.ScriptedResource.scriptEngineManager");
            if (this.scriptEngineManager == null) {
                this.scriptEngineManager = new ScriptEngineManager();
            }
        }

        return this.scriptEngineManager;
    }

    /**
     * The {@link ScriptSource} used to fetch scripts. This must be set to a
     * valid value before this class is used!
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.scriptSource" in the
     * application's {@link Context}.
     * 
     * @return The script source
     */
    @SuppressWarnings("unchecked")
    public ScriptSource<EmbeddedScript> getScriptSource() {
        if (this.scriptSource == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.scriptSource = (ScriptSource<EmbeddedScript>) attributes
                    .get("org.restlet.ext.script.ScriptedResource.scriptSource");
            if (this.scriptSource == null) {
                throw new RuntimeException(
                        "Attribute org.restlet.ext.script.ScriptedResource.scriptSource must be set in context to use ScriptResource");
            }
        }

        return this.scriptSource;
    }

    /**
     * The name of the storeRepresentation entry point in the script. Defaults
     * to "storeRepresentation".
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.storeRepresentationEntryPointName"
     * in the application's {@link Context}.
     * 
     * @return The name of the "storeRepresentation" entry point
     */
    public String getStoreRepresentationEntryPointName() {
        if (this.storeRepresentationEntryPointName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.storeRepresentationEntryPointName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedResource.storeRepresentationEntryPointName");
            if (this.storeRepresentationEntryPointName == null) {
                this.storeRepresentationEntryPointName = "storeRepresentation";
            }
        }

        return this.storeRepresentationEntryPointName;
    }

    /**
     * Whether or not compilation is attempted for script engines that support
     * it. Defaults to true.
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.allowCompilation" in the
     * application's {@link Context}.
     * 
     * @return Whether to allow compilation
     */
    public boolean isAllowCompilation() {
        if (this.allowCompilation == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.allowCompilation = (Boolean) attributes
                    .get("org.restlet.ext.script.ScriptedResource.allowCompilation");
            if (this.allowCompilation == null) {
                this.allowCompilation = true;
            }
        }

        return this.allowCompilation;
    }

    /**
     * This is so we can see the source code for scripts by adding ?source=true
     * to the URL. You probably wouldn't want this for most applications.
     * Defaults to false.
     * <p>
     * This setting can be configured by setting an attribute named
     * "org.restlet.ext.script.ScriptedResource.sourceViewable" in the
     * application's {@link Context}.
     * 
     * @return Whether to allow viewing of script source code
     */
    public boolean isSourceViewable() {
        if (this.sourceViewable == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.sourceViewable = (Boolean) attributes
                    .get("org.restlet.ext.script.ScriptedResource.sourceViewable");
            if (this.sourceViewable == null) {
                this.sourceViewable = false;
            }
        }

        return this.sourceViewable;
    }

    /**
     * Delegates to the removeRepresentations entry point in the script.
     * 
     * @see #getRemoveRepresentationsEntryPointName()
     * @see Resource#removeRepresentations()
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this);

        container.invoke(getRemoveRepresentationsEntryPointName());
    }

    /**
     * Delegates to the represent entry point in the script.
     * 
     * @param variant
     * @return A representation of the resource's state
     * @see #getRepresentEntryPointName()
     * @see Resource#represent(Variant)
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this, variant);

        Request request = getRequest();
        if (isSourceViewable()
                && TRUE.equals(request.getResourceRef().getQueryAsForm()
                        .getFirstValue(SOURCE))) {
            // Represent script source
            String name = ScriptUtils
                    .getRelativePart(request, getDefaultName());
            try {
                return new StringRepresentation(getScriptSource()
                        .getScriptDescriptor(name).getText());
            } catch (IOException e) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
            }
        } else {
            Object r = container.invoke(getRepresentEntryPointName());
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
     * @see #getStoreRepresentationEntryPointName()
     * @see Resource#storeRepresentation(Representation)
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        ScriptedResourceContainer container = new ScriptedResourceContainer(
                this, entity);

        Object r = container.invoke(getStoreRepresentationEntryPointName());
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
