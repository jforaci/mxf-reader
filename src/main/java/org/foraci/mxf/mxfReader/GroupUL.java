package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.parsers.Parser;
import org.foraci.mxf.mxfReader.parsers.KlvParser;
import org.foraci.mxf.mxfReader.parsers.LocalSetParser;

import java.math.BigInteger;

/**
 * A UL for a set or pack
 * @author jforaci
 */
public class GroupUL extends UL {
    public static final int UNKNOWN = 0;
    public static final int UNIVERSAL_SET = 1;
    public static final int GLOBAL_SET = 2;
    public static final int LOCAL_SET = 3;
    public static final int VARIABLE_LENGTH_PACK = 4;
    public static final int FIXED_LENGTH_PACK = 5;

    public static final int LENGTH_TYPE_UNKNOWN = -1;
    public static final int LENGTH_TYPE_BER = 0;

    public static final int TAG_LENGTH_TYPE_UNKNOWN = -1;

    private final int type;
    private final int lengthType;
    private final int localSetTagLength;

    private GroupUL(String name, byte[] value, int type, int lengthType, int localSetTagLength) {
        super(name, value);
        this.type = type;
        this.lengthType = lengthType;
        this.localSetTagLength = localSetTagLength;
    }

    public static GroupUL match(String name, byte[] key) {
        if (key[4] != 0x02) { // not a set or pack
            return null;
        }
        int lengthType;
        // Universal Sets
        if (key[5] == 0x01) {
            if (name == null) {
                name = "Universal Set";
            }
            return new GroupUL(name, key, GroupUL.UNIVERSAL_SET, 0, 16);
        }
        // Global Sets
        if (key[5] == 0x02 || key[5] == 0x22 || key[5] == 0x42 || key[5] == 0x62) {
            if (key[5] == 0x22) {
                lengthType = 1;
            } else if (key[5] == 0x42) {
                lengthType = 2;
            } else if (key[5] == 0x62) {
                lengthType = 4;
            } else { // key[5] == 0x02
                lengthType = LENGTH_TYPE_BER;
            }
            if (name == null) {
                name = "Global Set";
            }
            return new GroupUL(name, key, GroupUL.GLOBAL_SET, lengthType, TAG_LENGTH_TYPE_UNKNOWN);
        }
        // Local Sets
        if (key[5] == 0x03 || key[5] == 0x13 || key[5] == 0x1B
                || key[5] == 0x23 || key[5] == 0x33 || key[5] == 0x3B
                || key[5] == 0x43 || key[5] == 0x53 || key[5] == 0x5B
                || key[5] == 0x63 || key[5] == 0x73 || key[5] == 0x7B) {
            if (key[5] <= 0x1B) {
                lengthType = LENGTH_TYPE_BER;
            } else if (key[5] <= 0x3B) {
                lengthType = 1;
            } else if (key[5] <= 0x5B) {
                lengthType = 2;
            } else if (key[5] <= 0x7B) {
                lengthType = 4;
            } else {
                throw new RuntimeException("illegal Local Set Registry Designator (byte 6) value (lengthType): " + key[5]);
            }
            int localSetTagLength;
            int uppermod2 = ((key[5] >> 4) & 0xF) % 2;
            int lower = key[5] & 0xF;
            if (uppermod2 % 2 == 0) {
                if (lower == 0x3) {
                    localSetTagLength = 1;
                } else { // implies: (lower == 0xB)
                    // ASN.1 OID BER encoded tag is unsupported and will not fall through to
                    // here because the outer "if" that checks "key[5]" skips it, but let's
                    // put something here to shut compiler up about uninitialized values
                    throw new RuntimeException("unsupported Local Set Registry Designator (byte 6) value (localSetTagLength): " + key[5]);
                }
            } else {
                if (lower == 0x3) {
                    localSetTagLength = 2;
                } else { // implies: (lower == 0xB)
                    localSetTagLength = 4;
                }
            }
            if (name == null) {
                name = "Local Set";
            }
            return new GroupUL(name, key, GroupUL.LOCAL_SET, lengthType, localSetTagLength);
        }
        // variable-length packs
        if (key[5] == 0x04 || key[5] == 0x24 || key[5] == 0x44 || key[5] == 0x64) {
            if (key[5] == 0x24) {
                lengthType = 1;
            } else if (key[5] == 0x44) {
                lengthType = 2;
            } else if (key[5] == 0x64) {
                lengthType = 4;
            } else { // key[5] == 0x04
                lengthType = LENGTH_TYPE_BER;
            }
            if (name == null) {
                name = "Variable-Len Pack";
            }
            return new GroupUL(name, key, GroupUL.VARIABLE_LENGTH_PACK, lengthType, 0);
        }
        // fixed-length packs
        if (key[5] == 0x05) {
            if (name == null) {
                name = "Fixed-Len Pack";
            }
            return new GroupUL(name, key, GroupUL.FIXED_LENGTH_PACK, LENGTH_TYPE_UNKNOWN, 0);
        }
        if (name == null) {
            name = "<Unknown Set/Pack>";
        }
        return new GroupUL(name, key, GroupUL.UNKNOWN, LENGTH_TYPE_UNKNOWN, 0);
    }

    @Override
    Parser parser(BigInteger length, MxfInputStream in) {
        if (getParserClass() != null) {
            return super.parser(length, in);
        }
        // Universal Sets
        if (isUniversalSet()) {
            return new KlvParser(length, in);
        }
        // Global Sets
        if (isGlobalSet()) {
            // return new GlobalSetParser(length, in, lengthType);
        }
        // Local Sets
        if (isLocalSet()) {
            return new LocalSetParser(length, in, lengthType, localSetTagLength);
        }
        // variable-length packs
        if (isVariableLengthPack()) {
            // return new VariableLengthParser(length, in, lengthType);
        }
        // fixed-length packs
        if (isFixedLengthPack()) {
            // no way to decode length; it is defined by a specific UL
            return super.parser(length, in);
        }
        return new Parser(length, in); // give up and return the default parser
    }

    @Override
    public boolean isSetOrPack() {
        return true;
    }

    @Override
    public boolean isUnknownGroup() {
        return (type == UNKNOWN);
    }

    @Override
    public boolean isUniversalSet() {
        return (type == UNIVERSAL_SET);
    }

    @Override
    public boolean isGlobalSet() {
        return (type == GLOBAL_SET);
    }

    @Override
    public boolean isLocalSet() {
        return (type == LOCAL_SET);
    }

    @Override
    public boolean isVariableLengthPack() {
        return (type == VARIABLE_LENGTH_PACK);
    }

    @Override
    public boolean isFixedLengthPack() {
        return (type == FIXED_LENGTH_PACK);
    }
}
