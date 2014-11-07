package org.restlet.ext.apispark.internal.agent;

/**
 * @author Manuel Boillod
 */
public class AgentException extends RuntimeException {

    public AgentException() {
    }

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }

    public AgentException(Throwable cause) {
        super(cause);
    }
}
