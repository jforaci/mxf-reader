package org.foraci.mxf.mxfReader;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Does nothing
 */
public class DummyOutputController implements EssenceContainerOutputController
{
    public boolean needEssence() {
        return false;
    }

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) {
        return null;
    }

    public OutputStream getOutputForSystemElement(long bodySid, UL ul) {
        return null;
    }

    public void close() throws IOException {
    }
}
