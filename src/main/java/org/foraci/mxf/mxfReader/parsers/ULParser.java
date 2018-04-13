package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.UL;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Sep 25, 2009 2:52:39 PM
 *
 * @author jforaci
 */
public class ULParser extends Parser {
    public ULParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public UL read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        count = count.add(BigInteger.valueOf(16));
        return in.readUL();
    }
}
