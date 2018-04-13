package org.foraci.mxf.mxfReader.anchd480;

import org.foraci.anc.anc.AncTrackReader;
import org.foraci.anc.anc.Smpte291InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.DataInputStream;

/**
 * HD 480 ancillary track reader. This is just a pass-thru.
 *
 * @see org.foraci.mxf.mxfReader.anc.Smpte291MxfInputStream
 * @author jforaci
 */
public class Hd480AncTrackReader extends AncTrackReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(Hd480AncTrackReader.class);

    private int numPackets;
    private int packetsRead;

    public Hd480AncTrackReader(InputStream in) throws IOException
    {
        super(in);
    }

    protected void doRead(OutputStream ancillaryOutputStream) throws IOException
    {
        do {
            final int bufferSize = 8 * 1024;
            byte[] buff = new byte[bufferSize];
            in.readFully(buff);
            ancillaryOutputStream.write(buff);
        } while (true);
    }

    protected Smpte291InputStream createSmpte291InputStream(InputStream inputStream)
    {
        Smpte291InputStream smpte291InputStream = new Smpte291ByteInputStream(new DataInputStream(inputStream), this);
        return smpte291InputStream;
    }

    public void run()
    {
        try {
            read();
        } catch (IOException e) {
            log.error("I/O error", e);
        } catch (InterruptedException e) {
            log.error("interrupted", e);
        }
    }
}
