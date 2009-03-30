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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.script.internal.RepresentableString;
import org.restlet.ext.script.internal.ScriptUtils;
import org.restlet.ext.script.internal.ScriptedTextResourceContainer;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.EmbeddedScript;
import com.threecrickets.scripturian.ScriptContextController;
import com.threecrickets.scripturian.ScriptSource;

/**
 * A Restlet resource which runs an {@link EmbeddedScript} and redirects its
 * standard output to a {@link Representation}, for both HTTP GET and POST
 * verbs.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getScriptSource()}. This source is
 * accessible from the script itself, via <code>script.source</code> (see
 * {@link EmbeddedScript}).
 * <p>
 * This resource supports two modes of output:
 * <ul>
 * <li>Caching mode: First, the entire script is run, with its output sent into
 * a buffer. This buffer is then cached, and <i>only then</i> sent to the
 * client. This is the default mode and recommended for most scripts. Scripts
 * can control the duration of their individual cache by changing the value of
 * <code>script.cacheDuration</code> (see {@link EmbeddedScript}). Because
 * output is not sent to the client until after the script finished its run, it
 * is possible for the script to determine output characteristics at any time by
 * changing the values of <code>container.mediaType</code>,
 * <code>container.characterSet</code>, and <code>container.language</code> (see
 * below).</li>
 * <li>Streaming mode: Output is sent to the client <i>while</i> the script
 * runs. This is recommended for scripts that need to output a very large amount
 * of string, which might take a long time, or that might otherwise encounter
 * slow-downs while running. In either case, you want the client to receive
 * ongoing output. The output of the script is not cached, and the value of
 * <code>script.cacheDuration</code> is reset to 0. To enter streaming mode,
 * call <code>container.stream()</code> (see below for details). Note that you
 * must determine output characteristics (<code>container.mediaType</code>,
 * <code>container.characterSet</code>, and <code>container.language</code>)
 * <i>before</i> entering streaming mode. Trying to change them while running in
 * streaming mode will raise an exception.
 * </ul>
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
 * <li><code>container.stream()</code>: If you are in caching mode, calling this
 * method will return true and cause the script to run again, where this next
 * run will be in streaming mode. Whatever output the script created in the
 * current run is discarded, and all further exceptions are ignored. For this
 * reason, it's probably best to call <code>container.stream()</code> as early
 * as possible in the script, and then to quit the script as soon as possible if
 * it returns true. For example, your script can start by testing whether it
 * will have a lot of output, and if so, set output characteristics, call
 * <code>container.stream()</code>, and quit. If you are already in streaming
 * mode, calling this method has no effect and returns false. Note that a good
 * way to quit the script is to throw an exception, because it will end the
 * script and otherwise be ignored.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>container.variant</code>: The {@link Variant} of this request.
 * Useful for interrogating the client's preferences.</li>
 * <li><code>container.request</code>: The {@link Request}. Useful for accessing
 * URL attributes, form parameters, etc.</li>
 * <li><code>container.response</code>: The {@link Response}. Useful for
 * explicitly setting response characteristics.</li>
 * <li><code>container.isStreaming</code>: This boolean is true when the writer
 * is in streaming mode (see above).</li>
 * <li><code>container.writer</code>: Allows the script direct access to the
 * {@link Writer}. This should rarely be necessary, because by default the
 * standard output for your scripting engine would be directed to it, and the
 * scripting platform's native method for printing should be preferred. However,
 * some scripting platforms may not provide adequate access or may otherwise be
 * broken. Additionally, it may be useful to access the writer during streaming
 * mode. For example, you can call {@link Writer#flush()} to make sure all
 * output is sent to the client.</li>
 * <li><code>container.errorWriter</code>: Same as above, for standard error.
 * (Nothing is currently done with the contents of this, but this may change in
 * future implementations.)</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>container.mediaType</code>: The {@link MediaType} that will be used
 * for the generated string. Defaults to what the client requested (in
 * <code>container.variant</code>). If not in streaming mode, your script can
 * change this to something else.</li>
 * <li><code>container.characterSet</code>: The {@link CharacterSet} that will
 * be used for the generated string. Defaults to what the client requested (in
 * <code>container.variant</code>), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it. If not in
 * streaming mode, your script can change this to something else.</li>
 * <li><code>container.language</code>: The {@link Language} that will be used
 * for the generated string. Defaults to null. If not in streaming mode, your
 * script can change this to something else.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link ScriptContextController} can be set to add
 * your own global variables to each embedded script. See
 * {@link #getScriptContextController()}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.allowCompilation:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowCompilation()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.cache:</code>
 * {@link ConcurrentMap}, defaults to a new instance of
 * {@link ConcurrentHashMap}. See {@link #getCache()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.containerVariableName:</code>
 * {@link String}, defaults to "container". See
 * {@link #getContainerVariableName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li><code>org.restlet.ext.script.ScriptedTextResource.defaultName:</code>
 * {@link String}, defaults to "index.page". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.defaultScriptEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultScriptEngineName()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.scriptContextController:</code>
 * {@link ScriptContextController}. See {@link #getScriptContextController()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.scriptEngineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getScriptEngineManager()}.</li>
 * <li>
 * <code>org.restlet.ext.script.ScriptedTextResource.scriptSource:</code>
 * {@link ScriptSource}. <b>Required.</b> See {@link #getScriptSource()}.</li>
 * </ul>
 * 
 * @author Tal Liron
 * 
 * @see EmbeddedScript
 * @see ScriptedResource
 */
