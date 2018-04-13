package org.foraci.mxf.mxfReader;

/**
 * Date: Sep 21, 2009 10:03:33 AM
 *
 * @author jforaci
 */
public class BatchHeader {
    private long size;
    private long elementSize;

    BatchHeader(long size, long elementSize) {
        this.size = size;
        this.elementSize = elementSize;
    }

    public long getSize() {
        return size;
    }

    public long getElementSize() {
        return elementSize;
    }
}
