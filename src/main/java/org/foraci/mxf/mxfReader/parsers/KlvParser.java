package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.Key;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Parser to read KLV's, mainly used for Universal Sets
 * @author jforaci
 */
public class KlvParser extends Parser implements SetParser
{
    public KlvParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public Key read() throws IOException {
        if (count.compareTo(length) >= 0) {
            return null;
        }
        Key key = in.readKey();
        count = count.add(key.getLength());
        return key;
    }
}
