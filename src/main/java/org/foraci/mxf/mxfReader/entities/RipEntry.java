package org.foraci.mxf.mxfReader.entities;

import java.math.BigInteger;

/**
 * An entry for a Random Index Pack (RIP)
 */
public class RipEntry {
    private final long bodySid;
    private final BigInteger bodyOffset;

    public RipEntry(long bodySid, BigInteger bodyOffset) {
        this.bodySid = bodySid;
        this.bodyOffset = bodyOffset;
    }

    public BigInteger getBodyOffset() {
        return bodyOffset;
    }

    public long getBodySid() {
        return bodySid;
    }
}
