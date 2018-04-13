package org.foraci.mxf.mxfReader.util.io;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * A input stream filter that keeps track of its position
 *
 * @author jforaci
 */
public class CountingInputStream extends FilterInputStream
{
    private long pos = 0;
    private long mark = 0;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    /**
     * Get the stream position. Eventually, the position will roll over.
     * Reading 1 Tb per second, this would occur after approximately three
     * months. Applications should account for this possibility in their
     * design.
     *
     * @return the current stream position.
     */
    public long getPosition() {
        return pos;
    }

    public int read()
            throws IOException
    {
        int b = super.read();
        if (b >= 0)
            pos += 1;
        return b;
    }

    public int read(byte[] b, int off, int len)
            throws IOException {
        int n = super.read(b, off, len);
        if (n > 0)
            pos += n;
        return n;
    }

    public long skip(long skip)
            throws IOException {
        long n = super.skip(skip);
        if (n > 0)
            pos += n;
        return n;
    }

    public void mark(int readlimit) {
        super.mark(readlimit);
        mark = pos;
    }

    public void reset()
            throws IOException {
        if (!markSupported())
            throw new IOException("mark is not supported by underlying stream");
        super.reset();
        pos = mark;
    }
}
