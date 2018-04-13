package org.foraci.mxf.mxfReader.registries;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.PartialUL;

import java.util.*;

/**
 * A registry that manages known ULs
 * @author jforaci
 */
public class Registry {
    private static HashMap registry = new HashMap();
    private static SortedSet partial = new TreeSet();

    public static void initialize() {
        Metadata.init();
        Groups.init();
        Labels.init();
    }

    static UL add(String name, byte[] key) {
        return add(name, key, null);
    }

    static UL add(String name, byte[] key, String description) {
        UL ul = new UL(name, key);
        ul.setDescription(description);
        registry.put(ul, ul);
        return ul;
    }

    static UL add(UL ul) {
        registry.put(ul, ul);
        return ul;
    }

    static void addPartial(PartialUL ul) {
        partial.add(ul);
    }

    public UL lookup(byte[] key) {
        return (UL)registry.get(new UL(null, key));
    }

    public PartialUL lookupPartial(byte[] key) {
        for (Iterator i = partial.iterator(); i.hasNext();) {
            PartialUL partialUL = (PartialUL) i.next();
            if (partialUL.matchKeyPrefix(key)) {
                return partialUL;
            }
        }
        return null;
    }
}
