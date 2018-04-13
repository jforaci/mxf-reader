package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.parsers.Parser;
import org.foraci.mxf.mxfReader.parsers.NumericParser;

import java.math.BigInteger;

/**
 * A UL with a numeric value; returns a <code>NumericParser</code>
 * @author jforaci
 */
public class NumericUL extends UL {
    private final boolean signed;
    private final int size;

    public NumericUL(String name, byte[] value, boolean signed, int size) {
        super(name, value);
        this.signed = signed;
        this.size = size;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getSize() {
        return size;
    }

    @Override
    public Class getParserClass() {
        return NumericParser.class;
    }

    @Override
    protected Parser parser(BigInteger length, MxfInputStream in) {
        return new NumericParser(length, in, signed, size);
    }
}
