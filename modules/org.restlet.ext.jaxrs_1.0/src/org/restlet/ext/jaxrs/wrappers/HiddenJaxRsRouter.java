package org.restlet.ext.jaxrs.wrappers;

import java.util.logging.Logger;

import org.restlet.ext.jaxrs.AccessControl;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This methods are used to get attributes from the {@link JaxRsRouter}. This
 * interface is implemented to decouple the wrapper classes (see package
 * {@link org.restlet.ext.jaxrs.wrappers}) from the JaxRsRouter.
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
     * @return the {@link WrapperFactory} for this JaxRsRouter.
     */
    public WrapperFactory getWrapperFactory();
}