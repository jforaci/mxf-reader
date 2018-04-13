package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.entities.RipEntry;

import java.util.Collections;
import java.util.List;

/**
 * An MXF Random Index Pack (RIP). This is an optional, fixed-length
 * pack that describes how to locate a partition in an MXF file.
 */
public class RandomIndexPack {
    private List<RipEntry> entries;
    private final long totalLength;
    private long offset;

    public RandomIndexPack(List<RipEntry> entries, long totalLength) {
        this.entries = entries;
        this.totalLength = totalLength;
    }

    public List<RipEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public long getTotalLength() {
        return totalLength;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
