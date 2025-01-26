/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.util.emul;

import java.io.IOException;

/**
 * Emulation of the {@link java.io.UnsupportedEncodingException} class for the
 * GWT edition.
 * 
 * @author Thierry Boileau
 */
@SuppressWarnings("serial")
public class UnsupportedEncodingException extends IOException {

    public UnsupportedEncodingException() {
        super();
    }

    public UnsupportedEncodingException(String message) {
        super(message);
    }

}
