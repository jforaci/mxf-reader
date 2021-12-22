package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.exceptions.ValidationException;
import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.entities.Node;
import org.foraci.mxf.mxfReader.util.NodeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * MXF reader that builds a tree of the metadata
 *
 * @author jforaci
 */
public class MxfTreeReader extends MxfReader
{
    private static final Logger log = LoggerFactory.getLogger(MxfTreeReader.class);

    private static int PARTITION_PACK_NOT_FOUND = 0;
    private static int PARTITION_PACK_FOUND = 1;
    private static int PARTITION_PACK_READ = 2;

    private boolean readClosedCompleteMetadataOnly;
    private int partitionPackState;
    private Stack<GroupNode> stack;
    private GroupNode root, cur;
    private LeafNode lastLeaf;
    private int instanceNumber;
    private SortedSet<GroupNode> groups;
    private Map<UID, LeafNode> strongRefMap;
    private Map<UID, GroupNode> uuidMap;
    private int rlevel = 0;

    public MxfTreeReader(File file) {
        super(file);
        setReadClosedCompleteMetadataOnly(true);
    }

    public GroupNode getRootGroupNode() {
        return root;
    }

    public SortedSet<GroupNode> getGroups() {
        return Collections.unmodifiableSortedSet(groups);
    }

    public GroupNode find(UID uid) {
        return uuidMap.get(uid);
    }

    @Override
    public void readAll() throws IOException {
        log.info(getClass().getSimpleName() + ".readAll() parsing: " + getFile().getName());
        partitionPackState = PARTITION_PACK_NOT_FOUND;
        stack = new Stack<GroupNode>();
        instanceNumber = 0;
        root = cur = new GroupNode(null, instanceNumber++); // this is the "document" node
        lastLeaf = null;
        groups = new TreeSet<GroupNode>(new NodeComparator());
        strongRefMap = new HashMap<UID, LeafNode>();
        uuidMap = new HashMap<UID, GroupNode>();
        rlevel = 0;
        super.readAll();
        resolveReferences();
        if (partitionPackState == PARTITION_PACK_FOUND) {
            partitionPackState = PARTITION_PACK_READ;
        }
        log.info("MxfTreeParser.readAll() done: " + getFile().getName());
    }

    /**
     * Validates the MXF stream that was read. This implementation throws a
     * <code>IllegalStateException</code> if <code>readAll()</code> was never called
     * and only validates that a closed, complete partition was found.
     */
    public void validate() {
        if (root == null) { // readAll() was never invoked
            throw new IllegalStateException("no MXF stream has been read");
        }
        if (!isPartitionPackFound()) {
            throw new ValidationException("no partition with finalized metadata found");
        }
    }

    public boolean isPartitionPackFound() {
        return (partitionPackState == PARTITION_PACK_READ);
    }

    @Override
    public void partitionPackRead(PartitionPack partitionPack) {
        super.partitionPackRead(partitionPack);
        if (partitionPackState == PARTITION_PACK_READ) {
            return;
        }
        if (partitionPackState == PARTITION_PACK_NOT_FOUND) {
            if (isReadClosedCompleteMetadataOnly()) {
                if (!partitionPack.isClosed() || !partitionPack.isComplete()
                        || partitionPack.getHeaderByteCount().equals(BigInteger.ZERO)) {
                    return;
                }
            }
            partitionPackState = PARTITION_PACK_FOUND;
            return;
        }
        partitionPackState = PARTITION_PACK_READ;
    }

    @Override
    public void groupSetStarted(Key key) {
        super.groupSetStarted(key);
        if (partitionPackState != PARTITION_PACK_FOUND) {
            return;
        }
        GroupNode group = new GroupNode(key, instanceNumber++);
        cur.addChild(group);
        stack.push(cur);
        cur = group;
    }

    @Override
    public void groupSetEnded(Key key) {
        super.groupSetEnded(key);
        if (partitionPackState != PARTITION_PACK_FOUND) {
            return;
        }
        groups.add(cur);
        cur = stack.pop();
    }

    @Override
    public void valueRead(Key key, Object value) {
        super.valueRead(key, value);
        if (partitionPackState != PARTITION_PACK_FOUND) {
            return;
        }
        // check if we have a strong reference referring to this instance UID
        if (key.getUL().equals(Metadata.InstanceID)) {
            // record this UUID's group (parent) for lookup by client
            if (!(value instanceof UID)) {
                warn("value not a UID for key " + key + " at " + in.getLastKeyOffset());
            } else {
                uuidMap.put((UID) value, cur);
            }
        }
        //
        LeafNode leaf;
        if (lastLeaf != null && lastLeaf.key().equals(key)) {
            leaf = lastLeaf;
            leaf.addValue(value);
        } else {
            leaf = new LeafNode(key, value, instanceNumber++);
            lastLeaf = leaf;
            cur.addChild(leaf);
        }
        // record strong references
        if (key.getUL().isStrongReference() && (value instanceof UID)) {
            strongRefMap.put((UID) value, leaf);
        }
    }

