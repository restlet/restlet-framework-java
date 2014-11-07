package org.restlet.ext.apispark.internal.agent;

/**
 * @author Manuel Boillod
 */
public class AgentConfigurationException extends AgentException {

    public AgentConfigurationException() {
    }

    public AgentConfigurationException(String message) {
        super(message);
    }

    public AgentConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AgentConfigurationException(Throwable cause) {
        super(cause);
    }
}
