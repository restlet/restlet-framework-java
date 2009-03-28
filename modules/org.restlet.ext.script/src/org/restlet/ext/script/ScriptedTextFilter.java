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

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import com.threecrickets.scripturian.ScriptContextController;

/**
 * Filter response's entity to allow for embedded scriptlets. Internally wraps
 * the entity in a {@link ScriptedTextRepresentation}. As part of the process,
 * the entity is translated into text via {@link Representation#getText()}.
 * <p>
 * Note that scripts are not cached; they are parsed and run anew for every
 * entity that passes through the filter. If you are passing the same entities
 * in a predictable manner, then using this filter will be very inefficient. For
 * a more complete container environment for scripted textual representations,
 * which allows for caching, see {@link ScriptedTextResource}.
 * <p>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Tal Liron
 * @see ScriptedTextRepresentation
 */
public class ScriptedTextFilter extends Filter {
    /**
     * The default script engine name to be used if the script doesn't specify
     * one.
     */
    private String defaultScriptEngineName;

    /**
     * Whether or not compilation is attempted for script engines that support
     * it.
     */
    private boolean allowCompilation;

    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * scripts.
     */
    private ScriptEngineManager scriptEngineManager;

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     */
    private ScriptContextController scriptContextController;

    /**
     * Constructor. Sets a default instance of a {@link ScriptEngineManager},
     * default script engine name of "js", and no compilation.
     * 
     */
    public ScriptedTextFilter() {
        super();
        this.scriptEngineManager = new ScriptEngineManager();
        this.defaultScriptEngineName = "js";
        this.allowCompilation = false;
    }

    /**
     * Constructor. Sets a default instance of a {@link ScriptEngineManager},
     * default script engine name of "js", and no compilation.
     * 
     * @param context
     *            The context
     */
    public ScriptedTextFilter(Context context) {
        super(context);
        this.scriptEngineManager = new ScriptEngineManager();
        this.defaultScriptEngineName = "js";
        this.allowCompilation = false;
    }

    /**
     * Constructor. Sets a default instance of a {@link ScriptEngineManager},
     * default script engine name of "js", and no compilation.
     * 
     * @param context
     *            The context
     * @param next
     *            The next Restlet
     */
    public ScriptedTextFilter(Context context, Restlet next) {
        super(context, next);
        this.scriptEngineManager = new ScriptEngineManager();
        this.defaultScriptEngineName = "js";
        this.allowCompilation = false;
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context
     * @param next
     *            The next Restlet
     * @param scriptEngineManager
     *            The script engine manager
     * @param defaultScriptEngineName
     *            The default script engine name
     * @param allowCompilation
     *            Whether or not compilation is attempted for script engines
     *            that support it (this is normally undesirable, because the
     *            script will be created and run anew for each entity that
     *            passes through the filter)
     */
    public ScriptedTextFilter(Context context, Restlet next,
            ScriptEngineManager scriptEngineManager,
            String defaultScriptEngineName, boolean allowCompilation) {
        super(context, next);
        this.scriptEngineManager = scriptEngineManager;
        this.defaultScriptEngineName = defaultScriptEngineName;
        this.allowCompilation = allowCompilation;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()) {
            Representation entity = response.getEntity();
            try {
                String text = entity.getText();
                ScriptedTextRepresentation representation = new ScriptedTextRepresentation(
                        entity.getMediaType(), text,
                        this.defaultScriptEngineName, this.allowCompilation,
                        this.scriptEngineManager);
                if (this.scriptContextController != null) {
                    representation
                            .setScriptContextController(this.scriptContextController);
                }
                response.setEntity(representation);
            } catch (IOException e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            } catch (ScriptException e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }
        }
    }

    /**
     * The default script engine name to be used if none is specified.
     * 
     * @return The default script engine name
     * @see #setDefaultScriptEngineName(String)
     */
    public String getDefaultScriptEngineName() {
        return this.defaultScriptEngineName;
    }

    /**
     * An optional script context controller to be used.
     * 
     * @return The script context controller or null if none used
     * @see #setScriptContextController(ScriptContextController)
     */
    public ScriptContextController getScriptContextController() {
        return this.scriptContextController;
    }

    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * script.
     * 
     * @return The script engine manager
     * @see #setScriptEngineManager(ScriptEngineManager)
     */
    public ScriptEngineManager getScriptEngineManager() {
        return this.scriptEngineManager;
    }

    /**
     * Whether or not compilation is attempted for script engines that support
     * it. Compilation is normally undesirable, because the script will be
     * created and run anew for each entity that passes through the filter.
     * 
     * @return Whether to try to compile the script
     */
    public boolean isAllowCompilation() {
        return this.allowCompilation;
    }

    /**
     * @param allowCompilation
     *            Whether to try to compile the script
     * @see #isAllowCompilation()
     */
    public void setAllowCompilation(boolean allowCompilation) {
        this.allowCompilation = allowCompilation;
    }

    /**
     * @param defaultScriptEngineName
     *            The default script engine name
     * @see #getDefaultScriptEngineName()
     */
    public void setDefaultScriptEngineName(String defaultScriptEngineName) {
        this.defaultScriptEngineName = defaultScriptEngineName;
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

    /**
     * @param scriptEngineManager
     *            The script engine manager
     * @see #getScriptEngineManager()
     */
    public void setScriptEngineManager(ScriptEngineManager scriptEngineManager) {
        this.scriptEngineManager = scriptEngineManager;
    }
}
