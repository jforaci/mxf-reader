package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UID;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.Utils;

import java.util.Map;
import java.util.List;

/**
 * A sequence that belongs to a <code>Track</code>
 * @author jforaci
 */
public class Sequence implements UuidAddressable, Resolvable {
    private UID id;
    private UID genId;
    private UL dataDefinition;
    private long duration = -1;
    private List componentRefs;
    private List components;

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

    public List getComponentRefs() {
        return componentRefs;
    }

    public void setComponentRefs(List componentRefs) {
        this.componentRefs = componentRefs;
    }

    public List getComponents() {
        return components;
    }

    public void setComponents(List components) {
        this.components = components;
    }

    public void resolve(Map map) {
        components = Utils.resolveGuids(componentRefs, map);
    }
}
