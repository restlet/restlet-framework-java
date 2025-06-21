/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.restlet.Response;

/**
 * Dimension on which the representations of a resource may vary. Note that when
 * used with HTTP connectors, this class maps to the "Vary" header.
 * 
 * @see Response#getDimensions()
 * @author Jerome Louvel
 * @author Piyush Purang (ppurang@gmail.com)
 */
public enum Dimension {
	AUTHORIZATION, CHARACTER_SET, CLIENT_ADDRESS, CLIENT_AGENT, UNSPECIFIED, ENCODING, LANGUAGE, MEDIA_TYPE, TIME,
	ORIGIN
}
