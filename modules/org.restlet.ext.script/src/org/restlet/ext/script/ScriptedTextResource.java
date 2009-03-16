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
import java.util.HashMap;
import java.util.Map;

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
 * A restlet resource which runs an {@link EmbeddedScript} and redirects its
 * standard output to a {@link Representation}, for both HTTP GET and POST
 * verbs.
 * <p>
 * Before using this resource, make sure to set {@link ScriptSource} to a valid
 * source.
 * <p>
 * This resource supports two modes of output:
 * <ul>
 * <li>Caching mode: First, the entire script is run, with its output sent into
 * a buffer. This buffer is then cached, and <i>only then</i> sent to the
 * client. This is the default mode and recommended for most scripts. Scripts
 * can control the duration of their individual cache by changing the value of
 * script.cacheDuration (see {@link EmbeddedScript}). Because output is not sent
 * to the client until after the script finished its run, it is possible for the
 * script to determine output characteristics at any time by changing the values
 * of container.mediaType, container.characterSet, and container.language (see
 * below).</li>
 * <li>Streaming mode: RepresentableString is sent to the client <i>while</i>
 * the script runs. This is recommended for scripts that need to output a very
 * large amount of string, which might take a long time, or that might otherwise
 * encounter slow-downs while running. In either case, you want the client to
 * receive ongoing output. The output of the script is not cached, and the value
 * of script.cacheDuration is reset to 0. To enter streaming mode, call
 * container.stream() (see below for details). Note that you must determine
 * output characteristics (container.mediaType, container.characterSet, and
 * container.language) <i>before</i> entering streaming mode. Trying to change
 * them while running in streaming mode will raise an exception.
 * </ul>
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
 * <li><b>container.stream()</b>: If you are in caching mode, calling this
 * method will return true and cause the script to run again, where this next
 * run will be in streaming mode. Whatever output the script created in the
 * current run is discarded, and all further exceptions are ignored. For this
 * reason, it's probably best to call container.stream() as early as possible in
 * the script, and then to quit the script as soon as possible if it returns
 * true. For example, your script can start by testing whether it will have a
 * lot of output, and if so, set output characteristics, call
 * container.stream(), and quit. If you are already in streaming mode, calling
 * this method has no effect and returns false. Note that a good way to quit the
 * script is to throw an exception, because it will end the script and otherwise
 * be ignored.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><b>container.variant</b>: The {@link Variant} of this request. Useful for
 * interrogating the client's preferences.</li>
 * <li><b>container.request</b>: The {@link Request}. Useful for accessing URL
 * attributes, form parameters, etc.</li>
 * <li><b>container.response</b>: The {@link Response}. Useful for explicitly
 * setting response characteristics.</li>
 * <li><b>container.isStreaming</b>: This boolean is true when the writer is in
 * streaming mode (see above).</li>
 * <li><b>container.writer</b>: Allows the script direct access to the
 * {@link Writer}. This should rarely be necessary, because by default the
 * standard output for your scripting engine would be directed to it, and the
 * scripting platform's native method for printing should be preferred. However,
 * some scripting platforms may not provide adequate access or may otherwise be
 * broken. Additionally, it may be useful to access the writer during streaming
 * mode. For example, you can call {@link Writer#flush()} to make sure all
 * output is sent to the client.</li>
 * <li><b>container.errorWriter</b>: Same as above, for standard error. (Nothing
 * is currently done with the contents of this, but this may change in future
 * implementations.)</li>
 * <li><b>container.scriptEngineManager</b>: This is the
 * {@link ScriptEngineManager} used to create the script engine. Scripts may use
 * it to get information about what other engines are available.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><b>container.mediaType</b>: The {@link MediaType} that will be used for
 * the generated string. Defaults to what the client requested (in
 * container.variant). If not in streaming mode, your script can change this to
 * something else.</li>
 * <li><b>container.characterSet</b>: The {@link CharacterSet} that will be used
 * for the generated string. Defaults to what the client requested (in
 * container.variant), or to the value of {@link #defaultCharacterSet} if the
 * client did not specify it. If not in streaming mode, your script can change
 * this to something else.</li>
 * <li><b>container.language</b>: The {@link Language} that will be used for the
 * generated string. Defaults to null. If not in streaming mode, your script can
 * change this to something else.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link #scriptContextController} can be set to
 * add your own global variables to each embedded script.
 * 
 * @author Tal Liron
 * @see EmbeddedScript
 * @see ScriptedResource
 */
public class ScriptedTextResource extends Resource {
    /**
     * The {@link ScriptEngineManager} used to create the script engines for the
     * scripts. Uses a default instance, but can be set to something else.
     */
    public static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    /**
     * The {@link ScriptSource} used to fetch scripts. This must be set to a
     * valid value before this class is used!
     */
    public static ScriptSource<EmbeddedScript> scriptSource;

    /**
     *If the URL points to a directory rather than a file, and that directory
     * contains a file with this name, then it will be used. This allows you to
     * use the directory structure to create nice URLs that do not contain
     * filenames. Defaults to "index.page".
     */
    public static String defaultName = "index.page";

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
     * The default variable name for the {@link ScriptedTextResourceContainer}
     * instance. Defaults to "container".
     */
    public static String containerVariableName = "container";

    /**
     * An optional {@link ScriptContextController} to be used with the scripts.
     * Useful for adding your own global variables to the script.
     */
    public static ScriptContextController scriptContextController;

    /**
     * Whether or not compilation is attempted for script engines that support
     * it. Defaults to true.
     */
    public static boolean allowCompilation = true;

    /**
     * This is so we can see the source code for scripts by adding ?source=true
     * to the URL. You probably wouldn't want this for most applications.
     * Defaults to false.
     */
    public static boolean sourceViewable = false;

    private static final String SOURCE = "source";

    private static final String TRUE = "true";

    private static Map<String, RepresentableString> cache = new HashMap<String, RepresentableString>();

    /**
     * @param context
     * @param request
     * @param response
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

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Request request = getRequest();
        String name = ScriptUtils.getRelativePart(request, defaultName);

        try {
            if (sourceViewable
                    && TRUE.equals(request.getResourceRef().getQueryAsForm()
                            .getFirstValue(SOURCE))) {
                // Represent script source
                return new StringRepresentation(scriptSource
                        .getScriptDescriptor(name).getText());
            } else {
                // Run script and represent its output
                ScriptedTextResourceContainer container = new ScriptedTextResourceContainer(
                        variant, request, getResponse(), cache);
                // container.stream();
                Representation representation = container.include(name);
                if (representation == null) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
                } else {
                    return representation;
                }

                /*
                 * String output = container.include(name); if (output == null)
                 * throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
                 * return new StringRepresentation(output, container.mediaType,
                 * container.language, container.characterSet);
                 */
            }
        } catch (FileNotFoundException x) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, x);
        } catch (IOException x) {
            throw new ResourceException(x);
        } catch (ScriptException x) {
            throw new ResourceException(x);
        }
    }
}
