package org.foraci.mxf.mxfReader;

import java.util.Arrays;

/**
 * Date: Sep 25, 2009 2:31:39 PM
 *
 * @author jforaci
 */
public class UMID {
    private byte[] bytes;

    public UMID(byte[] bytes) {
        if (bytes == null || bytes.length != 32) {
            throw new IllegalArgumentException("UMID must be 32 btyes");
        }
        this.bytes = bytes;
    }

    public boolean equals(Object o) {
        if (!(o instanceof UMID)) {
            return false;
        }
        UMID umid = (UMID) o;
        return (Arrays.equals(bytes, umid.bytes));
    }

    public int hashCode() {
        return (bytes != null ? Arrays.hashCode(bytes) : 0);
    }

    public String toString() {
        return Utils.bytesToString(bytes);
    }
}
