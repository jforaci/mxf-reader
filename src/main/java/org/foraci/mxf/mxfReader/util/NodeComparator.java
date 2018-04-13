package org.foraci.mxf.mxfReader.util;

import org.foraci.mxf.mxfReader.entities.Node;

import java.util.Comparator;

/**
 * Compares a <code>Node</code> according to its discovered order
 */
public class NodeComparator implements Comparator
{
    public int compare(Object o1, Object o2) {
        Node n1 = (Node) o1;
        Node n2 = (Node) o2;
        return n1.instanceId() - n2.instanceId();
    }
}
