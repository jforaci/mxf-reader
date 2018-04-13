package org.foraci.mxf.mxfReader.anc;

import org.foraci.anc.anc.AncPacketHeader;
import org.foraci.anc.anc.Smpte291InputStream;
import org.foraci.anc.anc.AncTrackReader;
import org.foraci.anc.anc.TrackAttributes;
import org.foraci.anc.atc.AncillaryTimecodePacket;
import org.foraci.anc.util.timecode.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;

/**
 * MXF SMPTE 436m ancillary track reader
 *
 * @see org.foraci.mxf.mxfReader.anc.Smpte291MxfInputStream
 * @author jforaci
 */
public class Mxf436AncTrackReader extends AncTrackReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(Mxf436AncTrackReader.class);

    private final Type type;

    public enum Type {
        VBI,
        ANC
    }

    private int numPackets;
    private int packetsRead;

    public Mxf436AncTrackReader(InputStream in, Type type, Timecode offset) throws IOException
    {
        super(in);
        this.setOffset(offset);
        this.type = type;
    }

    protected void doRead(OutputStream ancillaryOutputStream) throws IOException
    {
        do {
            int num = readNumPackets();
            for (int i = 0; i < num; i++) {
                readPacket(ancillaryOutputStream);
            }
        } while (true);
    }

    private int readNumPackets() throws IOException
    {
        numPackets = in.readShort();
        packetsRead = 0;
        return numPackets;
    }

    private void readPacket(OutputStream ancillaryOutputStream) throws IOException
    {
        if (packetsRead >= numPackets) {
            throw new IllegalStateException("all packets read");
        }
        final int lineNumber = in.readShort();
//        if (lineNumber == 9) {
//            field += 1;
//        }
        final int wrapType = in.read();
        final int fieldNumber;
        if (wrapType == 0x2 || wrapType == 0x12) {
            fieldNumber = 1;
        } else if (wrapType == 0x3 || wrapType == 0x13) {
            fieldNumber = 2;
        } else {
            fieldNumber = 0; // zero means no field number/packet applies to full frame
        }
        final boolean inHanc = ((wrapType & 0x10) > 0);
        final int sampleCoding = in.read();
        // TODO: support other sample sizes
        if (sampleCoding != 4 && sampleCoding != 5 && sampleCoding != 6
                && sampleCoding != 10 && sampleCoding != 11 && sampleCoding != 12) {
            throw new IOException("only 8-bit samples supported");
        }
        final boolean inChroma;
        if (type == Type.VBI) {
            inChroma = (sampleCoding == 2 || sampleCoding == 3
                    || sampleCoding == 5 || sampleCoding == 6
                    || sampleCoding == 8 || sampleCoding == 9);
        } else { // implies: if (type == Type.ANC)
            inChroma = (sampleCoding == 5 || sampleCoding == 6
                    || sampleCoding == 8 || sampleCoding == 9
                    || sampleCoding == 11 || sampleCoding == 12);
        }
        final int sampleCount = in.readShort();
        final int numElements = in.readInt();
        final int elementSize = in.readInt();
        final int arraySize = numElements * elementSize; // vendors sometimes describe the array as having 1 element but the element size is the number of bytes in the array...
        if (sampleCount > arraySize) { // TODO: revisit when sample coding can be other than 8-bit
            throw new IOException("sampleCount > arraySize");
        }
//        debugSyncRead(sampleCount);
        byte[] buff = new byte[sampleCount];
        in.readFully(buff);
        TrackAttributes trackAttributes = new TrackAttributes(fieldNumber, lineNumber, inHanc, inChroma);
        write(buff, 0, buff.length, trackAttributes);
        for (int i = 0; i < arraySize - sampleCount; i++) {
            in.read(); // throw away extra padding
        }
        packetsRead++;
    }

    /**
     * Debug code to synchronously read the 291 stream
     * @param len length of the samples payload in bytes
     */
    private void debugSyncRead(long len) throws IOException
    {
        long offset = getPosition();
        while (getPosition() - offset < len) {
            Smpte291InputStream smpte291InputStream = createSmpte291InputStream(in);
            AncPacketHeader header = smpte291InputStream.readAncPacket();
            log.debug("did=" + Integer.toHexString(header.getId().getDid())
                    + ",sdid=" + Integer.toHexString(header.getId().getSdid()) + ",len=" + len);
    //        if (header.getDid() == 0x61 && header.getSdid() == 0x01) {
    //            counter++;
    //        }
    //        AuditLogger.getLoggerInstance().sendToBoth("counter=" + counter);
            if (header.getId().getDid() == 0x60 && header.getId().getSdid() == 0x60) {
                AncillaryTimecodePacket atcPacket = (AncillaryTimecodePacket) smpte291InputStream.readAncPacketUserData(header);
                log.debug("ATC timecode=" + atcPacket.getTimecode());
            } else {
                smpte291InputStream.skipAncPacket(header);
            }
        }
    }

    protected Smpte291InputStream createSmpte291InputStream(InputStream inputStream)
    {
        Smpte291InputStream mxfSmpte291InputStream = new Smpte291MxfInputStream(new DataInputStream(inputStream), this);
        return mxfSmpte291InputStream;
    }

    public void run()
    {
        try {
            read();
        } catch (IOException e) {
            log.error("error", e);
        } catch (InterruptedException e) {
            log.error("interrupted", e);
        }
    }
}
