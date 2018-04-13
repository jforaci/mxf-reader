package org.foraci.mxf.mxfReader;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to collect found partition packs when reading a stream
 */
public class PartitionPackManifest {
    private List<PartitionPack> list;
    private PartitionPack cur;
    private BigInteger knownFooterOffset;

    PartitionPackManifest() {
        this.list = new LinkedList<PartitionPack>();
    }

    public void set(PartitionPack partitionPack, boolean rewind) {
        if (partitionPack == null) {
            throw new NullPointerException("partitionPack can not be null");
        }
        cur = partitionPack;
        if (!rewind) {
            list.add(cur);
        } else {
            list.add(0, cur);
        }
    }

    public void push() {
        cur = null;
    }

    public int count() {
        return list.size();
    }

    public boolean hasCurrent() {
        return (current() != null);
    }

    public PartitionPack current() {
        return cur;
    }

    public void pivot() {
        list = new ArrayList<PartitionPack>();
        list.add(cur);
    }

    public List<PartitionPack> getList() {
        return Collections.unmodifiableList(list);
    }

    public BigInteger getKnownFooterOffset() {
        return knownFooterOffset;
    }

    public void setKnownFooterOffset(BigInteger knownFooterOffset) {
        this.knownFooterOffset = knownFooterOffset;
    }
}
