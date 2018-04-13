package org.foraci.mxf.mxfReader.entities;

import java.util.Map;

/**
 * Date: Oct 2, 2009 10:58:34 AM
 *
 * @author jforaci
 */
public interface Resolvable {
    /**
     * Called with a <code>Map</code> that maps <code>UID</code>s to <code>Addressable</code>s
     * @param map
     */
    public void resolve(Map map);
}
