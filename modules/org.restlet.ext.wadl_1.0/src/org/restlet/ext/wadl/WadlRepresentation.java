/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.wadl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.SaxRepresentation;
import org.restlet.resource.TransformRepresentation;
import org.restlet.util.Engine;
import org.restlet.util.XmlWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Root of a WADL description document.
 * 
 * @author Jerome Louvel
 */
public class WadlRepresentation extends SaxRepresentation {

    /** Obtain a suitable logger. */
    private static Logger logger = Logger.getLogger(WadlRepresentation.class
            .getCanonicalName());

    // -------------------
    // Content reader part
    // -------------------
    private static class ContentReader extends DefaultHandler implements
            LexicalHandler {
        public enum State {
            APPLICATION, DOCUMENTATION, FAULT, GRAMMARS, INCLUDE, LINK, METHOD, NONE, OPTION, PARAMETER, REPRESENTATION, REQUEST, RESOURCE, RESOURCES, RESOURCETYPE, RESPONSE
        }

        /** Glean the content of the current parsed element. */
        private StringBuilder contentBuffer;

        /** The current parsed "application" tag. */
        private ApplicationInfo currentApplication;

        /** The current parsed "documentation" tag. */
        private DocumentationInfo currentDocumentation;

        /** The current parsed "fault" tag. */
        private FaultInfo currentFault;

        /** The current parsed "grammars" tag. */
        private GrammarsInfo currentGrammars;

        /** The current parsed "include" tag. */
        private IncludeInfo currentInclude;

        /** The current parsed "link" tag. */
        private LinkInfo currentLink;

        /** The current parsed "method" tag. */
        private MethodInfo currentMethod;

        /** The current parsed "option" tag. */
        private OptionInfo currentOption;

        /** The current parsed "param" tag. */
        private ParameterInfo currentParameter;

        /** The current parsed "representaiton" tag. */
        private RepresentationInfo currentRepresentation;

        /** The current parsed "request" tag. */
        private RequestInfo currentRequest;

        /** The current parsed "resources" tag. */
        private ResourcesInfo currentResources;

        /** The list of the current parsed "resource" tags. */
        private List<ResourceInfo> currentResourcesList;

        /** The current parsed "resource_type" tag. */
        private ResourceTypeInfo currentResourceType;

        /** The current parsed "response" tag. */
        private ResponseInfo currentResponse;

        /** The stack of parser states. */
        private List<State> states;

        /** The WadlRepresentation instance that represents the parsed document. */
        private WadlRepresentation wadlRepresentation;

        /**
         * Constructor
         * 
         * @param wadlRepresentation
         *            The WadlRepresentation instance that represents the parsed
         *            document.
         */
        public ContentReader(WadlRepresentation wadlRepresentation) {
            this.states = new ArrayList<State>();
            this.states.add(State.NONE);
            this.currentApplication = null;
            this.currentDocumentation = null;
            this.currentFault = null;
            this.currentGrammars = null;
            this.currentInclude = null;
            this.currentLink = null;
            this.currentMethod = null;
            this.currentOption = null;
            this.currentParameter = null;
            this.currentRepresentation = null;
            this.currentRequest = null;
            this.currentResourcesList = new ArrayList<ResourceInfo>();
            this.currentResources = null;
            this.currentResourceType = null;
            this.currentResponse = null;
            this.wadlRepresentation = wadlRepresentation;
        }

        /**
         * Receive notification of character data.
         * 
         * @param ch
         *            The characters from the XML document.
         * @param start
         *            The start position in the array.
         * @param length
         *            The number of characters to read from the array.
         */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            contentBuffer.append(ch, start, length);
        }

        public void comment(char[] ch, int start, int length)
                throws SAXException {
        }

        public void endCDATA() throws SAXException {
            if (getState() == State.DOCUMENTATION) {
                contentBuffer.append("]]>");
            }
        }

        /**
         * Receive notification of the end of a document.
         */
        @Override
        public void endDocument() throws SAXException {
            popState();
            this.contentBuffer = null;
            wadlRepresentation.setApplication(this.currentApplication);
        }

        public void endDTD() throws SAXException {
        }

