/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.util.EncodeOrCheck;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * The implementation of the JAX-RS interface {@link PathSegment}
 * 
 * @author Stephan Koops
 */
public class PathSegmentImpl implements PathSegment {

    /**
     * @param matrParamString
     *            The string to parse the matrix parameters
     * @param decoding
     *            if true, than the keys and values are decoded, if false, than
     *            not.
     * @param encodeAndCheckWhenNotDecode
     *            If decode is false and encodeAndCheckWhenNotDecode is true,
     *            than an IllegalArgumentException is thrown, when a parameter
     *            contains illegal characters, if false nothing will checked. If
     *            decode is true, this value is ignored.
     * @return Method is public for testing, otherwise it would be package
     *         visible.
     */
    public static MultivaluedMapImpl<String, String> parseMatrixParams(
            String matrParamString, boolean decoding) {
        final MultivaluedMapImpl<String, String> matrixParameters = new MultivaluedMapImpl<String, String>();
        if (matrParamString == null) {
            return matrixParameters;
        }
        final String[] paramsEncSpl = matrParamString.split(";");
        for (final String matrParamEnc : paramsEncSpl) {
            final int posEquSign = matrParamEnc.indexOf('=');
            String nameEnc;
            String valueEnc;
            if (posEquSign <= 0) {
                nameEnc = matrParamEnc;
                valueEnc = "";
            } else {
                nameEnc = matrParamEnc.substring(0, posEquSign);
                valueEnc = matrParamEnc.substring(posEquSign + 1);
            }
            if ((nameEnc.length() == 0) && (valueEnc == null)) {
                continue;
            }
            String name;
            String value;
            if (decoding) {
                name = Reference.decode(nameEnc);
                value = Reference.decode(valueEnc);
            } else {
                name = nameEnc;
                value = valueEnc;
            }
            matrixParameters.add(name, value);
        }
        return matrixParameters;
    }

    private final boolean decode;

    /**
     * The matrix parameters. Encoded or decoded, depends on {@link #decode}.
     */
    private volatile MultivaluedMap<String, String> matrixParameters;

    /**
     * the encoded matrix parameters, as given in constructor.
     */
    private final String matrParamEncoded;

    /** encoded or decoded, depends on {@link #decode} */
    private final String path;

    /**
     * @param segmentEnc
     *            Segment with matrix parameter. The segment is encoded.
     * @param decode
     *            true, if the path and the marix parameters should be decoded.
     * @param indexForErrMess
     *            If the user adds more than one path segment with one call, you
     *            can give the index for an error message here. Set -1, if none.
     *            See
     *            {@link EncodeOrCheck#checkForInvalidUriChars(String, int, String)}
     * @throws IllegalArgumentException
     *             the segment is null, if decode and encode is both true
     */
    public PathSegmentImpl(String segmentEnc, boolean decode,
            int indexForErrMess) throws IllegalArgumentException {
        if (segmentEnc == null) {
            if (indexForErrMess >= 0) {
                throw new IllegalArgumentException("The " + indexForErrMess
                        + ". segment must not be null");
            }

            throw new IllegalArgumentException("The segment must not be null");
        }
        this.decode = decode;
        final int indexOfSemic = segmentEnc.indexOf(';');
        String path;
        if (indexOfSemic >= 0) {
            path = segmentEnc.substring(0, indexOfSemic);
            this.matrParamEncoded = segmentEnc.substring(indexOfSemic + 1);
        } else {
            path = segmentEnc;
            this.matrParamEncoded = null;
        }
        this.path = decode ? Reference.decode(path) : path;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PathSegmentImpl)) {
            return false;
        }
        final PathSegment other = (PathSegment) object;
        if (!getPath().equals(other.getPath())) {
            return false;
        }
        if (!getMatrixParameters().equals(other.getMatrixParameters())) {
            return false;
        }
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
            this.matrixParameters = parseMatrixParams(this.matrParamEncoded,
                    this.decode);
        }
        return this.matrixParameters;
    }

    /**
     * @return the path segment
     * @see PathSegment#getPath()
     */
    public String getPath() {
        return this.path;
    }

    @Override
    public int hashCode() {
        return this.path.hashCode() ^ getMatrixParameters().hashCode();
    }

    /**
     * Appends this PathSegment to the given Appendable
     * 
     * @param stb
     *            StringBuilder or other Appendable to append this PathSegment.
     * @param convertBraces
     *            if true, all braces are converted, if false then not, see
     *            {@link Util#append(Appendable, CharSequence, boolean)}.
     * @throws IOException
     *             if the Appendable has a problem.
     */
    public void toAppendable(Appendable stb, boolean convertBraces)
            throws IOException {
        Util.append(stb, this.path, convertBraces);
        final MultivaluedMap<String, String> matrixParams = getMatrixParameters();
        for (final Map.Entry<String, List<String>> mpe : matrixParams
                .entrySet()) {
            for (final String value : mpe.getValue()) {
                stb.append(';');
                Util.append(stb, mpe.getKey(), convertBraces);
                stb.append('=');
                Util.append(stb, value, convertBraces);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder stb = new StringBuilder();
        toStringBuilder(stb, false);
        return stb.toString();
    }

    /**
     * Appends this PathSegment to the given StringBuilder
     * 
     * @param stb
     *            the StrinBuilder to append
     * @param convertBraces
     *            if true, than conatined braces will be encoded, see
     *            {@link Util#append(Appendable, CharSequence, boolean)}.
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