package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.UID;

import java.math.BigInteger;
import java.io.IOException;

/**
 * Date: Sep 25, 2009 2:04:19 PM
 *
 * @author jforaci
 */
public class UIDBatchParser extends BatchParser {
    public UIDBatchParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public UID read() throws IOException {
        return (UID)super.read();
    }

    public UID readElement(MxfInputStream in) throws IOException {
        byte[] bytes = new byte[16];
        in.readFully(bytes);
        count = count.add(BigInteger.valueOf(bytes.length));
        return new UID(bytes);
    }
}
