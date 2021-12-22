package org.foraci.mxf.mxfReader.parsers;

import org.foraci.mxf.mxfReader.MxfInputStream;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class FrameLayoutParser extends EnumLutParser {
    private static final Map<Integer, String> lut;

    static {
        lut = new HashMap<>();
        lut.put(0, "Full frame");
        lut.put(1, "Separate fields");
        lut.put(2, "Single field");
        lut.put(3, "Mixed fields");
        lut.put(4, "Segmented frame");
    }

    public FrameLayoutParser(BigInteger length, MxfInputStream in) {
        super(length, in, lut);
    }
}
