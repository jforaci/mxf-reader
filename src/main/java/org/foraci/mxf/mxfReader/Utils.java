package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.entities.UuidAddressable;

import java.util.Vector;
import java.util.Map;
import java.util.List;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;

/**
 * Some utility methods
 * @author jforaci
 */
public class Utils {
    public static final String bytesToString(byte[] key) {
        StringBuffer sb = new StringBuffer();
        int i = 1;
        for (byte b : key) {
            String h = Integer.toHexString(b & 0xFF);
            if (h.length() == 1) {
                sb.append('0');
            }
            sb.append(h);
            if (i % 4 == 0) {
                sb.append(' ');
            } else {
//                sb.append('.');
            }
            i++;
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static List resolveGuids(List refs, Map map) {
        Vector instances = new Vector();
        for (int i = 0; refs != null && i < refs.size(); i++) {
            UuidAddressable addressable = (UuidAddressable) map.get((UID)refs.get(i));
            instances.addElement(addressable);
        }
        return instances;
    }

    public static String dump(Object o, int level) {
        level++;
        StringBuffer tabs = new StringBuffer();
        for (int k = 0; k < level; k++) {
            tabs.append("\t");
        }
        StringBuffer buffer = new StringBuffer();
        Class oClass = o.getClass();
        buffer.append("\n");
        buffer.append(tabs);
        buffer.append(oClass.getSimpleName());
        if (oClass.isArray()) {
            buffer.append("[");
            for (int i = 0; i < Array.getLength(o); i++) {
                if (i < 0)
                    buffer.append(",");
                Object value = Array.get(o, i);
                if (value.getClass().isPrimitive() ||
                        value.getClass() == java.lang.Long.class ||
                        value.getClass() == java.lang.String.class ||
                        value.getClass() == java.lang.Integer.class ||
                        value.getClass() == java.lang.Boolean.class
                        ) {
                    buffer.append(value);
                } else {
                    buffer.append(dump(value, level));
                }
            }
            buffer.append(tabs);
            buffer.append("]");
        } else {
            buffer.append("{\n");
            while (oClass != null) {
                Field[] fields = oClass.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    buffer.append(tabs);
                    fields[i].setAccessible(true);
                    buffer.append(fields[i].getName());
                    buffer.append("=");
                    try {
                        Object value = fields[i].get(o);
                        if (value != null) {
                            if (value.getClass().isPrimitive() ||
                                    value.getClass() == Long.class ||
                                    value.getClass() == String.class ||
                                    value.getClass() == Integer.class ||
                                    value.getClass() == Boolean.class ||
                                    value.getClass() == BigInteger.class ||
                                    value.getClass() == String.class
                                    ) {
                                buffer.append(value);
                            } else {
                                buffer.append("object: " + value);
//                                buffer.append(dump(value, level));
                            }
                        } else {
                            buffer.append("null");
                        }
                    } catch (IllegalAccessException e) {
                        buffer.append(e.getMessage());
                    }
                    buffer.append("\n");
                }
                oClass = oClass.getSuperclass();
            }
            buffer.append(tabs);
            buffer.append("}");
        }
        return buffer.toString();
    }
}
