package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.Key;
import org.foraci.mxf.mxfReader.UL;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * A <code>Node</code> that is a <em>set</em>, as defined by SMPTE 336 (KLV encoding)
 * with restrictions by SMPTE 377 (MXF Container)
 *
 * @author jforaci
 */
public class GroupNode extends Node
{
    private List<Node> children;

    public GroupNode(Key key, int instanceId) {
        super(key, instanceId);
        this.children = new LinkedList();
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node find(UL ul) {
        for (Iterator<Node> i = getChildren().iterator(); i.hasNext();) {
            Node node = i.next();
            if (node.ul().equals(ul)) {
                return node;
            }
        }
        return null;
    }

    public GroupNode ref(UL ul) {
        List<GroupNode> refs = refs(ul);
        if (refs == null) {
            return null;
        }
        return refs.get(0);
    }

    public List<GroupNode> refs(UL ul) {
        Node node = find(ul);
        if (node == null) {
            return null;
        }
        if (!(node instanceof LeafNode)) {
            return null;
        }
        LeafNode leafNode = (LeafNode) node;
        if (leafNode.refs() == null) {
            return null;
        }
        return leafNode.refs();
    }

    public Object value(UL ul) {
        Node node = find(ul);
        if (node == null) {
            return null;
        }
        if (!(node instanceof LeafNode)) {
            return null;
        }
        LeafNode leafNode = (LeafNode) node;
        if (leafNode.values().isEmpty()) {
            return null;
        }
        return leafNode.values().get(0);
    }

    public String string(UL ul) {
        Object value = value(ul);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
