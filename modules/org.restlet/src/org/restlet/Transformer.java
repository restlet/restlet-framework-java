/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.TransformRepresentation;

/**
 * Filter that can transform XML representations by applying an XSLT transform
 * sheet. It uses the {@link org.restlet.resource.TransformRepresentation} to
 * actually transform the XML entities.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Transformer extends Filter {
    /**
     * Mode that transforms request entities before their handling by the
     * attached Restlet.
     */
    public static final int MODE_REQUEST = 1;

    /**
     * Mode that transforms response entities after their handling by the
     * attached Restlet.
     */
    public static final int MODE_RESPONSE = 2;

    /** The transformation mode. */
    private volatile int mode;

    /**
     * The character set of the result representation. The default value is
     * null.
     */
    private volatile CharacterSet resultCharacterSet;

    /**
     * The encodings of the result representation.
     */
    private volatile List<Encoding> resultEncodings;

    /** The languages of the result representation. */
    private volatile List<Language> resultLanguages;

    /**
     * The media type of the result representation. MediaType.APPLICATION_XML by
     * default.
     */
    private volatile MediaType resultMediaType;

    /** The XSLT transform sheet to apply to message entities. */
    private volatile Representation transformSheet;

    /**
     * Constructor.
     * 
     * @param mode
     *            The transformation mode.
     * @param transformSheet
     *            The XSLT transform sheet to apply to message entities.
     */
    public Transformer(int mode, Representation transformSheet) {
        this.mode = mode;
        this.transformSheet = transformSheet;
        this.resultMediaType = MediaType.APPLICATION_XML;
        this.resultCharacterSet = null;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (getMode() == MODE_RESPONSE) {
            response.setEntity(transform(response.getEntity()));
        }
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (getMode() == MODE_REQUEST) {
            request.setEntity(transform(request.getEntity()));
        }

        return CONTINUE;
    }

    /**
     * Returns the transformation mode. See MODE_* constants.
     * 
     * @return The transformation mode.
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * Returns the character set of the result representation. The default value
     * is null.
     * 
     * @return The character set of the result representation.
     */
    public CharacterSet getResultCharacterSet() {
        return this.resultCharacterSet;
    }

    /**
     * Returns the modifiable list of encodings of the result representation.
     * 
     * @return The encoding of the result representation.
     */
    public List<Encoding> getResultEncodings() {
        // Lazy initialization with double-check.
        List<Encoding> re = this.resultEncodings;
        if (re == null) {
            synchronized (this) {
                re = this.resultEncodings;
                if (re == null) {
                    this.resultEncodings = re = new CopyOnWriteArrayList<Encoding>();
                }
            }
        }
        return re;
    }

    /**
     * Returns the modifiable list of languages of the result representation.
     * 
     * @return The language of the result representation.
     */
    public List<Language> getResultLanguages() {
        // Lazy initialization with double-check.
        List<Language> v = this.resultLanguages;
        if (v == null) {
            synchronized (this) {
                v = this.resultLanguages;
                if (v == null) {
                    this.resultLanguages = v = new CopyOnWriteArrayList<Language>();
                }
            }
        }
        return v;
    }

    /**
     * Returns the media type of the result representation. The default value is
     * MediaType.APPLICATION_XML.
     * 
     * @return The media type of the result representation.
     */
    public MediaType getResultMediaType() {
        return this.resultMediaType;
    }

    /**
     * Returns the XSLT transform sheet to apply to message entities.
     * 
     * @return The XSLT transform sheet to apply to message entities.
     */
    public Representation getTransformSheet() {
        return this.transformSheet;
    }

    /**
     * Sets the transformation mode. See MODE_* constants.
     * 
     * @param mode
     *            The transformation mode.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Sets the character set of the result representation.
     * 
     * @param resultCharacterSet
     *            The character set of the result representation.
     */
    public void setResultCharacterSet(CharacterSet resultCharacterSet) {
        this.resultCharacterSet = resultCharacterSet;
    }

    /**
     * Sets the encodings of the result representation.
     * 
     * @param resultEncodings
     *            The encodings of the result representation.
     */
    public void setResultEncodings(List<Encoding> resultEncodings) {
        this.resultEncodings = resultEncodings;
    }

    /**
     * Sets the languages of the result representation.
     * 
     * @param resultLanguages
     *            The languages of the result representation.
     */
    public void setResultLanguages(List<Language> resultLanguages) {
        this.resultLanguages = resultLanguages;
    }

    /**
     * Sets the media type of the result representation.
     * 
     * @param resultMediaType
     *            The media type of the result representation.
     */
    public void setResultMediaType(MediaType resultMediaType) {
        this.resultMediaType = resultMediaType;
    }

    /**
     * Sets the XSLT transform sheet to apply to message entities.
     * 
     * @param transformSheet
     *            The XSLT transform sheet to apply to message entities.
     */
    public void setTransformSheet(Representation transformSheet) {
        this.transformSheet = transformSheet;
    }

    /**
     * Transforms a source XML representation by applying an XSLT transform
     * sheet to it.
     * 
     * @param source
     *            The source XML representation.
     * @return The generated result representation.
     */
    public Representation transform(Representation source) {
        final Representation result = new TransformRepresentation(getContext(),
                source, getTransformSheet());

        if (this.resultLanguages != null) {
            result.getLanguages().addAll(getResultLanguages());
        }

        result.setCharacterSet(getResultCharacterSet());
        if (this.resultEncodings != null) {
            result.getEncodings().addAll(getResultEncodings());
        }

        result.setMediaType(getResultMediaType());
        return result;
    }

}
