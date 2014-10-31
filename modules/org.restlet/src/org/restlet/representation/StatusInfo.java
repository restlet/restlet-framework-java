package org.restlet.representation;

import org.restlet.data.Status;

/**
 *
 * Representation of a {@link Status}.
 *
 * @author Manuel Boillod
 */
public class StatusInfo {

    /** The specification code. */
    int code;

    /** The longer description. */
    String description;

    /** The short reason phrase. */
    String reasonPhrase;

    /**
     *  Constructor
     */
    public StatusInfo() {
    }
    /**
     * Constructor
     * 
     * @param code
     *            The specification code.
     * @param description
     *            The longer description.
     * @param reasonPhrase
     *            The short reason phrase.
     */
    public StatusInfo(int code, String description,
                                String reasonPhrase) {
        super();
        this.code = code;
        this.description = description;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Constructor.
     *
     * @param status
     *            The represented status.
     */
    public StatusInfo(Status status) {
        this.code = status.getCode();
        this.reasonPhrase = status.getReasonPhrase();
        this.description = status.getDescription();

    }

    /**
     * Returns the code of the status.
     *
     * @return The code of the status.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the description of the status.
     *
     * @return The description of the status.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the short description of the status.
     *
     * @return The short description of the status.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Sets the code of the status.
     *
     * @param code
     *            The code of the status.
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Sets the description of the status.
     *
     * @param code
     *            The description of the status.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the short description of the status.
     *
     * @param code
     *            The short description of the status.
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }
}