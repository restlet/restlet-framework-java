/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

/** An immutable Server-Sent Event. */
public final class ServerSentEvent {

    private final String id;

    private final String event;

    private final String data;

    private final Long retry;

    private final String comment;

    private ServerSentEvent(Builder builder) {
        this.id = builder.id;
        this.event = builder.event;
        this.data = builder.data;
        this.retry = builder.retry;
        this.comment = builder.comment;
    }

    /** Returns a builder for an event. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the event ID, or {@code null} when absent. */
    public String getId() {
        return id;
    }

    /** Returns the event type, or {@code null} when absent. */
    public String getEvent() {
        return event;
    }

    /** Returns the event data, or {@code null} when absent. */
    public String getData() {
        return data;
    }

    /** Returns the reconnection delay in milliseconds, or {@code null} when absent. */
    public Long getRetry() {
        return retry;
    }

    /** Returns the comment, or {@code null} when absent. */
    public String getComment() {
        return comment;
    }

    /** Builds an immutable {@link ServerSentEvent}. */
    public static final class Builder {

        private String id;

        private String event;

        private String data;

        private Long retry;

        private String comment;

        private Builder() {}

        /**
         * Sets the event ID.
         *
         * @param id The ID; it must not contain a NUL, carriage return or line feed.
         * @return This builder.
         */
        public Builder id(String id) {
            if (id != null
                    && (id.indexOf('\0') >= 0 || id.indexOf('\r') >= 0 || id.indexOf('\n') >= 0)) {
                throw new IllegalArgumentException("An SSE id cannot contain NUL or a line break");
            }
            this.id = id;
            return this;
        }

        /**
         * Sets the event type.
         *
         * @param event The event type; it must not contain a line break.
         * @return This builder.
         */
        public Builder event(String event) {
            rejectLineBreak(event, "event");
            this.event = event;
            return this;
        }

        /** Sets the event data. Line breaks are encoded as multiple {@code data:} fields. */
        public Builder data(String data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the reconnection delay.
         *
         * @param retry The delay in milliseconds; must be non-negative.
         * @return This builder.
         */
        public Builder retry(Long retry) {
            if (retry != null && retry < 0) {
                throw new IllegalArgumentException("An SSE retry delay cannot be negative");
            }
            this.retry = retry;
            return this;
        }

        /** Sets a comment, commonly used as a heartbeat. Line breaks create multiple comments. */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /** Builds the event. At least one field must be set. */
        public ServerSentEvent build() {
            if (id == null && event == null && data == null && retry == null && comment == null) {
                throw new IllegalStateException("An SSE must contain at least one field");
            }
            return new ServerSentEvent(this);
        }

        private static void rejectLineBreak(String value, String field) {
            if (value != null && (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)) {
                throw new IllegalArgumentException(
                        "An SSE " + field + " field cannot contain a line break");
            }
        }
    }
}
