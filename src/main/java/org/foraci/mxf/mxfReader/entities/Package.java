package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.UMID;
import org.foraci.mxf.mxfReader.Utils;

import java.util.Map;
import java.util.Date;
import java.util.List;

/**
 * Date: Oct 2, 2009 4:40:53 PM
 *
 * @author jforaci
 */
public class Package implements UuidAddressable, UmidAddressable, Resolvable {
    private UID id;
    private UMID packageId;
    private UID genId;
    private String name;
    private Date creationDate;
    private Date modifiedDate;
    private List trackRefs;
    private List tracks;
    private UID essenceDescriptionId;

    public UID getId() {
        return id;
    }

    public UMID getUmid() {
        return getPackageId();
    }

    public void setId(UID id) {
        this.id = id;
    }

    public UMID getPackageId() {
        return packageId;
    }

    public void setPackageId(UMID packageId) {
        this.packageId = packageId;
    }

    public UID getGenId() {
        return genId;
    }

    public void setGenId(UID genId) {
        this.genId = genId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List getTrackRefs() {
        return trackRefs;
    }

    public void setTrackRefs(List trackRefs) {
        this.trackRefs = trackRefs;
    }

    public List getTracks() {
        return tracks;
    }

    public void setTracks(List tracks) {
        this.tracks = tracks;
    }

    public UID getEssenceDescriptionId()
    {
        return essenceDescriptionId;
    }

    public void setEssenceDescriptionId(UID uid)
    {
        essenceDescriptionId = uid;
    }

    public void resolve(Map map) {
        tracks = Utils.resolveGuids(trackRefs, map);
    }
}
