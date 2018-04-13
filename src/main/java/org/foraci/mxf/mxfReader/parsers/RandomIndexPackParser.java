package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.*;
import org.foraci.mxf.mxfReader.entities.RipEntry;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * The <code>Parser</code> for an MXF Random Index Pack (RIP). This is an optional, fixed-length
 * pack that describes how to locate a partition in an MXF file.
 * @author jforaci
 */
public class RandomIndexPackParser extends Parser {
    private long entryCount;

    public RandomIndexPackParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public void setEntryCount(long entryCount) {
        this.entryCount = entryCount;
    }

    public RandomIndexPack read() throws IOException {
        if (entryCount == 0) {
            in.warn("RIP entryCount is zero, it wasn't set for some reason");
        }
        List<RipEntry> entries = new ArrayList<RipEntry>();
        for (long c = 0; c < entryCount; c++) {
            long bodySid = in.readUInt();
            BigInteger bodyOffset = in.readULong();
            entries.add(new RipEntry(bodySid, bodyOffset));
        }
        long totalLength = in.readUInt();
        count = length;
        return new RandomIndexPack(entries, totalLength);
    }
}
