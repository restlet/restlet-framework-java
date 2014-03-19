/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.ext.swagger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.JsonSerializer;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;

/**
 * Root of a Swagger description document.<br>
 * 
 * @author Jerome Louvel
 */
public class SwaggerRepresentation extends OutputRepresentation {

    /** The root element of the Swagger document. */
    private ApplicationInfo application;

    /** The corresponding ApiListing. */
    private ApiListing apiListing;

    /**
     * Constructor.
     */
    public SwaggerRepresentation() {
        super(MediaType.APPLICATION_JSON);
    }

    /**
     * Constructor.
     * 
     * @param application
     *            The root element of the Swagger document.
     */
    public SwaggerRepresentation(ApplicationInfo application) {
        super(MediaType.APPLICATION_JSON);
        // TODO Resource path/basePath?
        java.util.List<String> protocols = new ArrayList<String>();
        for (ConnectorHelper<Server> helper : Engine.getInstance()
                .getRegisteredServers()) {
            for (Protocol protocol : helper.getProtocols()) {
                if (!protocols.contains(protocol.getName())) {
                    protocols.add(protocol.getName());
                }
            }
        }
        // TODO Authorizations
        List<String> produces = new ArrayList<String>();
        List<String> consumes = new ArrayList<String>();
        for (RepresentationInfo ri : application.getRepresentations()) {
            produces.add(ri.getMediaType().getName());
            consumes.add(ri.getMediaType().getName());
        }

        List<Authorization> authorizations = new ArrayList<Authorization>();
        List<ApiDescription> apis = new ArrayList<ApiDescription>();
        for (ResourceInfo ri : application.getResources().getResources()) {
            List<Operation> ops = new ArrayList<Operation>();
            int i = 0;
            for (MethodInfo mi : ri.getMethods()) {
                List<Parameter> params = new ArrayList<Parameter>();
                if (mi.getRequest() != null
                        && mi.getRequest().getParameters() != null) {
                    for (ParameterInfo pi : mi.getRequest().getParameters()) {
                        Parameter param = new Parameter(pi.getName(),
                                scala.Option.apply(toString(pi
                                        .getDocumentations())),
                                scala.Option.apply(pi.getDefaultValue()), true,
                                true, pi.getType(), new AllowableListValues(
                                        null, pi.getType()), pi.getStyle()
                                        .name(),
                                scala.Option.apply("paramAccess"));
                        params.add(param);
                    }
                }
                List<ResponseMessage> liste = new ArrayList<ResponseMessage>();
                Operation op = new Operation(mi.getName().getName(),
                        toString(mi.getDocumentations()), "", "",
                        mi.getIdentifier(), i, scala.collection.JavaConversions
                                .asScalaIterable(consumes).toList(),
                        scala.collection.JavaConversions.asScalaIterable(
                                consumes).toList(),
                        scala.collection.JavaConversions.asScalaIterable(
                                protocols).toList(),
                        scala.collection.JavaConversions.asScalaIterable(
                                authorizations).toList(),
                        scala.collection.JavaConversions
                                .asScalaIterable(params).toList(),
                        scala.collection.JavaConversions.asScalaIterable(liste)
                                .toList(), scala.Option.apply(""));
                ops.add(op);
                i++;
            }
            apis.add(new ApiDescription(ri.getPath(), scala.Option
                    .apply(toString(ri.getDocumentations())),
                    scala.collection.JavaConversions.asScalaIterable(ops)
                            .toList()));
        }

        Map<String, Model> models = new java.util.HashMap<String, Model>();
        scala.collection.immutable.Map<String, Model> pouet = new scala.collection.immutable.HashMap<String, Model>();
        scala.Option<scala.collection.immutable.Map<String, Model>> truc = scala.Option
                .apply(pouet);
        apiListing = new ApiListing(
                application.getVersion(),
                SwaggerSpec.version(),
                application.getResources().getBaseRef().toString(),
                application.getResources().getBaseRef().toString(),
                scala.collection.JavaConversions.asScalaIterable(consumes)
                        .toList(),
                scala.collection.JavaConversions.asScalaIterable(produces)
                        .toList(),
                scala.collection.JavaConversions.asScalaIterable(protocols)
                        .toList(),
                scala.collection.JavaConversions
                        .asScalaIterable(authorizations).toList(),
                scala.collection.JavaConversions.asScalaIterable(apis).toList(),
                truc, scala.Option.apply(toString(application
                        .getDocumentations())), 0);

        this.application = application;
    }

    private String toString(List<DocumentationInfo> di) {
        StringBuilder d = new StringBuilder();
        for (DocumentationInfo doc : di) {
            d.append(doc.getTextContent());
        }
        return d.toString();
    }

    /**
     * Constructor.
     * 
     * @param application
     *            The root element of the Swagger document.
     */
    public SwaggerRepresentation(ApiListing apiListing) {
        super(MediaType.APPLICATION_JSON);
        // Transform ApiListing to ApplicationInfo
        this.application = application;
    }

    // /**
    // * Constructor.
    // *
    // * @param representation
    // * The XML Swagger document.
    // * @throws IOException
    // */
    // public SwaggerRepresentation(Representation representation)
    // throws IOException {
    // super(representation);
    // setMediaType(MediaType.APPLICATION_JSON);
    //
    // // Parse the given document using SAX to produce an ApplicationInfo
    // // instance.
    // // parse(new ContentReader(this));
    // }

    /**
     * Constructor. The title of the resource, that is to say the title of its
     * first documentation tag is transfered to the title of the first
     * documentation tag of the main application tag.
     * 
     * @param resource
     *            The root element of the Swagger document.
     */
    public SwaggerRepresentation(ResourceInfo resource) {
        super(MediaType.APPLICATION_JSON);
        this.application = resource.createApplication();
    }

    /**
     * Returns the root element of the Swagger document.
     * 
     * @return The root element of the Swagger document.
     */
    public ApplicationInfo getApplication() {
        return this.application;
    }

    /**
     * Returns an HTML representation. Note that the internal XSLT stylesheet
     * used comes from <a href="http://www.mnot.net/webdesc/">Mark
     * Nottingham</a>. This stylesheet requires advanced XSLT features,
     * including EXSLT extensions. Usage of a recent version of Xalan-J is
     * suggested. It has been tested successfully with Xalan-J 2.7.1.
     * 
     * @return An HTML representation.
     */
    public Representation getHtmlRepresentation() {
        Representation representation = null;
        return representation;
    }

    @Override
    public InputStream getStream() throws IOException {
        return new ByteArrayInputStream(JsonSerializer.asJson(this.apiListing)
                .getBytes());
    }

    /**
     * Sets the root element of the Swagger document.
     * 
     * @param application
     *            The root element of the Swagger document.
     */
    public void setApplication(ApplicationInfo application) {
        this.application = application;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        Writer writer = IoUtils.getWriter(outputStream, getCharacterSet());
        write(writer);
        writer.flush();
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (this.apiListing != null) {
            writer.write(JsonSerializer.asJson(this.apiListing));
            writer.flush();
        }
    }

}
