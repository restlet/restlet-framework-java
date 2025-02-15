/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.representation;

import org.restlet.client.data.CharacterSet;
import org.restlet.client.data.MediaType;

/**
 * Representation based on a BIO character stream.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public abstract class CharacterRepresentation extends Representation {
    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public CharacterRepresentation(MediaType mediaType) {
        super(mediaType);
        setCharacterSet(CharacterSet.UTF_8);
    }





}
