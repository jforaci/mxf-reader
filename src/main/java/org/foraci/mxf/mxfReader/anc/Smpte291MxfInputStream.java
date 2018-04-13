package org.foraci.mxf.mxfReader.anc;

import org.foraci.anc.anc.*;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * SMPTE 291 reader for samples stored in a MXF SMPTE 436m track
 *
 * @author jforaci
 */
public class Smpte291MxfInputStream extends Smpte291InputStream
{
    private boolean full = false; // some sample depths require a checksum word (for pass-thru style of 436m encoding)

    public Smpte291MxfInputStream(InputStream in, AncTrackReader context) {
        super(in, context);
    }

    public int readWord() throws IOException {
        int word = in.read();
        if (word == -1) {
            throw new EOFException();
        }
        return word;
    }

    public AncPacketHeader readAncPacket() throws IOException
    {
        int did = readWord();
        int sdid = readWord();
        int dataCount = readWord();
        return createAncPacketHeader(did, sdid, dataCount);
    }

    public void skipAncPacket(AncPacketHeader header) throws IOException
    {
        header.skip(this);
        if (full) {
            int checkSumValue = readWord();
            header.setChecksum(checkSumValue);
        }
    }

    public AncPacketUserData readAncPacketUserData(AncPacketHeader header) throws IOException
    {
        AncPacketUserData data = header.read(this, context);
        if (full) {
            int checkSumValue = readWord();
            header.setChecksum(checkSumValue);
        }
        return data;
    }

    public AncPacketRawUserData readAncPacketRawUserData(AncPacketHeader header) throws IOException
    {
        int[] words = new int[header.getDataCount()];
        for (int i = 0; i < header.getDataCount(); i++) {
            words[i] = readWord();
        }
        if (full) {
            int checkSumValue = readWord();
            header.setChecksum(checkSumValue);
        }
        return new AncPacketRawUserData(words);
    }
}
