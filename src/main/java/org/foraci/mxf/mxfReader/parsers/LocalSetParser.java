package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.*;

import java.io.IOException;
import java.math.BigInteger;

/**
 * A Local Set parser that uses the <code>MxfInputStream</code>'s currently set
 * <code>primerPack</code> look-up table to translate the local tags to full (16 byte) UL's
 * @author jforaci
 */
public class LocalSetParser extends Parser implements SetParser
{
    private final int lengthType;
    private final int tagLength;

    public LocalSetParser(BigInteger length, MxfInputStream in,
            int lengthType, int tagLength) {
        super(length, in);
        this.lengthType = lengthType;
        this.tagLength = tagLength;
    }

    public Key read() throws IOException {
        if (!in.isPrimerPackDefined()) {
            throw new IOException("no primer pack defined");
        }
        do {
            if (count.compareTo(length) >= 0) {
                return null;
            }
            LocalTag tag = in.readLocalTag(tagLength);
            BigInteger valueLength;
            BigInteger lengthLength;
            if (lengthType == GroupUL.LENGTH_TYPE_BER) {
                BigInteger[] pair = readBerLength();
                valueLength = pair[0];
                lengthLength = pair[1];
            } else {
                valueLength = in.readBigInt(false, lengthType);
                lengthLength = BigInteger.valueOf(lengthType);
            }
            count = count.add(BigInteger.valueOf(tagLength))
                    .add(lengthLength).add(valueLength);
            UL ul = in.getPrimerEntry(tag);
            if (ul == null) {
                in.warn("unknown local tag, ignoring: " + tag);
                in.skip(valueLength);
                continue; // read next
            }
            return new Key(ul, valueLength);
        } while (true);
    }

    private BigInteger[] readBerLength() throws IOException {
        final int hibitMask = 0x80;
        final int lengthMask = 0x7F;
        int b = in.read();
        if (b == -1) {
            throw new IOException("could not read BER length");
        }
        int len = b & lengthMask;
        if ((b & hibitMask) == 0) { // len holds the length value
            return new BigInteger[] { BigInteger.valueOf(len), BigInteger.ONE };
        }
        // len actually holds the number of bytes of a big-endian, unsigned integer which in
        // turn holds the actual length value
        if (len == 0) { // a count of 0 bytes to hold a length value is illegal
            throw new IOException("illegal 1-byte BER length of 80h");
        }
        return new BigInteger[] { in.readBigInt(false, len), BigInteger.valueOf(len + 1) };
    }
}