public class ScriptedTextResource extends Resource {
    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * scripts.
     */
    private ScriptEngineManager scriptEngineManager;

    /**
     * The {@link ScriptSource} used to fetch scripts.
     */
    private ScriptSource<EmbeddedScript> scriptSource;

    /**
     * If the URL points to a directory rather than a file, and that directory
     * contains a file with this name, then it will be used.
     */
    private String defaultName;

    /**
     * The default script engine name to be used if the script doesn't specify
     * one.
     */
    private String defaultScriptEngineName;

    /**
     * The default character set to be used if the client does not specify it.
     */
    private CharacterSet defaultCharacterSet;

    /**
     * The default variable name for the container instance.
     */
    private String containerVariableName;

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     */
    private ScriptContextController scriptContextController;

    /**
     * Whether or not compilation is attempted for script engines that support
     * it.
     */
    private Boolean allowCompilation;

    /**
     * This is so we can see the source code for scripts by adding
     * <code>?source=true</code> to the URL.
     */
    private Boolean sourceViewable;

    /**
     * Constant.
     */
    private static final String SOURCE = "source";

    /**
     * Constant.
     */
    private static final String TRUE = "true";

    /**
     * Cache used for caching mode.
     */
    private ConcurrentMap<String, RepresentableString> cache;

