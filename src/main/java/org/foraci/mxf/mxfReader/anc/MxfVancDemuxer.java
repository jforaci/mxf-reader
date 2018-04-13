package org.foraci.mxf.mxfReader.anc;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.MxfReader;

import java.io.File;
import java.io.OutputStream;

/**
 * Special instance of <code>MxfReader</code> that uses a <code>MxfVancOutputController</code>
 * to dump the SMPTE 436m data to a file
 */
public class MxfVancDemuxer extends MxfReader
{
    private final OutputStream outputStream;

    public MxfVancDemuxer(File sourceFile, OutputStream outputStream)
    {
        super(sourceFile);
        this.outputStream = outputStream;
    }

    @Override
    protected EssenceContainerOutputController createEssenceFileOutputController(File file)
    {
        return new MxfVancOutputController(outputStream);
    }
}
