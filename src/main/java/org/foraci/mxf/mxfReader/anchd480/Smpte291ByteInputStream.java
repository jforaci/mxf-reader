package org.foraci.mxf.mxfReader.anchd480;

import org.foraci.anc.anc.*;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * SMPTE 291 reader for samples stored in captured HD 480 data
 *
 * @author jforaci
 */
public class Smpte291ByteInputStream extends Smpte291InputStream
{
    private boolean full = true;

    public Smpte291ByteInputStream(InputStream in, AncTrackReader context) {
        super(in, context);
    }

    public int readWord() throws IOException
    {
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
            // throw extra unknown bytes away for now...
            readWord();
            readWord();
            readWord();
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
            // throw extra unknown bytes away for now...
            readWord();
            readWord();
            readWord();
            header.setChecksum(checkSumValue);
        }
        return new AncPacketRawUserData(words);
    }
}
