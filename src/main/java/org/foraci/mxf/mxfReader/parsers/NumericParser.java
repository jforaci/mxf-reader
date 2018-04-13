package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;

import java.math.BigInteger;
import java.io.IOException;

/**
 * A general numeric parser that returns instances of <code>Number</code>
 * @author jforaci
 */
public class NumericParser extends Parser {
    private final boolean signed;
    private final int size;

    public NumericParser(BigInteger length, MxfInputStream in, boolean signed, int size) {
        super(length, in);
        this.signed = signed;
        this.size = size;
    }

    public Number read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        BigInteger i = in.readBigInt(signed, size);
        count = count.add(BigInteger.valueOf(size));
        return i;
    }
}
