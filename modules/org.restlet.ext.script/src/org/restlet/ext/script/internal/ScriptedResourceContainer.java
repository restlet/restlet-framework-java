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

package org.restlet.ext.script.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.script.ScriptedResource;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.EmbeddedScript;
import com.threecrickets.scripturian.ScriptSource;

/**
 * This is the type of the "container" variable exposed to the script. The name
 * is set according to {@link ScriptedResource#getContainerVariableName()}.
 * 
 * @author Tal Liron
 * @see ScriptedResource
 */
public class ScriptedResourceContainer {
    private final ScriptedResource resource;

    private final Variant variant;

    private final Representation entity;

    private MediaType mediaType;

    private CharacterSet characterSet;

    private Language language;

    private final Writer writer = new OutputStreamWriter(System.out);

    private final Writer errorWriter = new OutputStreamWriter(System.err);

    private final ScriptedResourceScriptContextController scriptContextController;

    private final ConcurrentMap<String, ScriptEngine> scriptEngines = new ConcurrentHashMap<String, ScriptEngine>();

    /**
     * Constructs a container with no variant or entity, plain text media type,
     * and {@link ScriptedResource#getDefaultCharacterSet()}.
     * 
     * @param resource
     *            The resource
     */
    public ScriptedResourceContainer(ScriptedResource resource) {
        this.resource = resource;
        this.variant = null;
        this.entity = null;
        this.mediaType = MediaType.TEXT_PLAIN;
        this.characterSet = resource.getDefaultCharacterSet();
        this.scriptContextController = new ScriptedResourceScriptContextController(
                resource, this);
    }

    /**
     * Constructs a container with media type and character set according to the
     * entity representation, or
     * {@link ScriptedResource#getDefaultCharacterSet()} if none is provided.
     * 
     * @param resource
     *            The resource
     * @param entity
     *            The entity's representation
     */
    public ScriptedResourceContainer(ScriptedResource resource,
            Representation entity) {
        this.resource = resource;
        this.variant = entity;
        this.entity = entity;
        this.mediaType = this.variant.getMediaType();
        this.characterSet = this.variant.getCharacterSet();
        if (this.characterSet == null) {
            this.characterSet = resource.getDefaultCharacterSet();
        }
        this.scriptContextController = new ScriptedResourceScriptContextController(
                resource, this);
    }

    /**
     * Constructs a container with media type and character set according to the
     * variant, or {@link ScriptedResource#getDefaultCharacterSet()} if none is
     * provided.
     * 
     * @param resource
     *            The resource
     * @param variant
     *            The variant
     */
    public ScriptedResourceContainer(ScriptedResource resource, Variant variant) {
        this.resource = resource;
        this.variant = variant;
        this.entity = null;
        this.mediaType = variant.getMediaType();
        this.characterSet = variant.getCharacterSet();
        if (this.characterSet == null) {
            this.characterSet = resource.getDefaultCharacterSet();
        }
        this.scriptContextController = new ScriptedResourceScriptContextController(
                resource, this);
    }

    /**
     * The {@link CharacterSet} that will be used if you return an arbitrary
     * type for represent(), acceptRepresentation() and storeRepresentation().
     * Defaults to what the client requested (in container.variant), or to the
     * value of {@link ScriptedResource#getDefaultCharacterSet()} if the client
     * did not specify it.
     * 
     * @return The character set
     * @see #setCharacterSet(CharacterSet)
     */
    public CharacterSet getCharacterSet() {
        return this.characterSet;
    }

    /**
     * The {@link Representation} of an entity provided with this request.
     * Available only in acceptRepresentation() and storeRepresentation(). Note
     * that container.variant is identical to container.entity when available.
     * 
     * @return The entity's representation or null if not available
     */
    public Representation getEntity() {
        return this.entity;
    }

    /**
     * Same as {@link #getWriter()}, for standard error.
     * 
     * @return The error writer
     */
    public Writer getErrorWriter() {
        return this.errorWriter;
    }

    /**
     * The {@link Language} that will be used if you return an arbitrary type
     * for represent(), acceptRepresentation() and storeRepresentation().
     * Defaults to null.
     * 
     * @return The language or null if not set
     * @see #setLanguage(Language)
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * The {@link MediaType} that will be used if you return an arbitrary type
     * for represent(), acceptRepresentation() and storeRepresentation().
     * Defaults to what the client requested (in container.variant).
     * 
     * @return The media type
     * @see #setMediaType(MediaType)
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * The instance of this resource. Acts as a "this" reference for the script.
     * For example, during a call to initializeResource(), this can be used to
     * change the characteristics of the resource. Otherwise, you can use it to
     * access the request and response.
     * 
     * @return The resource
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * This is the {@link ScriptEngineManager} used to create the script engine.
     * Scripts may use it to get information about what other engines are
     * available.
     * 
     * @return The script engine manager
     */
    public ScriptEngineManager getScriptEngineManager() {
        return this.resource.getScriptEngineManager();
    }

