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
public interface DocumentedResource {

    /**
     * Name of the resource. When generating Restlet Framework client SDKs on <a
     * href="https://apispark.com">APISpark</a>, the annotated interface will be
     * named [name]Resource and the ClientResource [name]ClientResource.
     *
     * Can be overriden.
     */
    String getName();

    /**
     * Description of the resource.
     *
     * @return The description of the resource
     */
    String getDescription();

    /**
     * Name of a section of your Web API. Used to tidy it in documentation.
     * //todo section by method ??
     */
    String getSection();
}
