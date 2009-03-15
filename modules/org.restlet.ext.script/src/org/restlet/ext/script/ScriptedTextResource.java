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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.EmbeddedScript;
import com.threecrickets.scripturian.ScriptContextController;
import com.threecrickets.scripturian.ScriptSource;
import com.threecrickets.scripturian.file.ScriptFileSource;

/**
 * A restlet resource which runs an {@link EmbeddedScript} and redirects its
 * standard output to a {@link Representation}, for both HTTP GET and POST
 * verbs.
 * <p>
 * It works well with a {@link ByExtensionAndDirectorySwitcher} and a
 * {@link Directory} to allow transparent and optimized serving of file systems
 * which include content both static and "dynamic" (with embedded scripts). For
 * example, all files ending in with the ".page" extension can be switched to
 * this resource, while all the rest are switched to the directory. For this to
 * work, you can use a {@link ScriptFileSource} with a base path identical to
 * the root of the directory. Other switched configurations are possible and may
 * be useful.
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
     * This is the type of the "container" variable exposed to the script. The
     * name is set according to
     * {@link ScriptedTextResource#containerVariableName}.
     */
    public static class Container {
        private class Controller implements ScriptContextController {
            public void finalize(ScriptContext scriptContext) {
                scriptSource.finalize(scriptContext);

                if (scriptContextController != null) {
                    scriptContextController.finalize(scriptContext);
                }
            }

            public void initialize(ScriptContext scriptContext)
                    throws ScriptException {
                scriptContext.setAttribute(containerVariableName,
                        Container.this, ScriptContext.ENGINE_SCOPE);

                if (scriptContextController != null) {
                    scriptContextController.initialize(scriptContext);
                }

                scriptSource.initialize(scriptContext);
            }
        }

        private class StreamingRepresentation extends WriterRepresentation {
            private final EmbeddedScript script;

            public StreamingRepresentation(EmbeddedScript script) {
                // Note that we are setting representation characteristics
                // before we actually run the script
                super(Container.this.mediaType);
                setCharacterSet(Container.this.characterSet);
                if (Container.this.language != null) {
                    setLanguages(Arrays
                            .asList(new Language[] { Container.this.language }));
                }
                this.script = script;
            }

            @Override
            public void write(Writer writer) throws IOException {
                // writer = new OutputStreamWriter(System.out);
                Container.this.isStreaming = true;
                Container.this.writer = writer;
                try {
                    this.script.run(writer, Container.this.errorWriter,
                            Container.this.scriptEngines,
                            Container.this.controller, false);
                } catch (ScriptException x) {
                    IOException xx = new IOException("Script exception");
                    xx.initCause(x);
                    throw xx;
                } finally {
                    // The script may have set its cacheDuration, so we must
                    // make sure to disable it!
                    this.script.setCacheDuration(0);

                    writer.close();
                }
            }
        }

        private boolean startStreaming;

        private final Variant variant;

        private final Request request;

        private final Response response;

        private MediaType mediaType;

        private CharacterSet characterSet;

        private Language language;

        private boolean isStreaming;

        private Writer writer;

        private final Writer errorWriter = new StringWriter();

        private StringBuffer buffer;

        private final Controller controller = new Controller();

        private final Map<String, ScriptEngine> scriptEngines = new HashMap<String, ScriptEngine>();

        private Container(Variant variant, Request request, Response response) {
            this.variant = variant;
            this.request = request;
            this.response = response;
            this.mediaType = variant.getMediaType();
            this.characterSet = variant.getCharacterSet();
            if (this.characterSet == null) {
                this.characterSet = defaultCharacterSet;
            }
        }

        /**
         * The {@link CharacterSet} that will be used for the generated string.
         * Defaults to what the client requested (in container.variant), or to
         * the value of {@link ScriptedTextResource#defaultCharacterSet} if the
         * client did not specify it. If not in streaming mode, your script can
         * change this to something else.
         * 
         * @return The character set
         * @see #setCharacterSet(CharacterSet)
         */
        public CharacterSet getCharacterSet() {
            return this.characterSet;
        }

        /**
         * Same as {@link #getWriter()}, for standard error. (Nothing is
         * currently done with the contents of this, but this may change in
         * future implementations.)
         * 
         * @return The error writer
         */
        public Writer getErrorWriter() {
            return this.errorWriter;
        }

        /**
         * This boolean is true when the writer is in streaming mode.
         * 
         * @return True if in streaming mode, false if in caching mode
         */
        public boolean getIsStreaming() {
            return this.isStreaming;
        }

        /**
         * The {@link Language} that will be used for the generated string.
         * Defaults to null. If not in streaming mode, your script can change
         * this to something else.
         * 
         * @return The language or null if set
         * @see #setLanguage(Language)
         */
        public Language getLanguage() {
            return this.language;
        }

        /**
         * The {@link MediaType} that will be used for the generated string.
         * Defaults to what the client requested (in container.variant). If not
         * in streaming mode, your script can change this to something else.
         * 
         * @return The media type
         * @see #setMediaType(MediaType)
         */
        public MediaType getMediaType() {
            return this.mediaType;
        }

        /**
         * The {@link Request}. Useful for accessing URL attributes, form
         * parameters, etc.
         * 
         * @return The request
         */
        public Request getRequest() {
            return this.request;
        }

        /**
         * The {@link Response}. Useful for explicitly setting response
         * characteristics.
         * 
         * @return The response
         */
        public Response getResponse() {
            return this.response;
        }

        /**
         * This is the {@link ScriptEngineManager} used to create the script
         * engine. Scripts may use it to get information about what other
         * engines are available.
         * 
         * @return The script engine manager
         */
        public ScriptEngineManager getScriptEngineManager() {
            return scriptEngineManager;
        }

        /**
         * The {@link Variant} of this request. Useful for interrogating the
         * client's preferences.
         * 
         * @return The variant
         */
        public Variant getVariant() {
            return this.variant;
        }

        /**
         * Allows the script direct access to the {@link Writer}. This should
         * rarely be necessary, because by default the standard output for your
         * scripting engine would be directed to it, and the scripting
         * platform's native method for printing should be preferred. However,
         * some scripting platforms may not provide adequate access or may
         * otherwise be broken. Additionally, it may be useful to access the
         * writer during streaming mode. For example, you can call
         * {@link Writer#flush()} to make sure all output is sent to the client.
         * 
         * @return The writer
         */
        public Writer getWriter() {
            return this.writer;
        }

        /**
         * This powerful method allows scripts to execute other scripts in
         * place, and is useful for creating large, maintainable applications
         * based on scripts. Included scripts can act as a library or toolkit
         * and can even be shared among many applications. The included script
         * does not have to be in the same language or use the same engine as
         * the calling script. However, if they do use the same engine, then
         * methods, functions, modules, etc., could be shared. It is important
         * to note that how this works varies a lot per scripting platform. For
         * example, in JRuby, every script is run in its own scope, so that
         * sharing would have to be done explicitly in the global scope. See the
         * included embedded Ruby script example for a discussion of various
         * ways to do this.
         * 
         * @param name
         *            The script name
         * @return A representation of the script's output
         * @throws IOException
         * @throws ScriptException
         */
        public Representation include(String name) throws IOException,
                ScriptException {
            return include(name, null);
        }

        /**
         * As {@link #include(String)}, except that the script is not embedded.
         * As such, you must explicitly specify the name of the scripting engine
         * that should evaluate it.
         * 
         * @param name
         *            The script name
         * @param scriptEngineName
         *            The script engine name (if null, behaves identically to
         *            {@link #include(String)}
         * @return A representation of the script's output
         * @throws IOException
         * @throws ScriptException
         */
        public Representation include(String name, String scriptEngineName)
                throws IOException, ScriptException {

            // Get script descriptor
            ScriptSource.ScriptDescriptor<EmbeddedScript> scriptDescriptor = scriptSource
                    .getScriptDescriptor(name);

            EmbeddedScript script = scriptDescriptor.getScript();
            if (script == null) {
                // Create script from descriptor
                String text = scriptDescriptor.getText();
                if (scriptEngineName != null) {
                    text = EmbeddedScript.delimiter1Start + scriptEngineName
                            + " " + text + EmbeddedScript.delimiter1End;
                }
                script = new EmbeddedScript(text, scriptEngineManager,
                        defaultEngineName, allowCompilation);
                scriptDescriptor.setScript(script);
            }

            // Special handling for trivial scripts
            String trivial = script.getTrivial();
            if (trivial != null) {
                if (this.writer != null) {
                    this.writer.write(trivial);
                }
                return new StringRepresentation(trivial, this.mediaType,
                        this.language, this.characterSet);
            }

            int startPosition = 0;

            // Make sure we have a valid writer for caching mode
            if (!this.isStreaming) {
                if (this.writer == null) {
                    StringWriter stringWriter = new StringWriter();
                    this.buffer = stringWriter.getBuffer();
                    this.writer = new BufferedWriter(stringWriter);
                } else {
                    this.writer.flush();
                    startPosition = this.buffer.length();
                }
            }

            try {
                // Do not allow caching in streaming mode
                if (script.run(this.writer, this.errorWriter,
                        this.scriptEngines, this.controller, !this.isStreaming)) {

                    // Did the script ask us to start streaming?
                    if (this.startStreaming) {
                        this.startStreaming = false;

                        // Note that this will cause the script to run again!
                        return new StreamingRepresentation(script);
                    }

                    if (this.isStreaming) {
                        // Nothing to return in streaming mode
                        return null;
                    } else {
                        this.writer.flush();

                        // Get the buffer from when we ran the script
                        RepresentableString string = new RepresentableString(
                                this.buffer.substring(startPosition),
                                this.mediaType, this.language,
                                this.characterSet);

                        // Cache it
                        cache.put(name, string);

                        // Return a representation of the entire buffer
                        if (startPosition == 0) {
                            return string.represent();
                        } else {
                            return new StringRepresentation(this.buffer
                                    .toString(), this.mediaType, this.language,
                                    this.characterSet);
                        }
                    }
                } else {
                    // Attempt to use cache
                    RepresentableString string = cache.get(name);
                    if (string != null) {
                        if (this.writer != null) {
                            this.writer.write(string.string);
                        }
                        return string.represent();
                    } else {
                        return null;
                    }
                }
            } catch (ScriptException x) {
                // Did the script ask us to start streaming?
                if (this.startStreaming) {
                    this.startStreaming = false;

                    // Note that this will cause the script to run again!
                    return new StreamingRepresentation(script);

                    // Note that we will allow exceptions in scripts that ask us
                    // to start streaming! In fact, throwing an exception is a
                    // good way for the script to signal that it's done and is
                    // ready to start streaming.
                } else {
                    throw x;
                }
            }
        }

        /**
         * Throws an {@link IllegalStateException} if in streaming mode.
         * 
         * @param characterSet
         *            The character set
         * @see #getCharacterSet()
         */
        public void setCharacterSet(CharacterSet characterSet) {
            if (this.isStreaming) {
                throw new IllegalStateException(
                        "Cannot change character set while streaming");
            }
            this.characterSet = characterSet;
        }

        /**
         * Throws an {@link IllegalStateException} if in streaming mode.
         * 
         * @param language
         *            The language or null
         * @see #getLanguage()
         */
        public void setLanguage(Language language) {
            if (this.isStreaming) {
                throw new IllegalStateException(
                        "Cannot change language while streaming");
            }
            this.language = language;
        }

        /**
         * Throws an {@link IllegalStateException} if in streaming mode.
         * 
         * @param mediaType
         *            The media type
         * @see #getMediaType()
         */
        public void setMediaType(MediaType mediaType) {
            if (this.isStreaming) {
                throw new IllegalStateException(
                        "Cannot change media type while streaming");
            }
            this.mediaType = mediaType;
        }

        /**
         * If you are in caching mode, calling this method will return true and
         * cause the script to run again, where this next run will be in
         * streaming mode. Whatever output the script created in the current run
         * is discarded, and all further exceptions are ignored. For this
         * reason, it's probably best to call container.stream() as early as
         * possible in the script, and then to quit the script as soon as
         * possible if it returns true. For example, your script can start by
         * testing whether it will have a lot of output, and if so, set output
         * characteristics, call container.stream(), and quit. If you are
         * already in streaming mode, calling this method has no effect and
         * returns false. Note that a good way to quit the script is to throw an
         * exception, because it will end the script and otherwise be ignored.
         * 
         * @return True is started streaming mode, false if already in streaming
         *         mode
         */
        public boolean stream() {
            if (this.isStreaming) {
                return false;
            }
            this.startStreaming = true;
            return true;
        }
    }

    private static class RepresentableString {
        private final String string;

        private final MediaType mediaType;

        private final Language language;

        private final CharacterSet characterSet;

        private RepresentableString(String string, MediaType mediaType,
                Language language, CharacterSet characterSet) {
            this.string = string;
            this.mediaType = mediaType;
            this.language = language;
            this.characterSet = characterSet;
        }

        public StringRepresentation represent() {
            return new StringRepresentation(this.string, this.mediaType,
                    this.language, this.characterSet);
        }
    }

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
     * The default variable name for the {@link Container} instance. Defaults to
     * "container".
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
                Container container = new Container(variant, request,
                        getResponse());
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