    private void resolveReferences() {
        List list = new ArrayList(uuidMap.keySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                Node n1 = uuidMap.get(o1);
                Node n2 = uuidMap.get(o2);
                return n1.instanceId() - n2.instanceId();
            }
        });
        for (Iterator<UID> u = list.iterator(); u.hasNext();) {
            UID uid = u.next();
            LeafNode referent = strongRefMap.get(uid);
            if (referent != null) {
                GroupNode group = uuidMap.get(uid);
                referent.addReference(group);
            }
        }
    }

    public void dumpTree() {
        SortedSet<GroupNode> lgroups = new TreeSet<GroupNode>(groups);
        printTree(root);
        groups = lgroups;
    }

    private void printTree(GroupNode group) {
        for (Iterator<Node> i = group.getChildren().iterator(); i.hasNext();) {
            Node node = i.next();
            if (node instanceof GroupNode) {
                if (!groups.contains(node)) {
                    continue;
                }
                print(rlevel, node.key().toString() + ":");
                rlevel++;
                printTree((GroupNode) node);
                rlevel--;
            } else if (node instanceof LeafNode) {
                LeafNode leaf = (LeafNode) node;
                if (leaf.values().size() > 1) {
                    StringBuilder valuesString = new StringBuilder();
                    valuesString.append('[');
                    for (Iterator j = leaf.values().iterator(); j.hasNext();) {
                        Object value = j.next();
                        valuesString.append(String.valueOf(value));
                        if (j.hasNext()) {
                            valuesString.append(',');
                        }
                    }
                    valuesString.append(']');
                    print(rlevel, node.key().toString() + ": " + valuesString);
                } else {
                    Object value = leaf.values().get(0);
                    print(rlevel, node.key().toString() + ": " + value);
                }
                if (leaf.refs() != null) {
                    for (Iterator<GroupNode> j = leaf.refs().iterator(); j.hasNext();) {
                        GroupNode value = j.next();
                        print(rlevel, value.key().toString() + ":");
                        rlevel++;
                        printTree(value);
                        groups.remove(value);
                        rlevel--;
                    }
                }
            }
        }
    }

    private void print(int level, String message) {
        String pre = "";
        if (level > 0) {
            switch (level) {
                case 1:
                    pre = "\t";
                    break;
                case 2:
                    pre = "\t\t";
                    break;
                default:
                    char[] clevel = new char[level];
                    Arrays.fill(clevel, '\t');
                    pre = new String(clevel);
            }
        }
        dump(pre + message);
    }

    protected void dump(String line) {
        log.info(line);
    }

    /**
     * Whether to only read header metadata from a partition that is marked as closed and
     * complete. This property and <code>MxfReader</code>'s <code>readHeaderOnly</code> property
     * can't both be set. The default value is <code>true</code>.
     * @return Whether we will only build a tree from metadata from a closed, complete partition
     */
    public boolean isReadClosedCompleteMetadataOnly() {
        return readClosedCompleteMetadataOnly;
    }

    /**
     * Sets whether to only read header metadata from a partition that is marked as closed and
     * complete. This property and <code>MxfReader</code>'s <code>readHeaderOnly</code> property
     * can't both be set. This implementation will unset <code>readHeaderOnly</code> when this
     * property is set. The default value is <code>true</code>.
     */
    public void setReadClosedCompleteMetadataOnly(boolean readClosedCompleteMetadataOnly) {
        this.readClosedCompleteMetadataOnly = readClosedCompleteMetadataOnly;
        if (readClosedCompleteMetadataOnly) {
            setMetadataReadMode(MetadataReadMode.All);
        }
    }

    /**
     * Sets whether to only read header metadata from the first (header) partition.
     * This implementation will unset <code>readClosedCompleteMetadataOnly</code>
     * when this property is set.
     * @param mode Set to only read metadata from the first partition
     */
    @Override
    public void setMetadataReadMode(MetadataReadMode mode) {
        super.setMetadataReadMode(mode);
        if (mode == MetadataReadMode.HeaderOnly) {
            this.readClosedCompleteMetadataOnly = false;
        }
    }
}
