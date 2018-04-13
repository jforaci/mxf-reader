package org.foraci.mxf.mxfReader;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Date: Oct 1, 2009 3:54:59 PM
 *
 * @author jforaci
 */
public abstract class PartialUL implements Comparable {
    private final String name;
    private final byte[] value;

    public PartialUL(String name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    abstract Key createKey(UL ul, BigInteger length);

    public boolean matchKeyPrefix(byte[] key) {
        for (int i = 0; i < value.length && i < key.length; i++) {
            if (value[i] != key[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(value);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PartialUL)) {
            return false;
        }
        return (compareTo(obj) == 0);
    }

    public int compareTo(Object o) {
        PartialUL ul = (PartialUL) o;
        for (int i = 0; i < value.length && i < ul.value.length; i++) {
            if (value[i] != ul.value[i]) {
                return ((value[i] & 0xFF) - (ul.value[i] & 0xFF));
            }
        }
        return (ul.value.length - value.length);
    }
}
