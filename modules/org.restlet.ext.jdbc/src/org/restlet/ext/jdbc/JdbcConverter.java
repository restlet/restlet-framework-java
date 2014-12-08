/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jdbc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.WebRowSet;

import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * A converter helper to convert from {@link WebRowSet}, {@link JdbcResult} or
 * {@link ResultSet} objects to Representation.
 * 
 * @author Thierry Boileau
 * @deprecated Use a persistence technology such as Mybatis or Hibernate instead.
 */
@Deprecated
public class JdbcConverter extends ConverterHelper {

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        return null;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        return null;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        if (source instanceof WebRowSet || source instanceof JdbcResult
                || source instanceof ResultSet) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        return 0;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
        if (source instanceof WebRowSet) {
            return new RowSetRepresentation((WebRowSet) source);
        } else if (source instanceof JdbcResult) {
            try {
                return new RowSetRepresentation((JdbcResult) source);
            } catch (SQLException e) {
                throw new IOException(
                        "Cannot convert the JdbcResult source object as a RowSetRepresentation due to:"
                                + e.getMessage());
            }
        } else if (source instanceof ResultSet) {
            try {
                return new RowSetRepresentation((ResultSet) source);
            } catch (SQLException e) {
                throw new IOException(
                        "Cannot convert the ResultSet source object as a RowSetRepresentation due to:"
                                + e.getMessage());

            }
        }

        return null;
    }

}
