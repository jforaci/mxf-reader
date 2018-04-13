package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.Key;

import java.io.IOException;

/**
 * Interface for parsers that that parse groups (e.g. SMPTE 336M Sets), returning
 * <code>Key</code>s for their value(s). Note: SMPTE 336M Packs wouldn't implement this since
 * the values in a pack don't really have a key (or length) associated with them.
 *
 * @author jforaci
 */
public interface SetParser
{
    public Key read() throws IOException;
}
