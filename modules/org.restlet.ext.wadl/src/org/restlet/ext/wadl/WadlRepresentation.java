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

package org.restlet.ext.wadl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.SaxRepresentation;
import org.restlet.representation.TransformRepresentation;
import org.restlet.util.XmlWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Root of a WADL description document.
 * 
 * @author Jerome Louvel
 */
public class WadlRepresentation extends SaxRepresentation {

    // -------------------
    // Content reader part
    // -------------------
    private static class ContentReader extends DefaultHandler implements
            LexicalHandler {
        public enum MixedContentState {
            CDATA, COMMENT, ELEMENT, ENTITY, NONE, TEXT
        }

        public enum State {
            APPLICATION, DOCUMENTATION, FAULT, GRAMMARS, INCLUDE, LINK, METHOD, NONE, OPTION, PARAMETER, REPRESENTATION, REQUEST, RESOURCE, RESOURCES, RESOURCETYPE, RESPONSE
        }

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

        /** The current mixed content CDataSection. */
        private CDATASection currentMixedContentCDataSection;

        /** The current mixed content node. */
        private Node currentMixedContentNode;

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
        private final List<ResourceInfo> currentResourcesList;

        /** The current parsed "resource_type" tag. */
        private ResourceTypeInfo currentResourceType;

        /** The current parsed "response" tag. */
        private ResponseInfo currentResponse;

        /** The document used to create the mixed content. */
        private Document mixedContentDocument;

        /** The stack of mixed content parser states. */
        private final List<MixedContentState> mixedContentStates;

        /** The top node of mixed content nodes. */
        private Node mixedContentTopNode;

        /**
         * Map of namespaces used in the WADL document. The key is the URI of
         * the namespace and the value, the prefix.
         */
        private Map<String, String> namespaces;

        /** The stack of parser states. */
        private final List<State> states;

        /** The WadlRepresentation instance that represents the parsed document. */
        private final WadlRepresentation wadlRepresentation;

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
            this.mixedContentStates = new ArrayList<MixedContentState>();
            this.mixedContentStates.add(MixedContentState.NONE);
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
            try {
                this.mixedContentDocument = new DomRepresentation(
                        MediaType.TEXT_XML).getDocument();
            } catch (IOException e) {
            }
            this.currentMixedContentCDataSection = null;
            this.namespaces = new HashMap<String, String>();
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
            if (getState() == State.DOCUMENTATION) {
                if (getMixedContentState() == MixedContentState.CDATA) {
                    this.currentMixedContentCDataSection.appendData(new String(
                            ch, start, length));
                } else if (getMixedContentState() != MixedContentState.ENTITY) {
                    this.currentMixedContentNode
                            .appendChild(this.mixedContentDocument
                                    .createTextNode(new String(ch, start,
                                            length)));
                }
            }
        }

        /**
         * Receive notification of a comment section.
         * 
         * @param ch
         *            The characters from the XML document.
         * @param start
         *            The start position in the array.
         * @param length
         *            The number of characters to read from the array.
         */
        public void comment(char[] ch, int start, int length)
                throws SAXException {
            if (getState() == State.DOCUMENTATION) {
                this.currentMixedContentNode
                        .appendChild(this.mixedContentDocument
                                .createComment(new String(ch, start, length)));
            }
        }

        /**
         * Receive notification of the end of a CDATA sectionn.
         */
        public void endCDATA() throws SAXException {
            if (getState() == State.DOCUMENTATION) {
                popMixedContentState();
            }
        }

        /**
         * Receive notification of the end of a document.
         */
        @Override
        public void endDocument() throws SAXException {
            popState();
            if (this.namespaces != null && !this.namespaces.isEmpty()
                    && this.currentApplication != null) {
                this.currentApplication.setNamespaces(namespaces);
            }
            this.wadlRepresentation.setApplication(this.currentApplication);
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
                if (localName.equals("application")) {
                    popState();
                } else if (localName.equals("doc")) {
                    this.currentDocumentation
                            .setMixedContent(mixedContentTopNode);
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
            } else {
                if (getState() == State.DOCUMENTATION) {
                    popMixedContentState();
                    this.currentMixedContentNode = this.currentMixedContentNode
                            .getParentNode();
                }
            }
        }

