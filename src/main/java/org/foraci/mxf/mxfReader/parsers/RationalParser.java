package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.entities.Rational;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Oct 2, 2009 5:23:32 PM
 *
 * @author jforaci
 */
public class RationalParser extends Parser {
    public RationalParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public Rational read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        int n = in.readInt();
        int d = in.readInt();
        count = count.add(BigInteger.valueOf(8));
        return new Rational(n, d);
    }
}
