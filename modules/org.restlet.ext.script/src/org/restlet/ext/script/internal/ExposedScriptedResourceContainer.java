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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.script.ScriptedResource;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.EmbeddedScript;
import com.threecrickets.scripturian.ScriptContextController;
import com.threecrickets.scripturian.ScriptSource;

/**
 * This is the type of the <code>script.container</code> variable exposed to the
 * script.
 * 
 * @author Tal Liron
 * @see ScriptedResource
 */
public class ExposedScriptedResourceContainer {
    /**
     * The instance of this resource.
     */
    private final ScriptedResource resource;

    /**
     * The variants of this resource.
     */
    private final Map<Method, Object> variants;

    /**
     * The {@link Variant} of this request.
     */
    private final Variant variant;

    /**
     * The {@link Representation} of an entity provided with this request.
     */
    private final Representation entity;

    /**
     * The {@link MediaType} that will be used if you return an arbitrary type
     * for <code>represent()</code>, <code>acceptRepresentation()</code> and
     * <code>storeRepresentation()</code>.
     */
    private MediaType mediaType;

    /**
     * The {@link CharacterSet} that will be used if you return an arbitrary
     * type for <code>represent()</code>, <code>acceptRepresentation()</code>
     * and <code>storeRepresentation()</code>.
     */
    private CharacterSet characterSet;

    /**
     * The {@link Language} that will be used if you return an arbitrary type
     * for <code>represent()</code>, <code>acceptRepresentation()</code> and
     * <code>storeRepresentation()</code>.
     */
    private Language language;

    /**
     * A cache of script engines used by {@link EmbeddedScript}.
     */
    private final ConcurrentMap<String, ScriptEngine> scriptEngines = new ConcurrentHashMap<String, ScriptEngine>();

    /**
     * Constructs a container with no variant or entity, plain text media type,
     * and {@link ScriptedResource#getDefaultCharacterSet()}.
     * 
     * @param resource
     *            The resource
     * @param variants
     *            The variants of the resource
     */
    public ExposedScriptedResourceContainer(ScriptedResource resource,
            Map<Method, Object> variants) {
        this.resource = resource;
        this.variants = variants;
        this.variant = null;
        this.entity = null;
        this.mediaType = MediaType.TEXT_PLAIN;
        this.characterSet = resource.getDefaultCharacterSet();
    }

    /**
     * Constructs a container with media type and character set according to the
     * entity representation, or
     * {@link ScriptedResource#getDefaultCharacterSet()} if none is provided.
     * 
     * @param resource
     *            The resource
     * @param variants
     *            The variants of the resource
     * @param entity
     *            The entity's representation
     * @param variant
     *            The request variant
     */
    public ExposedScriptedResourceContainer(ScriptedResource resource,
            Map<Method, Object> variants, Representation entity, Variant variant) {
        this.resource = resource;
        this.variants = variants;
        this.entity = entity;
        this.variant = variant;
        this.mediaType = this.variant.getMediaType();
        this.characterSet = this.variant.getCharacterSet();
        if (this.characterSet == null) {
            this.characterSet = resource.getDefaultCharacterSet();
        }
    }

    /**
     * Constructs a container with media type and character set according to the
     * variant, or {@link ScriptedResource#getDefaultCharacterSet()} if none is
     * provided.
     * 
     * @param resource
     *            The resource
     * @param variants
     *            The variants of the resource
     * @param variant
     *            The variant
     */
    public ExposedScriptedResourceContainer(ScriptedResource resource,
            Map<Method, Object> variants, Variant variant) {
        this.resource = resource;
        this.variants = variants;
        this.variant = variant;
        this.entity = null;
        this.mediaType = variant.getMediaType();
        this.characterSet = variant.getCharacterSet();
        if (this.characterSet == null) {
            this.characterSet = resource.getDefaultCharacterSet();
        }
    }

    /**
     * The {@link CharacterSet} that will be used if you return an arbitrary
     * type for <code>represent()</code>, <code>acceptRepresentation()</code>
     * and <code>storeRepresentation()</code>. Defaults to what the client
     * requested (in <code>container.variant</code>), or to the value of
     * {@link ScriptedResource#getDefaultCharacterSet()} if the client did not
     * specify it.
     * 
     * @return The character set
     * @see #setCharacterSet(CharacterSet)
     */
    public CharacterSet getCharacterSet() {
        return this.characterSet;
    }

    /**
     * The {@link Representation} of an entity provided with this request.
     * Available only in <code>acceptRepresentation()</code> and
     * <code>storeRepresentation()</code>. Note that
     * <code>container.variant</code> is identical to
     * <code>container.entity</code> when available.
     * 
     * @return The entity's representation or null if not available
     */
    public Representation getEntity() {
        return this.entity;
    }

    /**
     * The {@link Language} that will be used if you return an arbitrary type
     * for <code>represent()</code>, <code>acceptRepresentation()</code> and
     * <code>storeRepresentation()</code>. Defaults to null.
     * 
     * @return The language or null if not set
     * @see #setLanguage(Language)
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * The {@link MediaType} that will be used if you return an arbitrary type
     * for <code>represent()</code>, <code>acceptRepresentation()</code> and
     * <code>storeRepresentation()</code>. Defaults to what the client requested
     * (in <code>container.variant</code>).
     * 
     * @return The media type
     * @see #setMediaType(MediaType)
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * The instance of this resource. Acts as a "this" reference for the script.
     * For example, during a call to <code>initializeResource()</code>, this can
     * be used to change the characteristics of the resource. Otherwise, you can
     * use it to access the request and response.
     * 
     * @return The resource
     */
    public ScriptedResource getResource() {
        return this.resource;
    }

    /**
     * The {@link ScriptSource} used to fetch and cache scripts.
     * 
     * @return The script source
     */
    public ScriptSource<EmbeddedScript> getSource() {
        return this.resource.getScriptSource();
    }

    /**
     * The {@link Variant} of this request. Useful for interrogating the
     * client's preferences. This is available only in <code>represent()</code>,
     * <code>acceptRepresentation()</code> and
     * <code>storeRepresentation()</code>.
     * 
     * @return The variant or null if not available
     */
    public Variant getVariant() {
        return this.variant;
    }

    /**
     * A map of possible variants or media types supported by this resource. You
     * should initialize this during a call to <code>initializeResource()</code>
     * . Values for the map can be {@link MediaType} constants, explicit
     * {@link Variant} instances (in which case these variants will be returned
     * immediately for their media type without calling the entry point), or a
     * {@link List} containing both media types and variants. Use map key
     * {@link Method#ALL} to indicate support for all methods.
     * 
     * @return The variants
     */
    public Map<Method, Object> getVariants() {
        return this.variants;
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
                    .isAllowCompilation());
            scriptDescriptor.setScript(script);
        }

        script.run(this.resource.getWriter(), this.resource.getErrorWriter(),
                true, this.scriptEngines, this, this.resource
                        .getScriptContextController(), false);
    }

    /**
     * Invokes an entry point in the embedded script.
     * 
     * @param entryPointName
     *            Name of entry point
     * @return Result of invocation
     * @throws ResourceException
     * @see {@link EmbeddedScript#invoke(String, Object, ScriptContextController)}
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
                        .isAllowCompilation());
                scriptDescriptor.setScript(script);
                script.run(this.resource.getWriter(), this.resource
                        .getErrorWriter(), true, this.scriptEngines, this,
                        this.resource.getScriptContextController(), false);
            }

            return script.invoke(entryPointName, this, this.resource
                    .getScriptContextController());
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