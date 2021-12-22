package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.entities.Rational;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * Parser returns a string based on a supplied lookup table
 *
 * @author jforaci
 */
public abstract class EnumLutParser extends Parser {
    private final Map<Integer, String> lut;

    public EnumLutParser(BigInteger length, MxfInputStream in, Map<Integer, String> lut) {
        super(length, in);
        this.lut = lut;
    }

    public String read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        int n = in.read();
        count = count.add(BigInteger.ONE);
        return (!lut.containsKey(n)) ? "?" : String.valueOf(lut.get(n));
    }
}
