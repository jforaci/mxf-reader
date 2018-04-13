package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.UL;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Sep 25, 2009 2:04:19 PM
 *
 * @author jforaci
 */
public class ULBatchParser extends BatchParser {
    public ULBatchParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public UL read() throws IOException {
        return (UL)super.read();
    }

    public UL readElement(MxfInputStream in) throws IOException {
        count = count.add(BigInteger.valueOf(16));
        return in.readUL();
    }
}
