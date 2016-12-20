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

package org.restlet.ext.odata.internal.edm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.restlet.Context;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.odata.EdmConverter;
import org.restlet.ext.odata.JavaTypeHandler;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;

/**
 * Handle type operations.
 * 
 * @author Thierry Boileau
 */
public class TypeUtils {

    /** Formater for the EDM DateTime type. */
    public static final List<String> dateTimeFormats = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm", "EEE, dd MMM yyyy HH:mm:ss zzz");

    /** Formater for the EDM Decimal type. */
    public static final NumberFormat decimalFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Double type. */
    public static final NumberFormat doubleFormat = new DecimalFormat(
            "0.###############", new DecimalFormatSymbols(Locale.US));

    /** Formater for the EDM Single type. */
    public static final NumberFormat singleFormat = new DecimalFormat(
            "0.#######", new DecimalFormatSymbols(Locale.US));

    /** Formater for the EDM Time type. */
    public static final NumberFormat timeFormat = DecimalFormat
            .getIntegerInstance(Locale.US);

    private static EdmConverter edmConverter = new EdmConverter();
    private static JavaTypeHandler javaTypeHandler = new JavaTypeHandler();
    
    /**
     * Converts the String representation of the target WCF type to its
     * corresponding value.
     * 
     * @param value
     *            The value to convert.
     * @param adoNetType
     *            The target WCF type.
     * @return The converted value.
     */
    public static Object fromEdm(String value, String adoNetType) {
        if (value == null) {
            return null;
        }

        return edmConverter.fromEdm(value, adoNetType);
    }

    /**
     * Returns a correct full class name from the given name. Especially, it
     * ensures that the first character of each sub package is in lower case.
     * 
     * @param name
     *            The name.
     * @return The package name extracted from the given name.
     */
    public static String getFullClassName(String name) {
        StringBuilder builder = new StringBuilder();

        int index = name.lastIndexOf(".");
        if (index > -1) {
            builder.append(getPackageName(ReflectUtils.normalize(name
                    .substring(0, index))));
            builder.append(name.substring(index));
        } else {
            builder.append(name);
        }

        return builder.toString();
    }

    /**
     * Returns the Java class that corresponds to the given type according to
     * the naming rules. It looks for the schema namespace name taken as the
     * package name, then the name of this entity type is the class name.
     * 
     * @param type
     *            The entity type.
     * @return The Java class that corresponds to this type.
     */
    public static Class<?> getJavaClass(EntityType type) {
        Class<?> result = null;
        String fullClassName = getPackageName(type.getSchema()) + "."
                + type.getClassName();
        try {
            result = Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            Context.getCurrentLogger().warning(
                    "Can't find the following class in the class loader: "
                            + fullClassName);
        }
        return result;
    }

    /**
     * Returns the literal form of the given value.
     * 
     * @param value
     *            The value to convert.
     * @param adoNetType
     *            The type of the value.
     * @return The literal form of the given value.
     * @see <a
     *      href="http://www.odata.org/docs/%5BMC-APDSU%5D.htm#z61934eae311a4af4b8f882c112248651">Abstract
     *      Type System</a>
     */
    public static String getLiteralForm(String value, String adoNetType) {
        if (value == null) {
            return null;
        }

        return edmConverter.getLiteralForm(value, adoNetType);
    }

    /**
     * Returns the package name related to the given schema.
     * 
     * @param schema
     *            The schema.
     * @return The package name related to the given schema.
     */
    public static String getPackageName(Schema schema) {
        return getPackageName(schema.getNamespace().getName());
    }

    /**
     * Returns a correct package name from the given name. Especially, it
     * ensures that the first character of each sub package is in lower case.
     * 
     * @param name
     *            The name.
     * @return The package name extracted from the given name.
     */
    public static String getPackageName(String name) {
        StringBuilder builder = new StringBuilder();

        String[] tab = name.split("\\.");
        for (int i = 0; i < tab.length; i++) {
            String string = tab[i];
            if (i > 0) {
                builder.append(".");
            }
            builder.append(string.toLowerCase());
        }
        return builder.toString();
    }
    
    /**
     * Allows to specify a new Edm type converter.
     * @param edmConverter
     *          The new Edm type converter.
     */
    public static void registerEdmConverter(EdmConverter edmConverter) {
        Objects.requireNonNull(edmConverter);
        
        TypeUtils.edmConverter = edmConverter;
    }

    /**
     * Allows to specify a new Java type handler.
     * @param javaTypeHandler
     *          The new Java type handler.
     */
    public static void registerJavaTypeHandler(JavaTypeHandler javaTypeHandler) {
        Objects.requireNonNull(javaTypeHandler);
        
        TypeUtils.javaTypeHandler = javaTypeHandler;
    }
    
    /**
     * Converts a value to the String representation of the target WCF type.
     * 
     * @param value
     *            The value to convert.
     * @param type
     *            The target WCF type.
     * @return The converted value.
     */
    public static String toEdm(Object value, Type type) {
        String adoNetType = type.getName();
        if (value == null || adoNetType == null) {
            return null;
        }

        return edmConverter.toEdm(value, type);
    }

    /**
     * Convert the given value to the String representation of a EDM Binary
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmBinary(byte[] value) {
        return Base64.encode(value, false);
    }

    /**
     * Convert the given value to the String representation of a EDM Boolean
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmBoolean(boolean value) {
        return Boolean.toString(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Byte value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmByte(byte value) {
        return Byte.toString(value);
    }

    /**
     * Convert the given value to the String representation of a EDM DateTime
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmDateTime(Date value) {
        return DateUtils.format(value, dateTimeFormats.get(0));
    }

    /**
     * Convert the given value to the String representation of a EDM Decimal
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmDecimal(double value) {
        return decimalFormat.format(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Double
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmDouble(double value) {
        return doubleFormat.format(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Int16
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmInt16(short value) {
        return Short.toString(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Int32
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmInt32(int value) {
        return Integer.toString(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Int64
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmInt64(long value) {
        return Long.toString(value);
    }

    /**
     * Converts a value to the String representation of the target WCF type when
     * used a key in the URIs.
     * 
     * @param value
     *            The value to convert.
     * @param type
     *            The target WCF type.
     * @return The converted value.
     */
    public static String toEdmKey(Object value, Type type) {
        String adoNetType = type.getName();
        if (value == null || adoNetType == null) {
            return null;
        }

        return edmConverter.toEdmKey(value, type);
    }

    /**
     * Convert the given value to the String representation of a EDM Single
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmSingle(double value) {
        return singleFormat.format(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Single
     * value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmSingle(float value) {
        return singleFormat.format(value);
    }

    /**
     * Convert the given value to the String representation of a EDM Time value.
     * 
     * @param value
     *            The value to convert.
     * @return The value converted as String object.
     */
    public static String toEdmTime(long value) {
        return timeFormat.format(value);
    }

    /**
     * Returns the corresponding Java class or scalar type.
     * 
     * @param edmTypeName
     *            The type name.
     * @return The corresponding Java class or scalar type.
     */
    public static Class<?> toJavaClass(String edmTypeName) {
        return javaTypeHandler.toJavaClass(edmTypeName);
    }

    /**
     * Returns the name of the corresponding Java class or scalar type.
     * 
     * @param edmTypeName
     *            The type name.
     * @return The name of the corresponding Java class or scalar type.
     */
    public static String toJavaTypeName(String edmTypeName) {
        return javaTypeHandler.toJavaTypeName(edmTypeName);
    }

}
