package org.foraci.mxf.mxfReader.tests;

import org.foraci.mxf.mxfReader.MxfReader;
import org.foraci.mxf.mxfReader.MxfTreeReader;

import java.io.IOException;
import java.io.File;

/**
 * A test for {@link MxfTreeReader}
 *
 * @author Joe Foraci
 */
public class TestTreeReader
{
    public static void main(String[] args) throws IOException
    {
        MxfTreeReader mxfReader = new MxfTreeReader(new File(args[0]));
        mxfReader.setDebugPrint(false);
        mxfReader.setMetadataReadMode(MxfReader.MetadataReadMode.HeaderOnly);
        mxfReader.readAll();
        mxfReader.dumpTree();
    }
}
