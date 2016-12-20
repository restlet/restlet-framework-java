package org.restlet.ext.odata;

import static org.restlet.ext.odata.internal.edm.TypeUtils.dateTimeFormats;
import static org.restlet.ext.odata.internal.edm.TypeUtils.decimalFormat;
import static org.restlet.ext.odata.internal.edm.TypeUtils.doubleFormat;
import static org.restlet.ext.odata.internal.edm.TypeUtils.singleFormat;
import static org.restlet.ext.odata.internal.edm.TypeUtils.timeFormat;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmBinary;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmBoolean;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmByte;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmDateTime;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmDecimal;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmDouble;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmInt16;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmInt32;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmInt64;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmSingle;
import static org.restlet.ext.odata.internal.edm.TypeUtils.toEdmTime;

import java.util.Date;

import org.restlet.Context;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.odata.internal.edm.Type;

/**
 * Util class used to convert values to and from Edm types.<br>
 * Can be overriden to extends basic behaviour.
 */
public class EdmConverter {

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
    public Object fromEdm(String value, String adoNetType) {
        if (value == null) {
            return null;
        }

        Object result = null;
        try {
            if (adoNetType.endsWith("Binary")) {
                result = Base64.decode(value);
            } else if (adoNetType.endsWith("Boolean")) {
                result = Boolean.valueOf(value);
            } else if (adoNetType.endsWith("DateTime")) {
                result = DateUtils.parse(value, dateTimeFormats);
            } else if (adoNetType.endsWith("DateTimeOffset")) {
                result = DateUtils.parse(value, dateTimeFormats);
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
            Context.getCurrentLogger().warning(
                    "Cannot convert " + value + " from this EDM type "
                            + adoNetType);
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
     * @see <a href="http://www.odata.org/docs/%5BMC-APDSU%5D.htm#z61934eae311a4af4b8f882c112248651">Abstract Type
     *      System</a>
     */
    public String getLiteralForm(String value, String adoNetType) {
        if (value == null) {
            return null;
        }

        String result = null;
        try {
            if (adoNetType.endsWith("Binary")) {
                result = "'" + value + "'";
            } else if (adoNetType.endsWith("DateTime")) {
                result = "datetime'" + value + "'";
            } else if (adoNetType.endsWith("DateTimeOffset")) {
                result = "datetimeoffset'" + value + "'";
            } else if (adoNetType.endsWith("Time")) {
                result = "time'" + value + "'";
            } else if (adoNetType.endsWith("Guid")) {
                result = "guid'" + value + "'";
            } else if (adoNetType.endsWith("String")) {
                result = "'" + value + "'";
            }
        } catch (Exception e) {
            Context.getCurrentLogger().warning(
                    "Cannot convert " + value + " from this EDM type "
                            + adoNetType);
        }

        return result;
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
    public String toEdm(Object value, Type type) {
        String adoNetType = type.getName();
        if (value == null || adoNetType == null) {
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
            } else if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmSingle((Double) value);
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
     * Converts a value to the String representation of the target WCF type when
     * used a key in the URIs.
     * 
     * @param value
     *            The value to convert.
     * @param type
     *            The target WCF type.
     * @return The converted value.
     */
    public String toEdmKey(Object value, Type type) {
        String adoNetType = type.getName();
        if (value == null || adoNetType == null) {
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
            } else if ((Double.class).isAssignableFrom(value.getClass())) {
                result = toEdmSingle((Double) value);
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
            result = "'" + value.toString() + "'";
        }

        if (result == null) {
            result = value.toString();
        }

        return result;
    }

}