        /**
         * Receive notification of the end of an entity section.
         * 
         * @param name
         *            The name of the entity.
         */
        public void endEntity(String name) throws SAXException {
            popMixedContentState();
        }

        /**
         * Returns the current state when processing mixed content sections.
         * 
         * @return The current state when processing mixed content sections.
         */
        private MixedContentState getMixedContentState() {
            final MixedContentState result = this.mixedContentStates.get(0);
            return result;
        }

        /**
         * Returns a parameterStyle value according to the given string or null.
         * 
         * @param parameterStyle
         *            The given string.
         * @return The parameterStyle value that corresponds to the given style
         *         name, or null otherwise.
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
         * Returns the current state when processing the WADL document.
         * 
         * @return the current state when processing the WADL document.
         */
        private State getState() {
            final State result = this.states.get(0);
            return result;
        }

        /**
         * Drops the current state from the stack and returns it. This state
         * becomes the former current state.
         * 
         * @return the former current state.
         */
        private MixedContentState popMixedContentState() {
            return this.mixedContentStates.remove(0);
        }

        /**
         * Drops the current state from the stack and returns it. This state
         * becomes the former current state.
         * 
         * @return the former current state.
         */
        private State popState() {
            return this.states.remove(0);
        }

        /**
         * Adds the given state.
         * 
         * @param state
         *            The given state.
         */
        private void pushMixedContentState(MixedContentState state) {
            this.mixedContentStates.add(0, state);
        }

        /**
         * Adds the given state.
         * 
         * @param state
         *            The given state.
         */
        private void pushState(State state) {
            this.states.add(0, state);
        }

        /**
         * Receive notification of the beginning of a CDATA section.
         */
        public void startCDATA() throws SAXException {
            if (getState() == State.DOCUMENTATION) {
                pushMixedContentState(MixedContentState.CDATA);
                this.currentMixedContentCDataSection = this.mixedContentDocument
                        .createCDATASection("");
                this.currentMixedContentNode
                        .appendChild(this.currentMixedContentCDataSection);
            }
        }

