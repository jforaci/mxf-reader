package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.UL;

import java.util.Map;

/**
 * A timecode component referenced by a <code>Sequence</code> in a <code>Track</code>
 * @author jforaci
 */
public class TimecodeComponent implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private UL dataDefinition;
    private long duration = -1;
    private int roundedTimeCodeBase;
    private long startTimeCode;
    private boolean dropFrame;

    public UID getId() {
        return id;
    }

    public void setId(UID id) {
        this.id = id;
    }

    public UID getGenId() {
        return genId;
    }

    public void setGenId(UID genId) {
        this.genId = genId;
    }

    public UL getDataDefinition() {
        return dataDefinition;
    }

    public void setDataDefinition(UL dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getRoundedTimeCodeBase() {
        return roundedTimeCodeBase;
    }

    public void setRoundedTimeCodeBase(int roundedTimeCodeBase) {
        this.roundedTimeCodeBase = roundedTimeCodeBase;
    }

    public long getStartTimeCode() {
        return startTimeCode;
    }

    public void setStartTimeCode(long startTimeCode) {
        this.startTimeCode = startTimeCode;
    }

    public boolean isDropFrame() {
        return dropFrame;
    }

    public void setDropFrame(boolean dropFrame) {
        this.dropFrame = dropFrame;
    }

    public void resolve(Map map) {
    }
}
