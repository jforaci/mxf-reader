package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.LocalTag;
import org.foraci.mxf.mxfReader.entities.PrimerPackEntry;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Date: Sep 22, 2009 2:36:24 PM
 *
 * @author jforaci
 */
public class PrimerPackParser extends BatchParser {
    public PrimerPackParser(BigInteger length, MxfInputStream in) {
        super(length, in);
    }

    public PrimerPackEntry read() throws IOException {
        return (PrimerPackEntry)super.read();
    }

    public PrimerPackEntry readElement(MxfInputStream in) throws IOException {
        byte[] localTag = new byte[2];
        in.readFully(localTag);
        LocalTag tag = new LocalTag(localTag);
        UL ul = in.readUL();
        count = count.add(BigInteger.valueOf(localTag.length + 16));
        return new PrimerPackEntry(tag, ul);
    }
}
