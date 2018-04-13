package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.Utils;

import java.util.Map;
import java.util.Vector;
import java.util.List;

/**
 * Content storage
 * @author jforaci
 */
public class ContentStorage implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private List packageRefs;
    private List essenceContainerDataRefs;
    private List packages;
    private List essenceContainerData;

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

    public List getPackageRefs() {
        return packageRefs;
    }

    public void setPackageRefs(List packageRefs) {
        this.packageRefs = packageRefs;
    }

    public List getEssenceContainerDataRefs() {
        return essenceContainerDataRefs;
    }

    public void setEssenceContainerDataRefs(List essenceContainerDataRefs) {
        this.essenceContainerDataRefs = essenceContainerDataRefs;
    }

    public List getPackages() {
        return packages;
    }

    public void setPackages(List packages) {
        this.packages = packages;
    }

    public List getEssenceContainerData() {
        return essenceContainerData;
    }

    public void setEssenceContainerData(Vector essenceContainerData) {
        this.essenceContainerData = essenceContainerData;
    }

    public void resolve(Map map) {
        packages = Utils.resolveGuids(packageRefs, map);
        essenceContainerData = Utils.resolveGuids(essenceContainerDataRefs, map);
    }
}
