package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.Utils;

import java.util.Date;
import java.util.Vector;
import java.util.Map;
import java.util.List;

/**
 * Date: Oct 2, 2009 10:45:18 AM
 *
 * @author jforaci
 */
public class Preface implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private Date lastModified;
    private Object version;
    private Object modelVersion;
    private UID primaryPackage;
    private List identificationRefs;
    private UID contentStorage;
    private UL operationalPattern;
    private List identifications;
    private List essenceContainers;
    private List dmSchemes;

/*
    public Preface(UID id, Date lastModified, Object version, Vector identificationRefs,
                   UID contentStorage, Vector essenceContainerRefs, Vector dmSchemeRefs,
                   UL operationalPattern) {
        this.id = id;
        this.lastModified = lastModified;
        this.version = version;
        this.identificationRefs = identificationRefs;
        this.contentStorage = contentStorage;
        this.essenceContainerRefs = essenceContainerRefs;
        this.dmSchemeRefs = dmSchemeRefs;
        this.operationalPattern = operationalPattern;
    }
*/

    public void resolve(Map map) {
        identifications = Utils.resolveGuids(identificationRefs, map);
    }

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

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Object getVersion() {
        return version;
    }

    public void setVersion(Object version) {
        this.version = version;
    }

    public Object getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(Object modelVersion) {
        this.modelVersion = modelVersion;
    }

    public UID getPrimaryPackage() {
        return primaryPackage;
    }

    public void setPrimaryPackage(UID primaryPackage) {
        this.primaryPackage = primaryPackage;
    }

    public List getIdentificationRefs() {
        return identificationRefs;
    }

    public void setIdentificationRefs(List identificationRefs) {
        this.identificationRefs = identificationRefs;
    }

    public UID getContentStorage() {
        return contentStorage;
    }

    public void setContentStorage(UID contentStorage) {
        this.contentStorage = contentStorage;
    }

    public UL getOperationalPattern() {
        return operationalPattern;
    }

    public void setOperationalPattern(UL operationalPattern) {
        this.operationalPattern = operationalPattern;
    }

    public List getIdentifications() {
        return identifications;
    }

    public void setIdentifications(Vector identifications) {
        this.identifications = identifications;
    }

    public List getEssenceContainers() {
        return essenceContainers;
    }

    public void setEssenceContainers(List essenceContainers) {
        this.essenceContainers = essenceContainers;
    }

    public List getDmSchemes() {
        return dmSchemes;
    }

    public void setDmSchemes(List dmSchemes) {
        this.dmSchemes = dmSchemes;
    }
}
