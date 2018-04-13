package org.foraci.mxf.mxfReader;

import java.util.Arrays;

/**
 * Date: Sep 24, 2009 4:26:22 PM
 *
 * @author jforaci
 */
public class UID
{
    private byte[] bytes;

    public UID(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("UID must be 16 btyes");
        }
        this.bytes = bytes;
    }

    public boolean equals(Object o) {
        if (!(o instanceof UID)) {
            return false;
        }
        UID UID = (UID) o;
        return (Arrays.equals(bytes, UID.bytes));
    }

    public int hashCode() {
        return (bytes != null ? Arrays.hashCode(bytes) : 0);
    }

    public String toString() {
        return Utils.bytesToString(bytes);
    }
}
