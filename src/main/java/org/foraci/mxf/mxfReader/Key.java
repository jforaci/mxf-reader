package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.parsers.Parser;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 
 * @author jforaci
 */
public class Key {
    protected final UL ul;
    protected final BigInteger length;

    public Key(UL ul, BigInteger length) {
        this.ul = ul;
        this.length = length;
    }

    public UL getUL() {
        return ul;
    }

    public BigInteger getLength() {
        return length;
    }

    Parser parser(MxfInputStream in) throws IOException {
        return ul.parser(length, in);
    }

    public String toString() {
        return ul.toString() + ", len=" + length;
    }
}
