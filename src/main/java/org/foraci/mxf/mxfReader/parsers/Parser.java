package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;
import java.util.LinkedList;
import java.util.List;

/**
 * A default parser for a KLV's value
 * @author jforaci
 */
public class Parser implements Cloneable {
    protected final BigInteger length;
    protected final MxfInputStream in;
    protected BigInteger count = BigInteger.ZERO;

    public Parser(BigInteger length, MxfInputStream in) {
        this.length = length;
        this.in = in;
    }

    public Object read() throws IOException {
        skip();
        return null;
    }

    public List readAll() throws IOException {
        LinkedList v = new LinkedList();
        Object o;
        while ((o = read()) != null) {
            v.add(o);
        }
        return v;
    }

    public void skip() throws IOException {
        BigInteger length = this.length.subtract(this.count);
        in.skip(length);
    }

    protected final Parser clone() {
        try {
            return (Parser)super.clone();
        } catch (CloneNotSupportedException e) {
            in.error("unable to create parser");
            return null;
        }
    }
}
