package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.Key;
import org.foraci.mxf.mxfReader.UL;

/**
 * A key (with length and UL) and its values
 *
 * @author jforaci
 */
public abstract class Node implements Comparable
{
    private Key key;
    private int instanceId;

    public Node(Key key, int instanceId) {
        this.key = key;
        this.instanceId = instanceId;
    }

    public Key key() {
        return key;
    }

    public final int instanceId() {
        return instanceId;
    }

    /**
     * A convenience method to get the Universal Label (<code>UL</code>). This is the same as
     * invoking <code>key().getUL()</code>.
     * @return the Universal Label for this node
     */
    public UL ul() {
        return key().getUL();
    }

    public String toString() {
        return (key() == null) ? "<DOCUMENT>" : key().toString();
    }

    public int compareTo(Object o)
    {
        Node other = (Node) o;
        return instanceId() - other.instanceId();
    }
}
