package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.UMID;

import java.util.Map;

/**
 * A source clip referenced by a <code>Sequence</code> in a <code>Track</code>
 * @author jforaci
 */
public class SourceClip implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private UL dataDefinition;
    private long startPosition;
    private long duration = -1;
    private UMID sourcePackageId;
    private org.foraci.mxf.mxfReader.entities.Package sourcePackage;
    private int sourceTrackId;

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

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public UMID getSourcePackageId() {
        return sourcePackageId;
    }

    public void setSourcePackageId(UMID sourcePackageId) {
        this.sourcePackageId = sourcePackageId;
    }

    public int getSourceTrackId() {
        return sourceTrackId;
    }

    public void setSourceTrackId(int sourceTrackId) {
        this.sourceTrackId = sourceTrackId;
    }

    public org.foraci.mxf.mxfReader.entities.Package getSourcePackage() {
        return sourcePackage;
    }

    public void setSourcePackage(org.foraci.mxf.mxfReader.entities.Package sourcePackage) {
        this.sourcePackage = sourcePackage;
    }

    public void resolve(Map map) {
        sourcePackage = (org.foraci.mxf.mxfReader.entities.Package) map.get(sourcePackageId);
    }
}
