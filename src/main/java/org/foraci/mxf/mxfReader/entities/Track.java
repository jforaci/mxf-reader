package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.Utils;

import java.util.Map;
import java.util.List;

/**
 * A track
 * @author jforaci
 */
public class Track implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private long trackId;
    private long trackNumber;
    private String name;
    private Rational editRate;
    private long origin;
    private List sequenceRefs;
    private List sequences;

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

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public long getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(long trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rational getEditRate() {
        return editRate;
    }

    public void setEditRate(Rational editRate) {
        this.editRate = editRate;
    }

    public long getOrigin() {
        return origin;
    }

    public void setOrigin(long origin) {
        this.origin = origin;
    }

    public List getSequenceRefs() {
        return sequenceRefs;
    }

    public void setSequenceRefs(List sequenceRefs) {
        this.sequenceRefs = sequenceRefs;
    }

    public List getSequences() {
        return sequences;
    }

    public void setSequences(List sequences) {
        this.sequences = sequences;
    }

    public void resolve(Map map) {
        sequences = Utils.resolveGuids(sequenceRefs, map);
    }
}
