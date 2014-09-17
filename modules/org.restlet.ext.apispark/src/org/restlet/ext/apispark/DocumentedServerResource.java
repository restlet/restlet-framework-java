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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark;

import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.ext.apispark.internal.info.MethodInfo;
import org.restlet.resource.ServerResource;

/**
 * Describes the ServerResource so that introspection retrieves a more complete
 * description of the Web API.
 * 
 * @author Cyprien Quilici
 * 
 */
public class DocumentedServerResource extends ServerResource {

    /**
     * Name of a section of your Web API. Used to tidy it.
     */
    private String section;

    /**
     * Textual description of the resource, used in documentation.
     */
    private String description;

    /**
     * Name of the resource. When generating Restlet Framework client SDKs on <a
     * href="https://apispark.com">APISpark</a>, the annotated interface will be
     * named [name]Resource and the ClientResource [name]ClientResource.
     */
    private String name;

    /**
     * Method used to document the resource and its operations so that
     * introspection retrieves a more complete description of the API.
     * 
     * To do so, override the method and enrich the content of the MethodInfo
     * object (more information and sample <a href=
     * "http://restlet.com:8444/learn/guide/2.3/extensions/apispark/introspector"
     * >here</a>).
     * 
     * Fields retrieved by Introspector in mi:
     * 
     * mi#name, mi#description, mi#parameters
     * 
     * Fields retrieved by Introspector in mi#parameters:
     * 
     * parameter#name, parameter#description, parameter#defaultValue,
     * parameter#required, parameter#repeating
     * 
     * @param mi
     *            Object representing the operation
     * @param mai
     *            Object containing information about the annotation on the java
     *            method
     */
    public void describe(MethodInfo mi, MethodAnnotationInfo mai) {
        // To be overriden
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
