package org.foraci.mxf.mxfReader.io;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Date: May 4, 2011 9:56:39 AM
 *
 * @author Joe Foraci
 */
public class TsOutputStream extends DataOutputStream
{
    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see java.io.FilterOutputStream#out
     */
    public TsOutputStream(OutputStream out) {
        super(out);
    }

    private int cont = 0;
    private final boolean writeM2tsTimecode = false;
    private long start = -1;
    private boolean wroteFirstAdaptFieldV = false;
    private boolean wroteFirstAdaptFieldA = false;
    private boolean wrotePartialAudioFrame = false;
    private boolean wrotePmt = false;

    // tested with Seinfeld_Promo2103640_pp1.mxf
    // works with mplayer, not ffplay...
    public void writeEs(int pid, int pesStreamId, byte[] buf) throws IOException
    {
        int off = 0;
        boolean startOfPes = true;
        boolean writeAdaptation = true;

        while (off < buf.length) {
            int len = 188;
            final int sync = 0x47;
            int flags = (startOfPes) ? 0x2 : 0x0;
            if (start == -1) {
                start = System.currentTimeMillis();
            }
            int pid2 = (flags << 13) | pid;
            int scram = 0;
            int adapt = (writeAdaptation) ? 0x3 : 0x1;
            int w32 = (sync << 24) | (pid2 << 8) | (scram << 6) | (adapt << 4) | cont;
            cont = (cont + 1) % 16;
            // unused experimental adding of timecode to begining for M2TS
            if (writeM2tsTimecode) {
                long ms = System.currentTimeMillis() - start;
                int f = (int)(ms % 1000) % 30;
                ms /= 1000;
                int s = (int)(ms % 60);
                ms /= 60;
                int m = (int)(ms % 60);
                ms /= 60;
                int h = (int)(ms % 24);
//                int tc = ((h / 10) << 28) | ((h % 10) << 24) | ((m / 10) << 20) | ((m % 10) << 16)
//                        | ((s / 10) << 12) | ((s % 10) << 8) | ((f / 10) << 4) | ((f % 10) << 0);
                int tc = (h << 24) | (m << 16) | (s << 8) | f;
                writeInt(tc);
            }
            writeInt(w32);
            len -= 4;
            long ms = (System.currentTimeMillis() - start);
            int ms90 = (int)(ms * 90);
            int ms27 = (int)(ms * 27);
            if (writeAdaptation) {
                writeAdaptation = false;
                write(7);
                if (!wroteFirstAdaptFieldA && pesStreamId < 0xE0) {
                    write(0xD0);
                    wroteFirstAdaptFieldA = true;
                } else if (!wroteFirstAdaptFieldV && pesStreamId >= 0xE0) {
                    write(0xD0);
                    wroteFirstAdaptFieldV = true;
                } else {
                    write(0x10);
                }
                writeInt(ms90 >>> 1); // PTS is on a 90kHz clock (?)
                writeShort(((ms90 & 0x1) << 15) | (ms27 & 0x1FF));
                len -= 8;
            }
            if (startOfPes) {
                startOfPes = false;
                //write PES header
                writeInt((0x1 << 8) | pesStreamId);
                int pesPacketLen = 0;
                if (pesStreamId < 0xE0) {
                    writeInt((pesPacketLen << 16) | 0x84C0);
                } else {
                    writeInt((pesPacketLen << 16) | 0x84C0);
                }
                int pesRemainingPacketLen = 10;
                len -= 19; // including PES header remainging length byte we're about to write
                write(pesRemainingPacketLen);
                // pts
                write((0x3 << 4) | ((ms90 >>> 29) & 0xE) | 1);
                writeShort(((ms90 >>> 14) & 0xFFFE) | 1);
                writeShort(((ms90 << 1) & 0xFFFE) | 1);
                // dts (?)
                write((0x3 << 4) | ((ms90 >>> 29) & 0xE) | 1);
                writeShort(((ms90 >>> 14) & 0xFFFE) | 1);
                writeShort(((ms90 << 1) & 0xFFFE) | 1);
                if (pesStreamId < 0xE0) {
                    write(0xA0); // LPCM!
                    len -= 7;
                    int aframes;
                    if (!wrotePartialAudioFrame) {
                        aframes = (int) Math.ceil(buf.length / 320.0);
                    } else {
                        aframes = (int) Math.ceil(buf.length / 320.0);
                    }
                    int aoffset = 0x00; // TODO: ????
                    write(aframes);write((aoffset >> 8) & 0xFF);write(aoffset & 0xFF);
                    write(0x00);write(0x01);write(0x80);
                }
                //end write PES header
            }
            // write elementary stream
            int have = Math.min(len, buf.length - off);
            if (pesStreamId < 0xE0) { // if audio...
                // write audio elementary stream header
                // write LE as BE ...
                for (int i = 0; i < have; i+=2) {
                    write(buf[i + off + 1]);
                    write(buf[i + off]);
                }
                // end write audio elementary stream header
            } else {
                write(buf, off, have);
            }
            // add stuffing...
            while (len > buf.length - off) {
                write(0xff);
                len--;
            }
            // end write elementary stream
            off += len;
        }
    }
}
