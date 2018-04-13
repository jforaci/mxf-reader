package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.entities.PartitionPackKind;

import java.math.BigInteger;
import java.util.List;

/**
 * A partition pack
 * @author jforaci
 */
public class PartitionPack {
    private int kind;
    private int status;
    private int majorVersion;
    private int minorVersion;
    private long kagSize;
    private BigInteger thisPartition;
    private BigInteger previousPartition;
    private BigInteger footerPartition;
    private BigInteger headerByteCount;
    private BigInteger indexByteCount;
    private long indexSid;
    private BigInteger bodyOffset;
    private long bodySid;
    private UL operationalPattern;
    private List<UL> essenceContainers;
    private BigInteger endOfHeader = null;
    private BigInteger endOfIndex = null;
    private BigInteger offset = null;

    public PartitionPack(int kind, int status, int majorVersion, int minorVersion,
                  long kagSize, BigInteger thisPartition, BigInteger previousPartition, BigInteger footerPartition,
                  BigInteger headerByteCount, BigInteger indexByteCount, long indexSid, BigInteger bodyOffset,
                  long bodySid, UL operationalPattern, List<UL> essenceContainers) {
        this.kind = kind;
        this.status = status;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.kagSize = kagSize;
        this.thisPartition = thisPartition;
        this.previousPartition = previousPartition;
        this.footerPartition = footerPartition;
        this.headerByteCount = headerByteCount;
        this.indexByteCount = indexByteCount;
        this.indexSid = indexSid;
        this.bodyOffset = bodyOffset;
        this.bodySid = bodySid;
        this.operationalPattern = operationalPattern;
        this.essenceContainers = essenceContainers;
    }

    public PartitionPackKind getKind() {
        if (kind == 0x02) {
            return PartitionPackKind.Header;
        } else if (kind == 0x03) {
            return PartitionPackKind.Body;
        } else if (kind == 0x04) {
            return PartitionPackKind.Footer;
        } else {
            return null;
        }
    }

    public int getStatus() {
        return status;
    }

    public boolean isClosed() {
        return (status == 0x02 || status == 0x04);
    }

    public boolean isComplete() {
        return (status == 0x03 || status == 0x04);
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public long getKagSize() {
        return kagSize;
    }

    public void setKagSize(long kagSize) {
        this.kagSize = kagSize;
    }

    public BigInteger getThisPartition() {
        return thisPartition;
    }

    public void setThisPartition(BigInteger thisPartition) {
        this.thisPartition = thisPartition;
    }

    public BigInteger getPreviousPartition() {
        return previousPartition;
    }

    public void setPreviousPartition(BigInteger previousPartition) {
        this.previousPartition = previousPartition;
    }

    public BigInteger getFooterPartition() {
        return footerPartition;
    }

    public void setFooterPartition(BigInteger footerPartition) {
        this.footerPartition = footerPartition;
    }

    public BigInteger getHeaderByteCount() {
        return headerByteCount;
    }

    public void setHeaderByteCount(BigInteger headerByteCount) {
        this.headerByteCount = headerByteCount;
    }

    public BigInteger getIndexByteCount() {
        return indexByteCount;
    }

    public void setIndexByteCount(BigInteger indexByteCount) {
        this.indexByteCount = indexByteCount;
    }

    public long getIndexSid() {
        return indexSid;
    }

    public void setIndexSid(long indexSid) {
        this.indexSid = indexSid;
    }

    public BigInteger getBodyOffset() {
        return bodyOffset;
    }

    public void setBodyOffset(BigInteger bodyOffset) {
        this.bodyOffset = bodyOffset;
    }

    public long getBodySid() {
        return bodySid;
    }

    public void setBodySid(long bodySid) {
        this.bodySid = bodySid;
    }

    public UL getOperationalPattern() {
        return operationalPattern;
    }

    public void setOperationalPattern(UL operationalPattern) {
        this.operationalPattern = operationalPattern;
    }

    public List<UL> getEssenceContainers() {
        return essenceContainers;
    }

    public void setEssenceContainers(List<UL> essenceContainers) {
        this.essenceContainers = essenceContainers;
    }

    public BigInteger getEndOfHeader() {
        return endOfHeader;
    }

    public void setEndOfHeader(BigInteger endOfHeader) {
        this.endOfHeader = endOfHeader;
    }

    public BigInteger getEndOfIndex() {
        return endOfIndex;
    }

    public void setEndOfIndex(BigInteger endOfIndex) {
        this.endOfIndex = endOfIndex;
    }

    public BigInteger getOffset() {
        return offset;
    }

    public void setOffset(BigInteger offset) {
        this.offset = offset;
    }
}
