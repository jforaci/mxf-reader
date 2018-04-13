package org.foraci.mxf.mxfReader.anc;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.UL;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used to dump the 436m VANC/VBI data
 */
class MxfVancOutputController implements EssenceContainerOutputController
{
    private final OutputStream out;

    MxfVancOutputController(OutputStream out)
    {
        this.out = out;
    }

    public boolean needEssence()
    {
        return true;
    }

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) throws IOException
    {
        final long trackNumber = ul.getTrackNumber();
        if (!ul.isDataEssence() || (trackNumber != SMPTE436_VBI_TRACK && trackNumber != SMPTE436_ANC_TRACK)) {
            return null;
        }
        return out;
    }

    public OutputStream getOutputForSystemElement(long bodySid, UL ul) throws IOException
    {
        return null; // this output controller doesn't return System Elements
    }

    public void close() throws IOException { }
}
