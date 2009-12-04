/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.odata.internal.edm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.restlet.Context;
import org.restlet.engine.util.DateUtils;

/**
 * Type resolver. Able to handle WCF <=> Java types conversions.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399213.aspx">Simple
 *      Types (EDM)</a>
 */
public class Type {

    /** Formater for the EDM DateTime type. */
    public static final List<String> dateTimeFormats = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss zzz");

    /** Formater for the EDM Decimal type. */
    public static final NumberFormat decimalFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Double type. */
    public static final NumberFormat doubleFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Single type. */
    public static final NumberFormat singleFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Time type. */
    public static final NumberFormat timeFormat = DecimalFormat
            .getIntegerInstance(Locale.US);

    /**
     * Converts the String representation of the target WCF type to its
     * corresponding value.
     * 
     * @param value
     *            The value to convert.
     * @param wcfType
     *            The target WCF type.
     * @return The converted value.
     */
    public static Object fromEdm(String value, String wcfType) {
        if (value == null) {
            return null;
        }

        Object result = null;
        try {
            if (wcfType.endsWith("Binary")) {
                result = value.getBytes();
            } else if (wcfType.endsWith("Boolean")) {
                result = Boolean.valueOf(value);
            } else if (wcfType.endsWith("DateTime")) {
                result = DateUtils.parse(value, dateTimeFormats);
            } else if (wcfType.endsWith("DateTimeOffset")) {
                result = DateUtils.parse(value, dateTimeFormats);
            } else if (wcfType.endsWith("Time")) {
                result = timeFormat.parseObject(value);
            } else if (wcfType.endsWith("Decimal")) {
                result = decimalFormat.parseObject(value);
            } else if (wcfType.endsWith("Single")) {
                result = singleFormat.parseObject(value);
            } else if (wcfType.endsWith("Double")) {
                result = doubleFormat.parseObject(value);
            } else if (wcfType.endsWith("Guid")) {
                result = value;
            } else if (wcfType.endsWith("Int16")) {
                result = Short.valueOf(value);
            } else if (wcfType.endsWith("Int32")) {
                result = Integer.valueOf(value);
            } else if (wcfType.endsWith("Int64")) {
                result = Long.valueOf(value);
            } else if (wcfType.endsWith("Byte")) {
                result = Byte.valueOf(value);
            } else if (wcfType.endsWith("String")) {
                result = value;
            }
        } catch (Exception e) {
            Context.getCurrentLogger().warning(
                    "Cannot convert " + value + " from this EDM type "
                            + wcfType);
        }

        return result;
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

        String[] tab = name.split("\\.");
        if (tab.length > 1) {
            int i = 0;
            for (; i < tab.length - 1; i++) {
                String string = tab[i];
                if (i > 0) {
                    builder.append(".");
                }
                builder.append(string.substring(0, 1).toLowerCase());
                builder.append(string.substring(1));
            }
            builder.append(".");
            builder.append(tab[i]);
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
     * Returns the package name related to the given schema.
     * 
     * @param schema
     *            The schema.
     * @return The package name related to the given schema.
     */
    public static String getPackageName(Schema schema) {
        return getPackageName(schema.getNamespace().getNormalizedName());
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
            builder.append(string.substring(0, 1).toLowerCase());
            builder.append(string.substring(1));
        }
        return builder.toString();
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
        String wcfType = type.getWcfType();
        if (value == null && wcfType == null) {
            return null;
        }

        String result = null;
        if (wcfType.endsWith("Binary")) {
            if ((byte[].class).isAssignableFrom(value.getClass())) {
                result = toEdmBinary((byte[]) value);
            }
        } else if (wcfType.endsWith("Boolean")) {
            if ((Boolean.class).isAssignableFrom(value.getClass())) {
                result = toEdmBoolean((Boolean) value);
            }
        } else if (wcfType.endsWith("DateTime")) {
            if ((Date.class).isAssignableFrom(value.getClass())) {
                result = toEdmDateTime((Date) value);
            }
        } else if (wcfType.endsWith("DateTimeOffset")) {
            if ((Date.class).isAssignableFrom(value.getClass())) {
                result = toEdmDateTime((Date) value);
            }
        } else if (wcfType.endsWith("Time")) {
            if ((Long.class).isAssignableFrom(value.getClass())) {
                result = toEdmTime((Long) value);
            }
        } else if (wcfType.endsWith("Decimal")) {
            if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmDecimal((Double) value);
            }
        } else if (wcfType.endsWith("Single")) {
            if ((Float.class).isAssignableFrom(value.getClass())) {
                result = toEdmSingle((Float) value);
            }
        } else if (wcfType.endsWith("Double")) {
            if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmDouble((Double) value);
            }
        } else if (wcfType.endsWith("Guid")) {
            result = value.toString();
        } else if (wcfType.endsWith("Int16")) {
            if ((Short.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt16((Short) value);
            }
        } else if (wcfType.endsWith("Int32")) {
            if ((Integer.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt32((Integer) value);
            }
        } else if (wcfType.endsWith("Int64")) {
            if ((Long.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt64((Long) value);
            }
        } else if (wcfType.endsWith("Byte")) {
            if ((Byte.class).isAssignableFrom(value.getClass())) {
                result = toEdmByte((Byte) value);
            }
        } else if (wcfType.endsWith("String")) {
            result = value.toString();
        }

        if (result == null) {
            result = value.toString();
        }

        return result;
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
        return new String(value);
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
        String wcfType = type.getWcfType();
        if (value == null && wcfType == null) {
            return null;
        }

        String result = null;
        if (wcfType.endsWith("Binary")) {
            if ((byte[].class).isAssignableFrom(value.getClass())) {
                result = toEdmBinary((byte[]) value);
            }
        } else if (wcfType.endsWith("Boolean")) {
            if ((Boolean.class).isAssignableFrom(value.getClass())) {
                result = toEdmBoolean((Boolean) value);
            }
        } else if (wcfType.endsWith("DateTime")) {
            if ((Date.class).isAssignableFrom(value.getClass())) {
                result = toEdmDateTime((Date) value);
            }
        } else if (wcfType.endsWith("DateTimeOffset")) {
            if ((Date.class).isAssignableFrom(value.getClass())) {
                result = toEdmDateTime((Date) value);
            }
        } else if (wcfType.endsWith("Time")) {
            if ((Long.class).isAssignableFrom(value.getClass())) {
                result = toEdmTime((Long) value);
            }
        } else if (wcfType.endsWith("Decimal")) {
            if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmDecimal((Double) value);
            }
        } else if (wcfType.endsWith("Single")) {
            if ((Float.class).isAssignableFrom(value.getClass())) {
                result = toEdmSingle((Float) value);
            }
        } else if (wcfType.endsWith("Double")) {
            if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmDouble((Double) value);
            }
        } else if (wcfType.endsWith("Guid")) {
            result = value.toString();
        } else if (wcfType.endsWith("Int16")) {
            if ((Short.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt16((Short) value);
            }
        } else if (wcfType.endsWith("Int32")) {
            if ((Integer.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt32((Integer) value);
            }
        } else if (wcfType.endsWith("Int64")) {
            if ((Long.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt64((Long) value);
            }
        } else if (wcfType.endsWith("Byte")) {
            if ((Byte.class).isAssignableFrom(value.getClass())) {
                result = toEdmByte((Byte) value);
            }
        } else if (wcfType.endsWith("String")) {
            result = "'" + value.toString() + "'";
        }

        if (result == null) {
            result = value.toString();
        }

        return result;
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

    private String wcfType;

    public Type(String wcfType) {
        super();
        this.wcfType = wcfType;
    }

    public Class<?> getJavaClass() {
        Class<?> result = Object.class;
        if (wcfType.endsWith("Binary")) {
            result = byte[].class;
        } else if (wcfType.endsWith("Boolean")) {
            result = Boolean.class;
        } else if (wcfType.endsWith("DateTime")) {
            result = Date.class;
        } else if (wcfType.endsWith("DateTimeOffset")) {
            result = Date.class;
        } else if (wcfType.endsWith("Time")) {
            result = Long.class;
        } else if (wcfType.endsWith("Decimal")) {
            result = Double.class;
        } else if (wcfType.endsWith("Single")) {
            result = Float.class;
        } else if (wcfType.endsWith("Double")) {
            result = Double.class;
        } else if (wcfType.endsWith("Guid")) {
            result = String.class;
        } else if (wcfType.endsWith("Int16")) {
            result = Short.class;
        } else if (wcfType.endsWith("Int32")) {
            result = Integer.class;
        } else if (wcfType.endsWith("Int64")) {
            result = Long.class;
        } else if (wcfType.endsWith("Byte")) {
            result = Byte.class;
        } else if (wcfType.endsWith("String")) {
            result = String.class;
        }

        return result;
    }

    public String getJavaType() {
        String result = "Object";
        if (wcfType.endsWith("Binary")) {
            result = "byte[]";
        } else if (wcfType.endsWith("Boolean")) {
            result = "boolean";
        } else if (wcfType.endsWith("DateTime")) {
            result = "Date";
        } else if (wcfType.endsWith("DateTimeOffset")) {
            result = "Date";
        } else if (wcfType.endsWith("Time")) {
            result = "long";
        } else if (wcfType.endsWith("Decimal")) {
            result = "double";
        } else if (wcfType.endsWith("Single")) {
            result = "float";
        } else if (wcfType.endsWith("Double")) {
            result = "double";
        } else if (wcfType.endsWith("Guid")) {
            result = "String";
        } else if (wcfType.endsWith("Int16")) {
            result = "short";
        } else if (wcfType.endsWith("Int32")) {
            result = "int";
        } else if (wcfType.endsWith("Int64")) {
            result = "long";
        } else if (wcfType.endsWith("Byte")) {
            result = "byte";
        } else if (wcfType.endsWith("String")) {
            result = "String";
        }

        return result;
    }

    public String getWcfType() {
        return wcfType;
    }

    public void setWcfType(String wcfType) {
        this.wcfType = wcfType;
    }

}