        /**
         * Receive notification of the end of an element.
         * 
         * @param uri
         *            The Namespace URI, or the empty string if the element has
         *            no Namespace URI or if Namespace processing is not being
         *            performed.
         * @param localName
         *            The local name (without prefix), or the empty string if
         *            Namespace processing is not being performed.
         * @param qName
         *            The qualified XML name (with prefix), or the empty string
         *            if qualified names are not available.
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (uri.equalsIgnoreCase(APP_NAMESPACE)) {
                if (localName.equals("currentApplication")) {
                    popState();
                } else if (localName.equals("doc")) {
                    // Get the current text.
                    this.currentDocumentation.setTextContent(contentBuffer
                            .toString());
                    popState();
                } else if (localName.equals("fault")) {
                    popState();
                } else if (localName.equals("grammars")) {
                    popState();
                } else if (localName.equals("include")) {
                    popState();
                } else if (localName.equals("link")) {
                    popState();
                } else if (localName.equals("method")) {
                    popState();
                } else if (localName.equals("option")) {
                    popState();
                } else if (localName.equals("param")) {
                    popState();
                } else if (localName.equals("representation")) {
                    popState();
                } else if (localName.equals("request")) {
                    popState();
                } else if (localName.equals("resource")) {
                    this.currentResourcesList.remove(0);
                    popState();
                } else if (localName.equals("resources")) {
                    popState();
                } else if (localName.equals("resource_type")) {
                    popState();
                } else if (localName.equals("response")) {
                    popState();
                }
            }
        }

        public void endEntity(String name) throws SAXException {
        }

        /**
         * Returns a parameterStyle value according to the given string or null.
         * 
         * @param parameterStyle
         *            The given string.
         * @return
         */
        public ParameterStyle getParameterStyle(String parameterStyle) {
            ParameterStyle result = null;
            if ("header".equalsIgnoreCase(parameterStyle)) {
                result = ParameterStyle.HEADER;
            } else if ("matrix".equalsIgnoreCase(parameterStyle)) {
                result = ParameterStyle.MATRIX;
            } else if ("plain".equalsIgnoreCase(parameterStyle)) {
                result = ParameterStyle.PLAIN;
            } else if ("query".equalsIgnoreCase(parameterStyle)) {
                result = ParameterStyle.QUERY;
            } else if ("template".equalsIgnoreCase(parameterStyle)) {
                result = ParameterStyle.TEMPLATE;
            }

            return result;
        }

        /**
         * Returns the state at the beginning of the stack
         * 
         * @return
         */
        private State getState() {
            State result = this.states.get(0);
            return result;
        }

        /**
         * Returns the state at the beginning of the stack
         * 
         * @return
         */
        private State popState() {
            return this.states.remove(0);
        }

        /**
         * Adds the given state.
         * 
         * @param state
         */
        private void pushState(State state) {
            this.states.add(0, state);
        }

        public void startCDATA() throws SAXException {
            if (getState() == State.DOCUMENTATION) {
                contentBuffer.append("<![CDATA[");
            }
        }

        /**
         * Receive notification of the beginning of a document.
         */
        @Override
        public void startDocument() throws SAXException {
            this.contentBuffer = new StringBuilder();
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
        }

