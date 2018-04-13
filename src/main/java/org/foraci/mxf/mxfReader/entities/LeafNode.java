package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.Key;

import java.util.List;
import java.util.LinkedList;

/**
 * A <code>Node</code> that only contains one or more values and is not a
 * <code>GroupNode</code>
 *
 * @author jforaci
 */
public class LeafNode extends Node
{
    private List values;
    private List<GroupNode> references;

    public LeafNode(Key key, Object value, int instanceId) {
        super(key, instanceId);
        values = new LinkedList();
        values.add(value);
    }

    public void addValue(Object newValue) {
        values.add(newValue);
    }

    public void addReference(GroupNode group) {
        if (references == null) {
            references = new LinkedList<GroupNode>();
        }
        references.add(group);
    }

    public List<GroupNode> refs() {
        return references;
    }

    public List values() {
        return values;
    }
}
