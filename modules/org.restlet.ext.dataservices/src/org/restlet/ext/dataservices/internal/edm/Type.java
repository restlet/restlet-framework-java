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

package org.restlet.ext.dataservices.internal.edm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.restlet.engine.util.DateUtils;

/**
 * Type resolver. Able to handle ADO.NET <=> Java types conversions.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399213.aspx">Simple
 *      Types (EDM)</a>
 */
public class Type {

    /** Formater for the EDM Decimal type. */
    public static final NumberFormat decimalFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Double type. */
    public static final NumberFormat doubleFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Int16 type. */
    public static final NumberFormat int16Format = DecimalFormat
            .getIntegerInstance(Locale.US);

    /** Formater for the EDM Int32 type. */
    public static final NumberFormat int32Format = DecimalFormat
            .getIntegerInstance(Locale.US);

    /** Formater for the EDM Int64 type. */
    public static final NumberFormat int64Format = DecimalFormat
            .getIntegerInstance(Locale.US);

    /** Formater for the EDM Single type. */
    public static final NumberFormat singleFormat = DecimalFormat
            .getNumberInstance(Locale.US);

    /** Formater for the EDM Time type. */
    public static final NumberFormat timeFormat = DecimalFormat
            .getIntegerInstance(Locale.US);

    /**
     * Converts the String representation of the target ADO.NET type to its
     * corresponding value.
     * 
     * @param value
     *            The value to convert.
     * @param adoNetType
     *            The target ADO.NET type.
     * @return The converted value.
     */
    public static Object fromEdm(String value, String adoNetType) {
        if (value == null) {
            return null;
        }

        Object result = null;
        try {
            if (adoNetType.endsWith("Binary")) {
                result = value.getBytes();
            } else if (adoNetType.endsWith("Boolean")) {
                result = Boolean.valueOf(value);
            } else if (adoNetType.endsWith("DateTime")) {
                result = DateUtils.parse(value, DateUtils.FORMAT_RFC_3339);
            } else if (adoNetType.endsWith("DateTimeOffset")) {
                result = DateUtils.parse(value, DateUtils.FORMAT_RFC_3339);
            } else if (adoNetType.endsWith("Time")) {
                result = timeFormat.parseObject(value);
            } else if (adoNetType.endsWith("Decimal")) {
                result = decimalFormat.parseObject(value);
            } else if (adoNetType.endsWith("Single")) {
                result = singleFormat.parseObject(value);
            } else if (adoNetType.endsWith("Double")) {
                result = doubleFormat.parseObject(value);
            } else if (adoNetType.endsWith("Guid")) {
                result = value;
            } else if (adoNetType.endsWith("Int16")) {
                result = Short.valueOf(value);
            } else if (adoNetType.endsWith("Int32")) {
                result = Integer.valueOf(value);
            } else if (adoNetType.endsWith("Int64")) {
                result = Long.valueOf(value);
            } else if (adoNetType.endsWith("Byte")) {
                result = Byte.valueOf(value);
            } else if (adoNetType.endsWith("String")) {
                result = value;
            }
        } catch (Exception e) {
            Logger.getLogger(Type.class.getCanonicalName()).warning(
                    "Cannot convert " + value + " from this EDM type "
                            + adoNetType);
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
        // TODO hum hum
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
            Logger.getLogger(Type.class.getCanonicalName()).warning(
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
     * Converts a value to the String representation of the target ADO.NET
     * type.
     * 
     * @param value
     *            The value to convert.
     * @param adoNetType
     *            The target ADO.NET type.
     * @return The converted value.
     */
    public static String toEdm(Object value, String adoNetType) {
        if (value == null) {
            return null;
        }

        String result = null;
        if (adoNetType.endsWith("Binary")) {
            if ((byte[].class).isAssignableFrom(value.getClass())) {
                result = toEdmBinary((byte[]) value);
            }
        } else if (adoNetType.endsWith("Boolean")) {
            if ((Boolean.class).isAssignableFrom(value.getClass())) {
                result = toEdmBoolean((Boolean) value);
            }
        } else if (adoNetType.endsWith("DateTime")) {
            if ((Date.class).isAssignableFrom(value.getClass())) {
                result = toEdmDateTime((Date) value);
            }
        } else if (adoNetType.endsWith("DateTimeOffset")) {
            if ((Date.class).isAssignableFrom(value.getClass())) {
                result = toEdmDateTime((Date) value);
            }
        } else if (adoNetType.endsWith("Time")) {
            if ((Long.class).isAssignableFrom(value.getClass())) {
                result = toEdmTime((Long) value);
            }
        } else if (adoNetType.endsWith("Decimal")) {
            if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmDecimal((Double) value);
            }
        } else if (adoNetType.endsWith("Single")) {
            if ((Float.class).isAssignableFrom(value.getClass())) {
                result = toEdmSingle((Float) value);
            }
        } else if (adoNetType.endsWith("Double")) {
            if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmDouble((Double) value);
            }
        } else if (adoNetType.endsWith("Guid")) {
            result = value.toString();
        } else if (adoNetType.endsWith("Int16")) {
            if ((Short.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt16((Short) value);
            }
        } else if (adoNetType.endsWith("Int32")) {
            if ((Integer.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt32((Integer) value);
            }
        } else if (adoNetType.endsWith("Int64")) {
            if ((Long.class).isAssignableFrom(value.getClass())) {
                result = toEdmInt64((Long) value);
            }
        } else if (adoNetType.endsWith("Byte")) {
            if ((Byte.class).isAssignableFrom(value.getClass())) {
                result = toEdmByte((Byte) value);
            }
        } else if (adoNetType.endsWith("String")) {
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
        return DateUtils.format(value, DateUtils.FORMAT_RFC_3339.get(0));
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
        return int16Format.format(value);
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
        return int32Format.format(value);
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
        return int64Format.format(value);
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

    private String adoNetType;

    public Type(String adoNetType) {
        super();
        this.adoNetType = adoNetType;
    }

    public String getAdoNetType() {
        return adoNetType;
    }

    public Class<?> getJavaClass() {
        Class<?> result = Object.class;
        if (adoNetType.endsWith("Binary")) {
            result = byte[].class;
        } else if (adoNetType.endsWith("Boolean")) {
            result = Boolean.class;
        } else if (adoNetType.endsWith("DateTime")) {
            result = Date.class;
        } else if (adoNetType.endsWith("DateTimeOffset")) {
            result = Date.class;
        } else if (adoNetType.endsWith("Time")) {
            result = Long.class;
        } else if (adoNetType.endsWith("Decimal")) {
            result = Double.class;
        } else if (adoNetType.endsWith("Single")) {
            result = Float.class;
        } else if (adoNetType.endsWith("Double")) {
            result = Double.class;
        } else if (adoNetType.endsWith("Guid")) {
            result = String.class;
        } else if (adoNetType.endsWith("Int16")) {
            result = Short.class;
        } else if (adoNetType.endsWith("Int32")) {
            result = Integer.class;
        } else if (adoNetType.endsWith("Int64")) {
            result = Long.class;
        } else if (adoNetType.endsWith("Byte")) {
            result = Byte.class;
        } else if (adoNetType.endsWith("String")) {
            result = String.class;
        }

        return result;
    }

    public String getJavaType() {
        String result = "Object";
        if (adoNetType.endsWith("Binary")) {
            result = "byte[]";
        } else if (adoNetType.endsWith("Boolean")) {
            result = "boolean";
        } else if (adoNetType.endsWith("DateTime")) {
            result = "Date";
        } else if (adoNetType.endsWith("DateTimeOffset")) {
            result = "Date";
        } else if (adoNetType.endsWith("Time")) {
            result = "long";
        } else if (adoNetType.endsWith("Decimal")) {
            result = "double";
        } else if (adoNetType.endsWith("Single")) {
            result = "float";
        } else if (adoNetType.endsWith("Double")) {
            result = "double";
        } else if (adoNetType.endsWith("Guid")) {
            result = "String";
        } else if (adoNetType.endsWith("Int16")) {
            result = "short";
        } else if (adoNetType.endsWith("Int32")) {
            result = "int";
        } else if (adoNetType.endsWith("Int64")) {
            result = "long";
        } else if (adoNetType.endsWith("Byte")) {
            result = "byte";
        } else if (adoNetType.endsWith("String")) {
            result = "String";
        }

        return result;
    }

    public void setAdoNetType(String adoNetType) {
        this.adoNetType = adoNetType;
    }

}
