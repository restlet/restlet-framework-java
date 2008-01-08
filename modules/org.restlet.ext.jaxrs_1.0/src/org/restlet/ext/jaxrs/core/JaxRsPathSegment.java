/*
 * Copyright 2005-2007 Noelios Consulting.
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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.restlet.data.Reference;

/**
 * The implementation of the JAX-RS interface {@link PathSegment}
 * 
 * @author Stephan Koops
 * 
 */
public class JaxRsPathSegment implements PathSegment {
    /** encoded or decoded, depends on {@link #decode} */
    private String path;

    /** encoded */
    private String matrParamEncoded;

    private boolean decode;

    /** encoded or decoded, depends on {@link #decode} */
    private MultivaluedMap<String, String> matrixParameters;

    /**
     * @param segmentEncoded
     *                Segment with matrix parameter.
     * @param decode
     *                true, if the path and the marix parameters should be
     *                decoded.
     */
    public JaxRsPathSegment(String segmentEncoded, boolean decode) {
        this.decode = decode;
        int indexOfSemic = segmentEncoded.indexOf(';');
        String path;
        if(indexOfSemic > 0)
        {
            path = segmentEncoded.substring(0, indexOfSemic);
            this.matrParamEncoded = segmentEncoded.substring(indexOfSemic + 1);
        }
        else
        {
            path = segmentEncoded;
            this.matrParamEncoded = null;
        }
        if (decode)
            this.path = Reference.decode(path);
        else
            this.path = path;
    }

    /**
     * Get a map of the decoded (or encoded?) matrix parameters associated with
     * the path segment
     * 
     * @return the map of matrix parameters. Never returns null.
     */
    public MultivaluedMap<String, String> getMatrixParameters() {
        if (this.matrixParameters == null) {
            MultivaluedMap<String, String> matrixParameters = new MultivaluedMapImpl<String, String>();
            if(matrParamEncoded != null)
            {
                String[] paramsEncSpl = matrParamEncoded.split(";");
                for (int i=0; i<paramsEncSpl.length; i++) {
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
                    String key = decode ? Reference.decode(keyEnc) : keyEnc;
                    String value = decode ? Reference.decode(valueEnc) : valueEnc;
                    matrixParameters.add(key, value);
                }
            }
            this.matrixParameters = matrixParameters;
        }
        return matrixParameters;
    }

    /**
     * @return the (decoded?) path segment
     * @see PathSegment#getPath()
     */
    public String getPath() {
        return path;
    }
    
    @Override
    public boolean equals(Object object)
    {
        if(this == object)
            return true;
        if(!(object instanceof JaxRsPathSegment))
            return false;
        PathSegment other = (PathSegment)object;
        return this.getPath().equals(other.getPath()) && this.getMatrixParameters().equals(other.getMatrixParameters());
    }

    
    @Override
    public int hashCode()
    {
        return this.path.hashCode() ^ this.getMatrixParameters().hashCode();
    }
}