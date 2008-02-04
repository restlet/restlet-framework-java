/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.ext.jaxrs.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.util.Util;

/**
 * The implementation of the JAX-RS interface {@link PathSegment}
 * 
 * @author Stephan Koops
 * 
 */
public class JaxRsPathSegment implements PathSegment {

    /**
     * @param matrParamString
     *                The string to parse the matrix parameters
     * @param decode
     *                if true, than the keys and values are decoded, if false,
     *                than not.
     * @param encodeAndCheckWhenNotDecode
     *                If decode is false and encodeAndCheckWhenNotDecode is
     *                true, than an IllegalArgumentException is thrown, when a
     *                parameter contains illegal characters, if false nothing
     *                will checked. If decode is true, this value is ignored.
     * @return
     * 
     * Method is public for testing, otherwise it would be package visible.
     */
    public static MultivaluedMapImpl<String, String> parseMatrixParams(
            String matrParamString, boolean decode,
            boolean encodeAndCheckWhenNotDecode) {
        MultivaluedMapImpl<String, String> matrixParameters = new MultivaluedMapImpl<String, String>();
        if (matrParamString != null) {
            String[] paramsEncSpl = matrParamString.split(";");
            for (int i = 0; i < paramsEncSpl.length; i++) {
                String matrParamEnc = paramsEncSpl[i];
                int posEquSign = matrParamEnc.indexOf('=');
                String keyEnc;
                String valueEnc;
                if (posEquSign <= 0) {
                    keyEnc = matrParamEnc;
                    valueEnc = null;
                } else {
                    keyEnc = matrParamEnc.substring(0, posEquSign);
                    valueEnc = matrParamEnc.substring(posEquSign + 1);
                }
                if (keyEnc.length() == 0 && valueEnc == null)
                    continue;
                String key;
                String value;
                if (decode) {
                    key = Reference.decode(keyEnc);
                    value = Reference.decode(valueEnc);
                } else if (encodeAndCheckWhenNotDecode) {
                    key = Util.encode(keyEnc, i, " matrix parameter key", true,
                            true);
                    value = Util.encode(valueEnc, i, " matrix parameter value",
                            true, true);
                } else {

                    key = keyEnc;
                    value = valueEnc;
                }
                matrixParameters.add(key, value);
            }
        }
        return matrixParameters;
    }

    private boolean decode;

    private boolean encode;

    /**
     * encoded or decoded, depends on {@link #decode} and {@link #encode}.
     */
    private MultivaluedMap<String, String> matrixParameters;

    /**
     * the matrix parameters, as given in constructor. If the parameters are
     * parsed ({@link #getMatrixParameters()}, this instance variable will be
     * set to null.
     */
    private String matrParamEncoded;

    /** encoded or decoded, depends on {@link #decode} and {@link #encode} */
    private String path;

    private boolean unmodifiable;

    /**
     * @param segment
     *                Segment with matrix parameter.
     * @param unmodifiable
     *                indicates if this instance is modifiable or not
     * @param decode
     *                true, if the path and the marix parameters should be
     *                decoded.
     * @param encode
     *                true, if the path and the marix parameters should be
     *                encoded. Braces are not encoded. (they are used for
     *                variables.)
     * @param checkForInvalidChars
     *                if true, than the path is checked for invalid chars, if
     *                decode and encode is both false.
     * @param indexForErrMess
     *                If the user adds more than one path segment with one call,
     *                you can give the index for an error message here. Set -1,
     *                if none. See
     *                {@link Util#checkForInvalidUriChars(String, int, String)}
     * @throws IllegalArgumentException
     *                 if decode and encode is both true
     */
    public JaxRsPathSegment(String segment, boolean unmodifiable,
            boolean decode, boolean encode, boolean checkForInvalidChars,
            int indexForErrMess) throws IllegalArgumentException {
        if (decode && encode)
            throw new IllegalArgumentException(
                    "It is not meaningful to require decode AND encode");
        if (segment == null) {
            if (indexForErrMess >= 0)
                throw new IllegalArgumentException("The " + indexForErrMess
                        + ". segment must not be null");
            else
                throw new IllegalArgumentException(
                        "The segment must not be null");
        }
        this.unmodifiable = unmodifiable;
        this.decode = decode;
        this.encode = encode;
        int indexOfSemic = segment.indexOf(';');
        String path;
        if (indexOfSemic > 0) {
            path = segment.substring(0, indexOfSemic);
            this.matrParamEncoded = segment.substring(indexOfSemic + 1);
        } else {
            path = segment;
            this.matrParamEncoded = null;
        }
        if (decode) {
            this.path = Reference.decode(path);
        } else if (encode) {
            this.path = Util.encodeNotBraces(path, true);
        } else {
            if (checkForInvalidChars)
                Util.checkForInvalidUriChars(path, indexForErrMess,
                        "new path segment");
            this.path = path;
        }
    }

