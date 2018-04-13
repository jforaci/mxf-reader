package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.UMID;

import java.util.Map;

/**
 * Essence container data
 * @author jforaci
 */
public class EssenceContainerData implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private UMID linkedPackageId;
    private Package linkedPackage;
    private long indexSid;
    private long bodySid;

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

    public UMID getLinkedPackageId() {
        return linkedPackageId;
    }

    public void setLinkedPackageId(UMID linkedPackageId) {
        this.linkedPackageId = linkedPackageId;
    }

    public Package getLinkedPackage() {
        return linkedPackage;
    }

    public void setLinkedPackage(Package linkedPackage) {
        this.linkedPackage = linkedPackage;
    }

    public long getIndexSid() {
        return indexSid;
    }

    public void setIndexSid(long indexSid) {
        this.indexSid = indexSid;
    }

    public long getBodySid() {
        return bodySid;
    }

    public void setBodySid(long bodySid) {
        this.bodySid = bodySid;
    }

    public void resolve(Map map) {
        linkedPackage = (Package) map.get(linkedPackageId);
    }
}
