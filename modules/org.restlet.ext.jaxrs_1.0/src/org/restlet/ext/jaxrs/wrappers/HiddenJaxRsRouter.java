package org.restlet.ext.jaxrs.wrappers;

import java.util.logging.Logger;

import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This methods are used to get attributes from the {@link JaxRsRouter}. This
 * interface is implemented to decouple from the JaxRsRouter.
 * 
 * @author Stephan Koops
 */
public interface HiddenJaxRsRouter {

    /**
     * Get the {@link Authenticator} from the {@link JaxRsRouter}.
     * @return the {@link Authenticator} from the {@link JaxRsRouter}.
     */
    public Authenticator getAuthenticator();

    /**
     * Get the {@link Logger} from the {@link JaxRsRouter}.
     * @return the {@link Logger} from the {@link JaxRsRouter}.
     */
    public Logger getLogger();

    /**
     * Get the {@link MessageBodyReaderSet} from the {@link JaxRsRouter}.
     * @return the {@link MessageBodyReaderSet} from the {@link JaxRsRouter}.
     */
    public MessageBodyReaderSet getMessageBodyReaders();

    /**
     * Get the {@link MessageBodyWriterSet} from the {@link JaxRsRouter}.
     * @return the {@link MessageBodyWriterSet} from the {@link JaxRsRouter}.
     */
    public MessageBodyWriterSet getMessageBodyWriters();
}