        /**
         * Receive notification of the beginning of an element.
         * 
         * @param uri
         *            The Namespace URI, or the empty string if the element has
         *            no Namespace URI or if Namespace processing is not being
         *            performed.
         * @param localName
         *            The local name (without prefix), or the empty string if
         *            Namespace processing is not being performed.
         * @param qName
         *            The qualified name (with prefix), or the empty string if
         *            qualified names are not available.
         * @param attrs
         *            The attributes attached to the element. If there are no
         *            attributes, it shall be an empty Attributes object. The
         *            value of this object after startElement returns is
         *            undefined.
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attrs) throws SAXException {
            if (getState() != State.DOCUMENTATION) {
                this.contentBuffer.delete(0, this.contentBuffer.length() + 1);
            }

            if (uri.equalsIgnoreCase(APP_NAMESPACE)) {
                if (localName.equals("application")) {
                    this.currentApplication = new ApplicationInfo();
                    pushState(State.APPLICATION);
                } else if (localName.equals("doc")) {
                    this.currentDocumentation = new DocumentationInfo();

                    if (attrs.getIndex("xml:lang") != -1) {
                        this.currentDocumentation.setLanguage(Language
                                .valueOf(attrs.getValue("xml:lang")));
                    }
                    if (attrs.getIndex("lang") != -1) {
                        this.currentDocumentation.setLanguage(Language
                                .valueOf(attrs.getValue("lang")));
                    }
                    if (attrs.getIndex("title") != -1) {
                        this.currentDocumentation.setTitle(attrs
                                .getValue("title"));
                    }
                    if (getState() == State.APPLICATION) {
                        this.currentApplication.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.FAULT) {
                        this.currentFault.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.GRAMMARS) {
                        this.currentGrammars.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.INCLUDE) {
                        this.currentInclude.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.LINK) {
                        this.currentLink.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.METHOD) {
                        this.currentMethod.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.OPTION) {
                        this.currentOption.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.PARAMETER) {
                        this.currentParameter.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.REPRESENTATION) {
                        this.currentRepresentation.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.REQUEST) {
                        this.currentRequest.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.RESOURCE) {
                        this.currentResourcesList.get(0).getDocumentations()
                                .add(this.currentDocumentation);
                    } else if (getState() == State.RESOURCES) {
                        this.currentResources.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.RESOURCETYPE) {
                        this.currentResourceType.getDocumentations().add(
                                this.currentDocumentation);
                    } else if (getState() == State.RESPONSE) {
                        this.currentResponse.getDocumentations().add(
                                this.currentDocumentation);
                    }
                    pushState(State.DOCUMENTATION);
                } else if (localName.equals("fault")) {
                    this.currentFault = new FaultInfo();
                    if (attrs.getIndex("id") != -1) {
                        this.currentFault.setIdentifier(attrs.getValue("id"));
                    }
                    if (attrs.getIndex("mediaType") != -1) {
                        this.currentFault.setMediaType(MediaType.valueOf(attrs
                                .getValue("mediaType")));
                    }
                    if (attrs.getIndex("element") != -1) {
                        this.currentFault.setXmlElement(attrs
                                .getValue("element"));
                    }
                    if (attrs.getIndex("profile") != -1) {
                        String[] profiles = attrs.getValue("profile")
                                .split(" ");
                        for (String string : profiles) {
                            this.currentFault.getProfiles().add(
                                    new Reference(string));
                        }
                    }
                    if (attrs.getIndex("status") != -1) {
                        String[] statuses = attrs.getValue("status").split(" ");
                        for (String string : statuses) {
                            this.currentFault.getStatuses().add(
                                    Status.valueOf(Integer.parseInt(string)));
                        }
                    }

                    if (getState() == State.APPLICATION) {
                        this.currentApplication.getFaults().add(
                                this.currentFault);
                    } else if (getState() == State.RESPONSE) {
                        this.currentResponse.getFaults().add(this.currentFault);
                    }
                    pushState(State.FAULT);
                } else if (localName.equals("grammars")) {
                    this.currentGrammars = new GrammarsInfo();
                    if (getState() == State.APPLICATION) {
                        this.currentApplication
                                .setGrammars(this.currentGrammars);
                    }
                    pushState(State.GRAMMARS);
                } else if (localName.equals("include")) {
                    this.currentInclude = new IncludeInfo();
                    if (attrs.getIndex("href") != -1) {
                        this.currentInclude.setTargetRef(new Reference(attrs
                                .getValue("href")));
                    }
                    if (getState() == State.GRAMMARS) {
                        this.currentGrammars.getIncludes().add(
                                this.currentInclude);
                    }
                    pushState(State.INCLUDE);
                } else if (localName.equals("link")) {
                    this.currentLink = new LinkInfo();
                    if (attrs.getIndex("rel") != -1) {
                        this.currentLink.setRelationship(attrs.getValue("rel"));
                    }
                    if (attrs.getIndex("rev") != -1) {
                        this.currentLink.setReverseRelationship(attrs
                                .getValue("rev"));
                    }
                    if (attrs.getIndex("resource_type") != -1) {
                        this.currentLink.setResourceType(new Reference(attrs
                                .getValue("resource_type")));
                    }

                    if (getState() == State.PARAMETER) {
                        this.currentParameter.setLink(this.currentLink);
                    }
                    pushState(State.LINK);
                } else if (localName.equals("method")) {
                    this.currentMethod = new MethodInfo();
                    if (attrs.getIndex("href") != -1) {
                        this.currentMethod.setTargetRef(new Reference(attrs
                                .getValue("href")));
                    }
                    if (attrs.getIndex("id") != -1) {
                        this.currentMethod.setIdentifier(attrs.getValue("id"));
                    }
                    if (attrs.getIndex("name") != -1) {
                        this.currentMethod.setName(Method.valueOf(attrs
                                .getValue("name")));
                    }

                    if (getState() == State.APPLICATION) {
                        this.currentApplication.getMethods().add(
                                this.currentMethod);
                    } else if (getState() == State.RESOURCE) {
                        this.currentResourcesList.get(0).getMethods().add(
                                this.currentMethod);
                    } else if (getState() == State.RESOURCETYPE) {
                        this.currentResourceType.getMethods().add(
                                this.currentMethod);
                    }
                    pushState(State.METHOD);
                } else if (localName.equals("option")) {
                    this.currentOption = new OptionInfo();
                    if (attrs.getIndex("value") != -1) {
                        this.currentOption.setValue(attrs.getValue("value"));
                    }
                    if (getState() == State.PARAMETER) {
                        this.currentParameter.getOptions().add(
                                this.currentOption);
                    }
                    pushState(State.OPTION);
                } else if (localName.equals("param")) {
                    this.currentParameter = new ParameterInfo();
                    if (attrs.getIndex("default") != -1) {
                        this.currentParameter.setDefaultValue(attrs
                                .getValue("default"));
                    }
                    if (attrs.getIndex("fixed") != -1) {
                        this.currentParameter.setFixed(attrs.getValue("fixed"));
                    }
                    if (attrs.getIndex("id") != -1) {
                        this.currentParameter.setIdentifier(attrs
                                .getValue("id"));
                    }
                    if (attrs.getIndex("path") != -1) {
                        this.currentParameter.setPath(attrs.getValue("path"));
                    }
                    if (attrs.getIndex("style") != -1) {
                        this.currentParameter.setStyle(getParameterStyle(attrs
                                .getValue("style")));
                    }
                    if (attrs.getIndex("name") != -1) {
                        this.currentParameter.setName(attrs.getValue("name"));
                    }
                    if (attrs.getIndex("type") != -1) {
                        this.currentParameter.setType(attrs.getValue("type"));
                    }
                    if (attrs.getIndex("repeating") != -1) {
                        this.currentParameter.setRepeating(Boolean
                                .parseBoolean(attrs.getValue("repeating")));
                    }
                    if (attrs.getIndex("required") != -1) {
                        this.currentParameter.setRequired(Boolean
                                .parseBoolean(attrs.getValue("required")));
                    }

                    if (getState() == State.FAULT) {
                        this.currentFault.getParameters().add(
                                this.currentParameter);
                    } else if (getState() == State.REPRESENTATION) {
                        this.currentRepresentation.getParameters().add(
                                this.currentParameter);
                    } else if (getState() == State.REQUEST) {
                        this.currentRequest.getParameters().add(
                                this.currentParameter);
                    } else if (getState() == State.RESOURCE) {
                        this.currentResourcesList.get(0).getParameters().add(
                                this.currentParameter);
                    } else if (getState() == State.RESOURCETYPE) {
                        this.currentRequest.getParameters().add(
                                this.currentParameter);
                    } else if (getState() == State.RESPONSE) {
                        this.currentRequest.getParameters().add(
                                this.currentParameter);
                    }

                    pushState(State.PARAMETER);
                } else if (localName.equals("representation")) {
                    this.currentRepresentation = new RepresentationInfo();
                    if (attrs.getIndex("id") != -1) {
                        this.currentRepresentation.setIdentifier(attrs
                                .getValue("id"));
                    }
                    if (attrs.getIndex("mediaType") != -1) {
                        this.currentRepresentation.setMediaType(MediaType
                                .valueOf(attrs.getValue("mediaType")));
                    }
                    if (attrs.getIndex("element") != -1) {
                        this.currentRepresentation.setXmlElement(attrs
                                .getValue("element"));
                    }
                    if (attrs.getIndex("profile") != -1) {
                        String[] profiles = attrs.getValue("profile")
                                .split(" ");
                        for (String string : profiles) {
                            this.currentRepresentation.getProfiles().add(
                                    new Reference(string));
                        }
                    }
                    if (attrs.getIndex("status") != -1) {
                        String[] statuses = attrs.getValue("status").split(" ");
                        for (String string : statuses) {
                            this.currentRepresentation.getStatuses().add(
                                    Status.valueOf(Integer.parseInt(string)));
                        }
                    }

                    if (getState() == State.APPLICATION) {
                        this.currentApplication.getRepresentations().add(
                                this.currentRepresentation);
                    } else if (getState() == State.REQUEST) {
                        this.currentRequest.getRepresentations().add(
                                this.currentRepresentation);
                    } else if (getState() == State.RESPONSE) {
                        this.currentResponse.getRepresentations().add(
                                this.currentRepresentation);
                    }
                    pushState(State.REPRESENTATION);
                } else if (localName.equals("request")) {
                    this.currentRequest = new RequestInfo();
                    if (getState() == State.METHOD) {
                        this.currentMethod.setRequest(this.currentRequest);
                    }
                    pushState(State.REQUEST);
                } else if (localName.equals("resource")) {
                    ResourceInfo resourceInfo = new ResourceInfo();
                    if (attrs.getIndex("id") != -1) {
                        resourceInfo.setIdentifier(attrs.getValue("id"));
                    }
                    if (attrs.getIndex("path") != -1) {
                        resourceInfo.setPath(attrs.getValue("path"));
                    }
                    if (attrs.getIndex("queryType") != -1) {
                        resourceInfo.setQueryType(MediaType.valueOf(attrs
                                .getValue("queryType")));
                    }
                    if (attrs.getIndex("type") != -1) {
                        String[] type = attrs.getValue("type").split(" ");
                        for (String string : type) {
                            resourceInfo.getType().add(new Reference(string));
                        }
                    }

                    if (getState() == State.RESOURCE) {
                        this.currentResourcesList.get(0).getChildResources()
                                .add(resourceInfo);
                    } else if (getState() == State.RESOURCES) {
                        this.currentResources.getResources().add(resourceInfo);
                    }

                    this.currentResourcesList.add(0, resourceInfo);
                    pushState(State.RESOURCE);
                } else if (localName.equals("resources")) {
                    this.currentResources = new ResourcesInfo();
                    if (attrs.getIndex("base") != -1) {
                        this.currentResources.setBaseRef(new Reference(attrs
                                .getValue("base")));
                    }
                    if (getState() == State.APPLICATION) {
                        this.currentApplication
                                .setResources(this.currentResources);
                    }
                    pushState(State.RESOURCES);
                } else if (localName.equals("resource_type")) {
                    this.currentResourceType = new ResourceTypeInfo();
                    if (attrs.getIndex("id") != -1) {
                        this.currentResourceType.setIdentifier(attrs
                                .getValue("id"));
                    }

                    if (getState() == State.APPLICATION) {
                        this.currentApplication.getResourceTypes().add(
                                this.currentResourceType);
                    }
                    pushState(State.RESOURCETYPE);
                } else if (localName.equals("response")) {
                    this.currentResponse = new ResponseInfo();
                    if (getState() == State.METHOD) {
                        this.currentMethod.setResponse(this.currentResponse);
                    }
                    pushState(State.RESPONSE);
                }
            }
        }

        public void startEntity(String name) throws SAXException {
        }
    }

    /** Web Application Description Language namespace. */
    public static final String APP_NAMESPACE = "http://research.sun.com/wadl/2006/10";

