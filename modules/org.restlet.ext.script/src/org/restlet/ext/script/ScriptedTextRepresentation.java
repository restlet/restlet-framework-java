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

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.script.internal.ExposedScriptedTextRepresentationContainer;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.threecrickets.scripturian.CompositeScript;
import com.threecrickets.scripturian.CompositeScriptContext;
import com.threecrickets.scripturian.ScriptContextController;

/**
 * A textual representation of a plain text stream with embedded scriptlets. The
 * scriptlets are run only when the representation is actually written.
 * <p>
 * Internally wraps a Scripturian {@link CompositeScript} instance.
 * <p>
 * A special container environment is created for scripts, with some useful
 * services. It is available to the script as a global variable named
 * <code>script.container</code>. The following read-only attributes are
 * available:
 * <ul>
 * <li><code>script.container.representation</code>: Access to the
 * representation itself. This can be useful for generating text according to
 * set characteristics. For example, calling
 * {@link Representation#getLanguages()} and generating the appropriate text.</li>
 * </ul>
 * <p>
 * Note that this container environment is very limited. The include and in-flow
 * tags of {@link CompositeScript} will not work here, nor is any caching of the
 * script done by default. For a more complete container environment for
 * scripted textual representations, see {@link ScriptedTextResource}.
 * <p>
 * You can optionally use
 * {@link #setScriptContextController(ScriptContextController)} to add your own
 * global variables to the script.
 * 
 * @author Tal Liron
 * @see CompositeScript
 * @see ScriptedTextResource
 */
public class ScriptedTextRepresentation extends WriterRepresentation {
    /**
     * The wrapped composite script instance.
     */
    private final CompositeScript compositeScript;

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
     * Construct an instance to wrap an existing composite script instance.
     * 
     * @param mediaType
     *            The media type
     * @param characterSet
     *            The character set
     * @param compositeScript
     *            The composite script instance
     */
    public ScriptedTextRepresentation(MediaType mediaType,
            CharacterSet characterSet, CompositeScript compositeScript) {
        super(mediaType);
        setCharacterSet(characterSet);
        this.compositeScript = compositeScript;
    }

    /**
     * Construct an instance based on a string, which involves both parsing and
     * optional compilation of the script. See {@link CompositeScript} for rules
     * on scriptlets.
     * <p>
     * Note that, depending on the script engines used, this can be slow and
     * resource-intensive. Also note that trivial cases, when we have no
     * embedded scriptlets, we will not use any script engine and thus the text
     * will be processed very effectively by this class.
     * <p>
     * After construction, you can access the internal composite script instance
     * via {@link #getCompositeScript()}.
     * 
     * @param mediaType
     *            The media type
     * @param text
     *            The composite script text
     * @param defaultScriptEngineName
     *            The default script engine name to be used if none is specified
     * @param allowCompilation
     *            Whether or not compilation is attempted for script engines
     *            that support it (this is usually undesirable, but you mat want
     *            this if you will be re-using the composite script accessed via
     *            {@link #getCompositeScript()})
     * @param scriptEngineManager
     *            The script engine manager
     * @throws ScriptException
     */
    public ScriptedTextRepresentation(MediaType mediaType, String text,
            String defaultScriptEngineName, boolean allowCompilation,
            ScriptEngineManager scriptEngineManager) throws ScriptException {
        super(mediaType);
        this.compositeScript = new CompositeScript(text, scriptEngineManager,
                defaultScriptEngineName, null, allowCompilation);
    }

    /**
     * Access the wrapped composite script instance.
     * 
     * @return The wrapped composite script instance
     */
    public CompositeScript getCompositeScript() {
        return this.compositeScript;
    }

    /**
     * The optional script context controller to be used when the
     * {@link CompositeScript} instance is run during {@link #write(Writer)}.
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
            this.compositeScript.run(false, writer, this.errorWriter, false,
                    new CompositeScriptContext(this.compositeScript
                            .getScriptEngineManager()),
                    new ExposedScriptedTextRepresentationContainer(this),
                    getScriptContextController());
        } catch (ScriptException e) {
            IOException ioe = new IOException("Script exception");
            ioe.initCause(e);
            throw ioe;
        }
    }
}