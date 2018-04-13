package org.foraci.mxf.mxfReader;

import java.util.Arrays;

/**
 * Date: Sep 24, 2009 5:39:18 PM
 *
 * @author jforaci
 */
public class LocalTag {
    private byte[] key;

    public LocalTag(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("local tag may not have a null key");
        }
        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }

    public boolean equals(Object o) {
        if (!(o instanceof LocalTag)) {
            return false;
        }
        LocalTag localTag = (LocalTag) o;
        return (Arrays.equals(key, localTag.key));
    }

    public int hashCode() {
        return (key != null ? Arrays.hashCode(key) : 0);
    }

    public String toString() {
        return Utils.bytesToString(key);
    }
}
