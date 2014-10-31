package org.restlet.representation;

import org.restlet.data.Status;

/**
 *
 * Representation of a {@link Status}.
 *
 * @author Manuel Boillod
 */
public class StatusRepresentation {
    int code;
    String reasonPhrase;
    String description;

    public StatusRepresentation() {
    }

    public StatusRepresentation(Status status) {
        this.code = status.getCode();
        this.reasonPhrase = status.getReasonPhrase();
        this.description = status.getDescription();

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}