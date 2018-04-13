package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.UMID;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Sep 24, 2009 4:17:10 PM
 *
 * @author jforaci
 */
public class UMIDParser extends Parser {
    public UMIDParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public UMID read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        byte[] bytes = new byte[32];
        in.readFully(bytes);
        count = count.add(BigInteger.valueOf(bytes.length));
        return new UMID(bytes);
    }
}
