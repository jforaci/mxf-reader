package org.foraci.mxf.mxfReader.tests;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.EssenceFileOutputController;
import org.foraci.mxf.mxfReader.MxfReader;
import org.foraci.mxf.mxfReader.UL;

import java.io.*;

/**
 * Simple test for <code>MxfReader</code>
 * @author jforaci
 */
public class TestReader {
    public static void main(String[] args) throws IOException, InterruptedException
    {
        final File file = new File(args[0]);
        MxfReader mxfReader = new MxfReader(file) {
            @Override
            public EssenceContainerOutputController createEssenceFileOutputController(File file) {
                return new EssenceFileOutputController(file) {
                    @Override
                    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) throws IOException {
                        if (ul.isDataEssence()) {
                            return super.getOutputForBodySidAndTrack(bodySid, ul);
                        }
                        return null;
                    }
                };
            }
        };
    }
}
