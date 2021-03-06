package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Sep 25, 2009 12:17:38 PM
 *
 * @author jforaci
 */
public class ISO88591StringParser extends Parser {
    public ISO88591StringParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public String read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        byte[] bytes = new byte[length.intValue()]; // TODO fix possible problems here with large strings
        in.readFully(bytes);
        count = length;
        return new String(bytes, "ISO8859-1");
    }
}