    /**
     * Creates a new PathSegment. An object created with this constructor will
     * not decode anything.
     * 
     * @param path
     *                The segment path. If decoding is necessary, it must be
     *                decoded externally.
     * @param unmodifiable
     *                indicates if this instance is modifiable or not
     * @param matrixParameters
     *                The matrix parameters of this segment. If decoding is
     *                necessary, it must be decoded externally. If it is null,
     *                it will created on first invoke of
     *                {@link #getMatrixParameters()}
     * @see #clone()
     */
    public JaxRsPathSegment(String path, boolean unmodifiable,
            MultivaluedMap<String, String> matrixParameters) {
        this.path = path;
        this.unmodifiable = unmodifiable;
        if(unmodifiable && !(matrixParameters instanceof UnmodifiableMultivaluedMap))
            this.matrixParameters = new UnmodifiableMultivaluedMap<String, String>(matrixParameters, false);
        this.matrixParameters = matrixParameters;
        this.matrParamEncoded = null;
        // the other instance variables will not be used, so forget them
    }

    @Override
    public JaxRsPathSegment clone() {
        MultivaluedMapImpl<String, String> clonedMatrParams = null;
        if (this.matrixParameters != null
                && !(this.matrixParameters instanceof UnmodifiableMultivaluedMap)) {
            if (this.matrixParameters instanceof MultivaluedMapImpl)
                clonedMatrParams = ((MultivaluedMapImpl<String, String>) this.matrixParameters)
                        .clone();
        }
        return new JaxRsPathSegment(path, unmodifiable, clonedMatrParams);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof JaxRsPathSegment))
            return false;
        PathSegment other = (PathSegment) object;
        if (!this.getPath().equals(other.getPath()))
            return false;
        if (!this.getMatrixParameters().equals(other.getMatrixParameters()))
            return false;
        return true;
    }

    /**
     * Get a map of the decoded (or encoded?) matrix parameters associated with
     * the path segment
     * 
     * @return the map of matrix parameters. Never returns null.
     */
    public MultivaluedMap<String, String> getMatrixParameters() {
        if (this.matrixParameters == null) {
            this.matrixParameters = parseMatrixParams(matrParamEncoded, decode,
                    encode);
            this.matrParamEncoded = null;
        }
        return matrixParameters;
    }

    /**
     * @return the path segment
     * @see PathSegment#getPath()
     */
    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return this.path.hashCode() ^ this.getMatrixParameters().hashCode();
    }

    /**
     * Sets the matrix parameters.
     * 
     * @param matrixParams
     *                new matrix parameters
     */
    public void setMatrixParameters(MultivaluedMap<String, String> matrixParams) {
        if(unmodifiable)
            throw new IllegalStateException("This instance is not modifiable");
        this.matrixParameters = matrixParams;
    }

    /**
     * Appends this PathSegment to the given Appendable
     * 
     * @param stb
     *                StringBuilder or other Appendable to append this
     *                PathSegment.
     * @param convertBraces
     *                if true, all braces are converted, if false then not, see
     *                {@link Util#append(Appendable, CharSequence, boolean)}.
     * @throws IOException
     *                 if the Appendable has a problem.
     */
    public void toAppendable(Appendable stb, boolean convertBraces)
            throws IOException {
        Util.append(stb, this.path, convertBraces);
        MultivaluedMap<String, String> matrixParams = getMatrixParameters();
        for (Map.Entry<String, List<String>> mpe : matrixParams.entrySet()) {
            for (String value : mpe.getValue()) {
                stb.append(';');
                Util.append(stb, mpe.getKey(), convertBraces);
                stb.append('=');
                Util.append(stb, value, convertBraces);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        toStringBuilder(stb, false);
        return stb.toString();
    }

    /**
     * Appends this PathSegment to the given StringBuilder
     * 
     * @param stb
     *                the StrinBuilder to append
     * @param convertBraces
     *                if true, than conatined braces will be encoded, see
     *                {@link Util#append(Appendable, CharSequence, boolean)}.
     */
    public void toStringBuilder(StringBuilder stb, boolean convertBraces) {
        try {
            toAppendable(stb, convertBraces);
        } catch (IOException e) {
            throw new RuntimeException(
                    "IOException in StringBuilder; that is normally not possible");
        }
    }
}