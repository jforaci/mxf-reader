package org.foraci.mxf.mxfReader.entities;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.LocalTag;
import org.foraci.mxf.mxfReader.Utils;

/**
 * Date: Sep 22, 2009 3:05:48 PM
 *
 * @author jforaci
 */
public class PrimerPackEntry {
    private LocalTag localTag;
    private UL ul;

    public PrimerPackEntry(LocalTag localTag, UL ul) {
        this.localTag = localTag;
        this.ul = ul;
    }

    public String toString() {
        return "localTag=" + localTag + ",ul=" + Utils.bytesToString(ul.getKey());
    }

    public LocalTag getLocalTag() {
        return localTag;
    }

    public UL getUL() {
        return ul;
    }
}
