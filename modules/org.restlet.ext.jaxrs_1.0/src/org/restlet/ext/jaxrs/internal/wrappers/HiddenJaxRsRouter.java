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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.util.logging.Logger;

import org.restlet.ext.jaxrs.AccessControl;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This methods are used to get attributes from the {@link JaxRsRouter}. This
 * interface is implemented to decouple the wrapper classes (see package
 * {@link org.restlet.ext.jaxrs.internal.wrappers}) from the JaxRsRouter.
 * 
 * @author Stephan Koops
 */
public interface HiddenJaxRsRouter {

    /**
     * Get the {@link AccessControl} from the {@link JaxRsRouter}.
     * 
     * @return the {@link AccessControl} from the {@link JaxRsRouter}.
     */
    public AccessControl getAccessControl();

    /**
     * Get the {@link Logger} from the {@link JaxRsRouter}.
     * 
     * @return the {@link Logger} from the {@link JaxRsRouter}.
     */
    public Logger getLogger();

    /**
     * Get the {@link MessageBodyReaderSet} from the {@link JaxRsRouter}.
     * 
     * @return the {@link MessageBodyReaderSet} from the {@link JaxRsRouter}.
     */
    public MessageBodyReaderSet getMessageBodyReaders();

    /**
     * Get the {@link MessageBodyWriterSet} from the {@link JaxRsRouter}.
     * 
     * @return the {@link MessageBodyWriterSet} from the {@link JaxRsRouter}.
     */
    public MessageBodyWriterSet getMessageBodyWriters();

    /**
     * Returns the {@link WrapperFactory} for this JaxRsRouter.
     * 
     * @return the {@link WrapperFactory} for this JaxRsRouter.
     */
    public WrapperFactory getWrapperFactory();
}