    /**
     * The {@link Variant} of this request. Useful for interrogating the
     * client's preferences. This is available only in represent(),
     * acceptRepresentation() and storeRepresentation().
     * 
     * @return The variant or null if not available
     */
    public Variant getVariant() {
        return this.variant;
    }

    /**
     * Allows the script direct access to the {@link Writer}. This should rarely
     * be necessary, because by default the standard output for your scripting
     * engine would be directed to it, and the scripting platform's native
     * method for printing should be preferred. However, some scripting
     * platforms may not provide adequate access or may otherwise be broken.
     * 
     * @return The writer
     */
    public Writer getWriter() {
        return this.writer;
    }

    /**
     * This powerful method allows scripts to execute other scripts in place,
     * and is useful for creating large, maintainable applications based on
     * scripts. Included scripts can act as a library or toolkit and can even be
     * shared among many applications. The included script does not have to be
     * in the same language or use the same engine as the calling script.
     * However, if they do use the same engine, then methods, functions,
     * modules, etc., could be shared. It is important to note that how this
     * works varies a lot per scripting platform. For example, in JRuby, every
     * script is run in its own scope, so that sharing would have to be done
     * explicitly in the global scope. See the included embedded Ruby script
     * example for a discussion of various ways to do this.
     * 
     * @param name
     *            The script name
     * @throws IOException
     * @throws ScriptException
     */
    public void include(String name) throws IOException, ScriptException {
        include(name, null);
    }

    /**
     * As {@link #include(String)}, except that the script is not embedded. As
     * such, you must explicitly specify the name of the scripting engine that
     * should evaluate it.
     * 
     * @param name
     *            The script name
     * @param scriptEngineName
     *            The script engine name (if null, behaves identically to
     *            {@link #include(String)}
     * @throws IOException
     * @throws ScriptException
     */
    public void include(String name, String scriptEngineName)
            throws IOException, ScriptException {
        ScriptSource.ScriptDescriptor<EmbeddedScript> scriptDescriptor = this.resource
                .getScriptSource().getScriptDescriptor(name);

        EmbeddedScript script = scriptDescriptor.getScript();
        if (script == null) {
            String text = scriptDescriptor.getText();
            if (scriptEngineName != null) {
                text = EmbeddedScript.DEFAULT_DELIMITER1_START
                        + scriptEngineName + " " + text
                        + EmbeddedScript.DEFAULT_DELIMITER1_END;
            }
            script = new EmbeddedScript(text, this.resource
                    .getScriptEngineManager(), this.resource
                    .getDefaultScriptEngineName(), this.resource
                    .isAllowCompilation(), this.resource.getScriptSource());
            scriptDescriptor.setScript(script);
        }

        script.run(this.writer, this.errorWriter, this.scriptEngines,
                this.scriptContextController, false);
    }

    /**
     * Invokes an entry point in the embedded script.
     * 
     * @param entryPointName
     *            Name of entry point
     * @return Result of invocation
     * @throws ResourceException
     * @see {@link EmbeddedScript#invoke(String, com.threecrickets.scripturian.ScriptContextController)}
     */
    public Object invoke(String entryPointName) throws ResourceException {
        String name = ScriptUtils.getRelativePart(this.resource.getRequest(),
                this.resource.getDefaultName());

        try {
            ScriptSource.ScriptDescriptor<EmbeddedScript> scriptDescriptor = this.resource
                    .getScriptSource().getScriptDescriptor(name);

            EmbeddedScript script = scriptDescriptor.getScript();
            if (script == null) {
                String text = scriptDescriptor.getText();
                script = new EmbeddedScript(text, this.resource
                        .getScriptEngineManager(), this.resource
                        .getDefaultScriptEngineName(), this.resource
                        .isAllowCompilation(), this.resource.getScriptSource());
                scriptDescriptor.setScript(script);
                script.run(this.writer, this.errorWriter, this.scriptEngines,
                        this.scriptContextController, false);
            }

            return script.invoke(entryPointName, this.scriptContextController);
        } catch (FileNotFoundException e) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
        } catch (IOException e) {
            throw new ResourceException(e);
        } catch (ScriptException e) {
            throw new ResourceException(e);
        } catch (NoSuchMethodException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * @param characterSet
     *            The character set
     * @see #getCharacterSet()
     */
    public void setCharacterSet(CharacterSet characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * @param language
     *            The language or null
     * @see #getLanguage()
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @param mediaType
     *            The media type
     * @see #getMediaType()
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}