        /**
         * Receive notification of the beginning of a document.
         */
        @Override
        public void startDocument() throws SAXException {
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

            if (uri.equalsIgnoreCase(APP_NAMESPACE)) {
                if (localName.equals("application")) {
                    this.currentApplication = new ApplicationInfo();
                    pushState(State.APPLICATION);
                } else if (localName.equals("doc")) {
                    this.currentDocumentation = new DocumentationInfo();
                    this.mixedContentTopNode = this.mixedContentDocument
                            .createDocumentFragment();
                    this.currentMixedContentNode = mixedContentTopNode;
                    this.currentMixedContentCDataSection = null;

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
                    this.currentFault = new FaultInfo(null);

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
                        final String[] profiles = attrs.getValue("profile")
                                .split(" ");
                        for (final String string : profiles) {
                            this.currentFault.getProfiles().add(
                                    new Reference(string));
                        }
                    }
                    if (attrs.getIndex("status") != -1) {
                        final String[] statuses = attrs.getValue("status")
                                .split(" ");
                        for (final String string : statuses) {
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
                        final String[] profiles = attrs.getValue("profile")
                                .split(" ");
                        for (final String string : profiles) {
                            this.currentRepresentation.getProfiles().add(
                                    new Reference(string));
                        }
                    }
                    if (attrs.getIndex("status") != -1) {
                        final String[] statuses = attrs.getValue("status")
                                .split(" ");
                        for (final String string : statuses) {
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
                    final ResourceInfo resourceInfo = new ResourceInfo();
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
                        final String[] type = attrs.getValue("type").split(" ");
                        for (final String string : type) {
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
            } else {
                if (getState() == State.DOCUMENTATION) {
                    // We are handling a new element
                    pushMixedContentState(MixedContentState.ELEMENT);
                    Node node = null;
                    if (("".equals(qName) || qName == null)) {
                        node = this.mixedContentDocument.createElementNS(uri,
                                localName);
                    } else {
                        node = this.mixedContentDocument.createElementNS(uri,
                                qName);
                    }

                    for (int i = 0; i < attrs.getLength(); i++) {
                        Attr attr = this.mixedContentDocument
                                .createAttributeNS(attrs.getURI(i), attrs
                                        .getLocalName(i));
                        attr.setNodeValue(attrs.getValue(i));
                        node.getAttributes().setNamedItemNS(attr);
                    }
                    // This element becomes the current one and is added to its
                    // parent.
                    this.currentMixedContentNode.appendChild(node);
                    this.currentMixedContentNode = node;
                }
            }
        }

        /**
         * Receive notification of the beginning of an entity.
         * 
         * @param name
         *            The name of the entity.
         */
        public void startEntity(String name) throws SAXException {
            pushMixedContentState(MixedContentState.ENTITY);
            this.currentMixedContentNode.appendChild(mixedContentDocument
                    .createEntityReference(name));
        }

        /**
         * Receive notification of the beginning of a prefix-URI Namespace
         * mapping.
         * 
         * @param name
         *            The name of the entity.
         */
        @Override
        public void startPrefixMapping(String arg0, String arg1)
                throws SAXException {
            this.namespaces.put(arg1, arg0);
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
        super(MediaType.APPLICATION_WADL);
    }

    /**
     * Constructor.
     * 
     * @param application
     *            The root element of the WADL document.
     */
    public WadlRepresentation(ApplicationInfo application) {
        super(MediaType.APPLICATION_WADL);
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
        setMediaType(MediaType.APPLICATION_WADL);

        // Parse the given document using SAX to produce an ApplicationInfo
        // instance.
        parse(new ContentReader(this));
    }

    /**
     * Constructor. The title of the resource, that is to say the title of its
     * first documentation tag is transfered to the title of the first
     * documentation tag of the main application tag.
     * 
     * @param resource
     *            The root element of the WADL document.
     */
    public WadlRepresentation(ResourceInfo resource) {
        super(MediaType.APPLICATION_WADL);

        this.application = new ApplicationInfo();
        if (!resource.getDocumentations().isEmpty()) {
            String titleResource = resource.getDocumentations().get(0)
                    .getTitle();
            if (titleResource != null && !"".equals(titleResource)) {
                DocumentationInfo doc = null;
                if (application.getDocumentations().isEmpty()) {
                    doc = new DocumentationInfo();
                    application.getDocumentations().add(doc);
                } else {
                    doc = application.getDocumentations().get(0);
                }
                doc.setTitle(titleResource);
            }
        }

        final ResourcesInfo resources = new ResourcesInfo();
        this.application.setResources(resources);
        resources.getResources().add(resource);
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
        final URL wadlHtmlXsltUrl = Engine.getClassLoader().getResource(
                "org/restlet/ext/wadl/htmlConvert.xsl");

        if (wadlHtmlXsltUrl != null) {
            try {
                final InputRepresentation xslRep = new InputRepresentation(
                        wadlHtmlXsltUrl.openStream(),
                        MediaType.APPLICATION_W3C_XSLT);
                representation = new TransformRepresentation(Context
                        .getCurrent(), this, xslRep);
                representation.setMediaType(MediaType.TEXT_HTML);
            } catch (IOException e) {
                Context.getCurrent().getLogger().log(Level.WARNING,
                        "Unable to generate the WADL HTML representation", e);
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
    public void write(XmlWriter writer) throws IOException {
        try {
            writer.forceNSDecl(APP_NAMESPACE, "");
            writer.setDataFormat(true);
            writer.setIndentStep(3);
            writer.processingInstruction("xml",
                    "version=\"1.0\" standalone=\"yes\"");
            writer.processingInstruction("xml-stylesheet",
                    "type=\"text/xsl\" href=\"wadl_documentation.xsl\"");
            this.application.writeElement(writer);
            writer.endDocument();
        } catch (SAXException e) {
            Context.getCurrentLogger().log(Level.SEVERE,
                    "Error when writing the WADL Representation.", e);
        }
    }
}