    /**
     * Constructs the resource.
     * 
     * @param context
     *            The Restlet context
     * @param request
     *            The request
     * @param response
     *            The response
     */
    public ScriptedTextResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        setModifiable(true);
    }

    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        // Handle the same was as represent(variant)
        Response response = getResponse();
        response.setEntity(represent());
    }

    /**
     * Cache used for caching mode. Defaults to a new instance of
     * {@link ConcurrentHashMap}. It is stored in the application's
     * {@link Context} for persistence across requests and for sharing among
     * instances of {@link ScriptedTextResource}.
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.cache</code> in the
     * application's {@link Context}.
     * 
     * @return The cache
     */
    @SuppressWarnings("unchecked")
    public ConcurrentMap<String, RepresentableString> getCache() {
        if (this.cache == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.cache = (ConcurrentMap<String, RepresentableString>) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.cache");
            if (this.cache == null) {
                this.cache = new ConcurrentHashMap<String, RepresentableString>();
                attributes.put(
                        "org.restlet.ext.script.ScriptedTextResource.cache",
                        this.cache);
            }
        }

        return this.cache;
    }

    /**
     * The default variable name for the container instance. Defaults to
     * "container".
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.containerVariableName</code>
     * in the application's {@link Context}.
     * 
     * @return The container variable name
     */
    public String getContainerVariableName() {
        if (this.containerVariableName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.containerVariableName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.containerVariableName");
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
     * <code>org.restlet.ext.script.ScriptedTextResource.defaultCharacterSet</code>
     * in the application's {@link Context}.
     * 
     * @return The default character set
     */
    public CharacterSet getDefaultCharacterSet() {
        if (this.defaultCharacterSet == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.defaultCharacterSet = (CharacterSet) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.defaultCharacterSet");
            if (this.defaultCharacterSet == null) {
                this.defaultCharacterSet = CharacterSet.UTF_8;
            }
        }

        return this.defaultCharacterSet;
    }

    /**
     * If the URL points to a directory rather than a file, and that directory
     * contains a file with this name, then it will be used. This allows you to
     * use the directory structure to create nice URLs that do not contain
     * filenames. Defaults to "index.page".
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.defaultName</code> in
     * the application's {@link Context}.
     * 
     * @return The default name
     */
    public String getDefaultName() {
        if (this.defaultName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.defaultName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.defaultName");
            if (this.defaultName == null) {
                this.defaultName = "index.page";
            }
        }

        return this.defaultName;
    }

    /**
     * The default script engine name to be used if the script doesn't specify
     * one. Defaults to "js".
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.defaultScriptEngineName</code>
     * in the application's {@link Context}.
     * 
     * @return The default script engine name
     */
    public String getDefaultScriptEngineName() {
        if (this.defaultScriptEngineName == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.defaultScriptEngineName = (String) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.defaultScriptEngineName");
            if (this.defaultScriptEngineName == null) {
                this.defaultScriptEngineName = "js";
            }
        }

        return this.defaultScriptEngineName;
    }

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     * Useful for adding your own global variables to the script.
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.scriptContextController</code>
     * in the application's {@link Context}.
     * 
     * @return The script context controller or null if none used
     */
    public ScriptContextController getScriptContextController() {
        if (this.scriptContextController == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.scriptContextController = (ScriptContextController) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.scriptContextController");
        }

        return this.scriptContextController;
    }

    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * scripts. Uses a default instance, but can be set to something else.
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.scriptEngineManager</code>
     * in the application's {@link Context}.
     * 
     * @return The script engine manager
     */
    public ScriptEngineManager getScriptEngineManager() {
        if (this.scriptEngineManager == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.scriptEngineManager = (ScriptEngineManager) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.scriptEngineManager");
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
     * <code>org.restlet.ext.script.ScriptedTextResource.scriptSource</code> in
     * the application's {@link Context}.
     * 
     * @return The script source
     */
    @SuppressWarnings("unchecked")
    public ScriptSource<EmbeddedScript> getScriptSource() {
        if (this.scriptSource == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.scriptSource = (ScriptSource<EmbeddedScript>) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.scriptSource");
            if (this.scriptSource == null) {
                throw new RuntimeException(
                        "Attribute org.restlet.ext.script.ScriptedTextResource.scriptSource must be set in context to use ScriptResource");
            }
        }

        return this.scriptSource;
    }

    /**
     * Whether or not compilation is attempted for script engines that support
     * it. Defaults to true.
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.allowCompilation</code>
     * in the application's {@link Context}.
     * 
     * @return Whether to allow compilation
     */
    public boolean isAllowCompilation() {
        if (this.allowCompilation == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.allowCompilation = (Boolean) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.allowCompilation");
            if (this.allowCompilation == null) {
                this.allowCompilation = true;
            }
        }

        return this.allowCompilation;
    }

    /**
     * This is so we can see the source code for scripts by adding
     * <code>?source=true</code> to the URL. You probably wouldn't want this for
     * most applications. Defaults to false.
     * <p>
     * This setting can be configured by setting an attribute named
     * <code>org.restlet.ext.script.ScriptedTextResource.sourceViewable</code>
     * in the application's {@link Context}.
     * 
     * @return Whether to allow viewing of script source code
     */
    public boolean isSourceViewable() {
        if (this.sourceViewable == null) {
            ConcurrentMap<String, Object> attributes = getContext()
                    .getAttributes();
            this.sourceViewable = (Boolean) attributes
                    .get("org.restlet.ext.script.ScriptedTextResource.sourceViewable");
            if (this.sourceViewable == null) {
                this.sourceViewable = false;
            }
        }

        return this.sourceViewable;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Request request = getRequest();
        String name = ScriptUtils.getRelativePart(request, getDefaultName());

        try {
            if (isSourceViewable()
                    && TRUE.equals(request.getResourceRef().getQueryAsForm()
                            .getFirstValue(SOURCE))) {
                // Represent script source
                return new StringRepresentation(getScriptSource()
                        .getScriptDescriptor(name).getText());
            } else {
                // Run script and represent its output
                ScriptedTextResourceContainer container = new ScriptedTextResourceContainer(
                        this, variant, getCache());
                Representation representation = container.include(name);
                if (representation == null) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
                } else {
                    return representation;
                }
            }
        } catch (FileNotFoundException e) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
        } catch (IOException e) {
            throw new ResourceException(e);
        } catch (ScriptException e) {
            throw new ResourceException(e);
        }
    }
}
