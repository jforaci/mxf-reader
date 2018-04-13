package org.foraci.mxf.mxfReader;

import java.io.*;

/**
 * Interface for a receiver to manage output streams for extracting essence container data
 * @author jforaci
 */
public interface EssenceContainerOutputController {
    public static int SMPTE436_VBI_TRACK = 0x17010101;
    public static int SMPTE436_ANC_TRACK = 0x17010201;

    public boolean needEssence();

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul)
        throws IOException;

    public OutputStream getOutputForSystemElement(long bodySid, UL ul)
        throws IOException;

    public void close() throws IOException;
}
