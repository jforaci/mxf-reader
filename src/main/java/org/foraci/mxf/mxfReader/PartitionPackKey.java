package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.parsers.PartitionPackParser;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Sep 18, 2009 2:16:55 PM
 *
 * @author jforaci
 */
public class PartitionPackKey extends Key {
    private static final int PACKKIND_HEADER = 2;
    private static final int PACKKIND_BODY = 3;
    private static final int PACKKIND_FOOTER = 4;

    private static final int STATUS_OPEN_INCOM = 1;
    private static final int STATUS_CLOSED_INCOM = 2;
    private static final int STATUS_OPEN_COM = 3;
    private static final int STATUS_CLOSED_COM = 4;

    private int structureVersion;
    private int structureKind;
    private int packKind;
    private int partStatus;

    PartitionPackKey(UL ul, BigInteger length) {
        super(ul, length);
        byte[] key = ul.getKey();
        this.structureVersion = key[11] & 0xFF;
        this.structureKind = key[12] & 0xFF;
        this.packKind = key[13] & 0xFF;
        this.partStatus = key[14] & 0xFF;
    }

    public PartitionPackParser parser(MxfInputStream in) throws IOException {
        return new PartitionPackParser(length, in);
    }

    public int getStructureVersion() {
        return structureVersion;
    }

    public int getStructureKind() {
        return structureKind;
    }

    int getPackKind() {
        return packKind;
    }

    public boolean isHeader() {
        return (getPackKind() == PACKKIND_HEADER);
    }

    public boolean isBody() {
        return (getPackKind() == PACKKIND_BODY);
    }

    public boolean isFooter() {
        return (getPackKind() == PACKKIND_FOOTER);
    }

    public boolean isOpen() {
        return (getPartStatus() == STATUS_OPEN_INCOM
            || getPartStatus() == STATUS_OPEN_COM);
    }

    public boolean isComplete() {
        return (getPartStatus() == STATUS_CLOSED_COM
            || getPartStatus() == STATUS_OPEN_COM);
    }

    public int getPartStatus() {
        return partStatus;
    }
}
