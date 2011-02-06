/**
 * Copyright 2005-2011 Noelios Technologies.
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

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * Describes an error condition for response descriptions.
 * 
 * @author Jerome Louvel
 * @deprecated This element has been removed from the WADL specification.
 */
@Deprecated
public class FaultInfo extends RepresentationInfo {

    /**
     * Constructor.
     * 
     * @param status
     *            The associated status code.
     */
    public FaultInfo(Status status) {
        super();
        getStatuses().add(status);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param status
     *            The associated status code.
     * @param documentation
     *            A single documentation element.
     */
    public FaultInfo(Status status, DocumentationInfo documentation) {
        super(documentation);
        getStatuses().add(status);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param status
     *            The associated status code.
     * @param documentations
     *            The list of documentation elements.
     */
    public FaultInfo(Status status, List<DocumentationInfo> documentations) {
        super(documentations);
        getStatuses().add(status);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param status
     *            The associated status code.
     * @param documentation
     *            A single documentation element.
     */
    public FaultInfo(Status status, String documentation) {
        this(status, new DocumentationInfo(documentation));
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param status
     *            The associated status code.
     * @param mediaType
     *            The fault representation's media type.
     * @param documentation
     *            A single documentation element.
     */
    public FaultInfo(Status status, MediaType mediaType, String documentation) {
        this(status, new DocumentationInfo(documentation));
        setMediaType(mediaType);
    }
}