    /** The root element of the WADL document. */
    private ApplicationInfo application;

    /**
     * Constructor.
     */
    public WadlRepresentation() {
        super(MediaType.APPLICATION_WADL_XML);
    }

    /**
     * Constructor.
     * 
     * @param application
     *            The root element of the WADL document.
     */
    public WadlRepresentation(ApplicationInfo application) {
        super(MediaType.APPLICATION_WADL_XML);
        this.application = application;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            The XML WADL document.
     * @throws IOException
     */
    public WadlRepresentation(Representation xmlRepresentation)
            throws IOException {
        super(xmlRepresentation);
        this.setMediaType(MediaType.APPLICATION_WADL_XML);

        // Parse the given document using SAX to produce an ApplicationInfo
        // instance.
        parse(new ContentReader(this));
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        return null;
    }

    /**
     * Returns the root element of the WADL document.
     * 
     * @return The root element of the WADL document.
     */
    public ApplicationInfo getApplication() {
        return application;
    }

    /**
     * Returns an HTML representation.
     * 
     * @return An HTML representation.
     */
    public Representation getHtmlRepresentation() {
        Representation representation = null;
        URL wadlHtmlXsltUrl = Engine.getClassLoader().getResource(
                "org/restlet/ext/wadl/htmlConvert.xsl");
        if (wadlHtmlXsltUrl != null) {
            try {
                setSaxSource(new SAXSource(new InputSource(this.getStream())));
                InputRepresentation xslRep = new InputRepresentation(
                        wadlHtmlXsltUrl.openStream(),
                        MediaType.APPLICATION_W3C_XSLT);
                representation = new TransformRepresentation(this, xslRep);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return representation;
    }

    /**
     * Sets the root element of the WADL document.
     * 
     * @param application
     *            The root element of the WADL document.
     */
    public void setApplication(ApplicationInfo application) {
        this.application = application;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        // Convert the attached ApplicationInfo instance into an equivalent WADL
        // XML document.
        XmlWriter writer = new XmlWriter(outputStream, "UTF-8");
        try {
            writer.forceNSDecl(APP_NAMESPACE, "");
            writer.setDataFormat(true);
            writer.setIndentStep(3);
            writer.startDocument();
            this.application.writeElement(writer);
            writer.endDocument();
        } catch (SAXException e) {
            logger.log(Level.SEVERE,
                    "Error when writing the WADL Representation.", e);
        }
    }
}
