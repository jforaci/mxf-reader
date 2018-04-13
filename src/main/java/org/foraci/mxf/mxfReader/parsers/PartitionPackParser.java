package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.BatchHeader;
import org.foraci.mxf.mxfReader.PartitionPack;
import org.foraci.mxf.mxfReader.*;

import java.math.BigInteger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * The <code>Parser</code> for an MXF Partition Pack. This is the fixed-length pack that
 * describes a partition in an MXF file.
 * @author jforaci
 */
public class PartitionPackParser extends Parser {
    private Key packKey;

    public PartitionPackParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public void setPackKey(Key packKey) {
        this.packKey = packKey;
    }

    public PartitionPack read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        int majorVersion = in.readUnsignedShort();
        int minorVersion = in.readUnsignedShort();
        long kagSize = in.readUInt();
        BigInteger thisPartition = in.readULong();
        BigInteger prevPartition = in.readULong();
        BigInteger footerPartition = in.readULong();
        BigInteger headerByteCount = in.readULong();
        BigInteger indexByteCount = in.readULong();
        long indexSid = in.readUInt();
        BigInteger bodyOffset = in.readULong();
        long bodySid = in.readUInt();
        UL operationalPattern = in.readUL();
        BatchHeader essBatchHeader = in.readBatch();
        List<UL> essenceContainers = new ArrayList<UL>();
        for (int i = 0; i < essBatchHeader.getSize(); i++) {
            essenceContainers.add(in.readUL());
        }
        count = length;
        int kind = packKey.getUL().getKey()[13];
        int status = packKey.getUL().getKey()[14];
        return new PartitionPack(kind, status, majorVersion, minorVersion, kagSize, thisPartition,
                prevPartition, footerPartition, headerByteCount, indexByteCount, indexSid, bodyOffset, bodySid,
                operationalPattern, essenceContainers);
    }
}
