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
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.script.internal.ScriptedTextRepresentationContainer;
import org.restlet.ext.script.internal.ScriptedTextRepresentationScriptContextController;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.threecrickets.scripturian.EmbeddedScript;
import com.threecrickets.scripturian.ScriptContextController;

/**
 * A textual representation of a string with embedded scriptlets. The script is
 * run only when the representation is actually written.
 * <p>
 * Internally wraps a Scripturian {@link EmbeddedScript} instance.
 * <p>
 * A special container environment is created for scripts, with some useful
 * services. It is available to the script as a global variable named
 * "container" (or anything else returned by
 * {@link EmbeddedScript#getContainerVariableName()}). The following read-only
 * attributes are available:
 * <ul>
 * <li><code>container.representation</code>: Access to the representation
 * itself. This can be useful for generating text according to set
 * characteristics. For example, calling {@link Representation#getLanguages()}
 * and generating the appropriate text.</li>
 * </ul>
 * <p>
 * Note that this container environment is very limited. The include tag of
 * {@link EmbeddedScript} will not work here, nor is any caching of the script
 * done by default. For a more complete container environment for scripted
 * textual representations, see {@link ScriptedTextResource}.
 * <p>
 * You can optionally use
 * {@link #setScriptContextController(ScriptContextController)} to add your own
 * global variables to the script.
 * 
 * @author Tal Liron
 * @see EmbeddedScript
 * @see ScriptedTextResource
 */
public class ScriptedTextRepresentation extends WriterRepresentation {
    /**
     * The wrapped embedded script instance.
     */
    private final EmbeddedScript embeddedScript;

    /**
     * The error writer. Note that we currently do nothing with whatever the
     * script writes here. Future versions may provide access to this.
     */
    private final StringWriter errorWriter = new StringWriter();

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     */
    private ScriptContextController scriptContextController;

    /**
     * A cache of script engines used by {@link EmbeddedScript}.
     */
    private final ConcurrentMap<String, ScriptEngine> scriptEngines = new ConcurrentHashMap<String, ScriptEngine>();

    /**
     * Construct an instance to wrap an existing embedded script instance.
     * 
     * @param mediaType
     *            The media type
     * @param characterSet
     *            The character set
     * @param embeddedScript
     *            The embedded script instance
     */
    public ScriptedTextRepresentation(MediaType mediaType,
            CharacterSet characterSet, EmbeddedScript embeddedScript) {
        super(mediaType);
        setCharacterSet(characterSet);
        this.embeddedScript = embeddedScript;
    }

    /**
     * Construct an instance based on a string, which involves both parsing and
     * optional compilation of the script. See {@link EmbeddedScript} for rules
     * on embedding scripts.
     * <p>
     * Note that, depending on the script engines used, this can be slow and
     * resource-intensive. Also note that trivial cases, with no embedded script
     * segments, will not use any script engine and thus be processed very
     * effectively by this class.
     * <p>
     * After construction, you can access the internal embedded script instance
     * via {@link #getEmbeddedScript()}.
     * 
     * @param mediaType
     *            The media type
     * @param text
     *            The embedded script text
     * @param defaultScriptEngineName
     *            The default script engine name to be used if none is specified
     * @param allowCompilation
     *            Whether or not compilation is attempted for script engines
     *            that support it (this is usually undesirable, but you mat want
     *            this is you will be re-using the embedded script accessed via
     *            {@link #getEmbeddedScript()})
     * @param scriptEngineManager
     *            The script engine manager
     * @throws ScriptException
     */
    public ScriptedTextRepresentation(MediaType mediaType, String text,
            String defaultScriptEngineName, boolean allowCompilation,
            ScriptEngineManager scriptEngineManager) throws ScriptException {
        super(mediaType);
        this.embeddedScript = new EmbeddedScript(text, scriptEngineManager,
                defaultScriptEngineName, allowCompilation, null);
    }

    /**
     * Access the wrapped embedded script instance.
     * 
     * @return The wrapped embedded script instance
     */
    public EmbeddedScript getEmbeddedScript() {
        return this.embeddedScript;
    }

    /**
     * The optional script context controller to be used when the
     * {@link EmbeddedScript} instance is run during {@link #write(Writer)}.
     * 
     * @return The script context controller or null if none used
     */
    public ScriptContextController getScriptContextController() {
        return this.scriptContextController;
    }

    /**
     * @param scriptContextController
     *            The script context controller or null if none used
     * @see #getScriptContextController()
     */
    public void setScriptContextController(
            ScriptContextController scriptContextController) {
        this.scriptContextController = scriptContextController;
    }

    @Override
    public void write(Writer writer) throws IOException {
        try {
            ScriptedTextRepresentationContainer container = new ScriptedTextRepresentationContainer(
                    this);
            this.embeddedScript.run(writer, this.errorWriter, false,
                    this.scriptEngines,
                    new ScriptedTextRepresentationScriptContextController(this,
                            container, this.embeddedScript
                                    .getContainerVariableName()), false);
        } catch (ScriptException e) {
            IOException ioe = new IOException("Script exception");
            ioe.initCause(e);
            throw ioe;
        }
    }
}