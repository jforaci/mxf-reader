package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.BatchHeader;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Date: Sep 22, 2009 2:55:59 PM
 *
 * @author jforaci
 */
public abstract class BatchParser extends Parser {
    protected BatchHeader header = null;
    protected long pos = 0;

    public BatchParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public Object read() throws IOException {
        if (header == null) {
            readHeader(in);
        }
        if (pos == header.getSize()) {
            return null;
        }
        pos++;
        return readElement(in);
    }

    public BatchHeader readHeader(MxfInputStream in) throws IOException {
        header = in.readBatch();
        count = count.add(BigInteger.valueOf(8));
        return header;
    }

    public abstract Object readElement(MxfInputStream in) throws IOException;
}
