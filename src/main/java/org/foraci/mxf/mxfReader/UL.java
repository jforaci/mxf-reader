package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.parsers.Parser;
import org.foraci.mxf.mxfReader.parsers.factory.ParserFactory;
import org.foraci.mxf.mxfReader.parsers.factory.UnknownValueParserFactory;

import java.math.BigInteger;
import java.lang.reflect.Constructor;

/**
 * A Universal Label as defined in SMPTE 298M and referenced in SMPTE 336M
 * @author jforaci
 */
public class UL {
    private static final int PICTURE_ELEMENT = 10;
    private static final int SOUND_ELEMENT = 11;
    private static final int DATA_ELEMENT = 12;
    private static final int COMPOUND_ELEMENT = 13;

    private static final ParserFactory defaultParserFactory = new UnknownValueParserFactory();
    private static ParserFactory parserFactory = null;

    public static ParserFactory getParserFactory() {
        return parserFactory;
    }

    public static void setParserFactory(ParserFactory parserFactory) {
        UL.parserFactory = parserFactory;
    }

    protected byte[] key;
    protected String name;
    private Class parserClass;
    private int type = 0;
    private boolean gcSystemItem = false, gcEssenceItem = false;
    private String description;

    public UL(String name, byte[] value) {
        this(name, value, null);
    }

    public UL(String name, byte[] value, Class parserClass) {
        if (value == null || value.length != 16) {
            throw new IllegalArgumentException("invalid UL key");
        }
        this.key = value;
        matchKey();
        if (name != null) {
            this.name = name;
        }
        if (parserClass != null) {
            this.parserClass = parserClass;
        }
    }

    private void matchKey() {
        gcSystemItem = matchGenericContainerSystemItem();
        gcEssenceItem = matchGenericContainerEssenceElement();
    }

    public byte[] getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Class getParserClass() {
        return parserClass;
    }

    public void setParserClass(Class parserClass) {
        this.parserClass = parserClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    Key createKey(BigInteger length) {
        return new Key(this, length);
    }

    Parser parser(BigInteger length, MxfInputStream in) {
        if (getParserClass() != null) {
            Class[] classes = { BigInteger.class, MxfInputStream.class };
            try {
                Constructor constructor = parserClass.getConstructor(classes);
                Object[] params = { length, in };
                return (Parser)constructor.newInstance(params);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("could not instantiate parser", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (getParserFactory() != null) {
            return getParserFactory().createParser(length, in);
        }
        return defaultParserFactory.createParser(length, in);
    }

    public boolean isStrongReference() {
        return (key[4] == 0x01 && key[5] == 0x01 && key[6] == 0x01 /* && key[7] == 0x02 // leave out the version */
                && key[8] == 0x06 && key[9] == 0x01 && key[10] == 0x01 && key[11] == 0x04
                && (key[12] == 0x02 || key[12] == 0x05 || key[12] == 0x06));
    }

    /**
     * Matches whether upper 4 bytes of 8 byte item designator are for the MXF Generic Container
     * @return <code>true</code> if this UL's key is for a GC item element
     */
    private boolean isGcKey() {
        return (key[8] == 0x0D && key[9] == 0x01 && key[10] == 0x03 && key[11] == 0x01);
    }

    private boolean matchGenericContainerSystemItem() {
        if (!isSetOrPack() || !isGcKey()) {
            return false;
        }
        if (key[12] == 0x04 || key[12] == 0x14) {
            if (key[12] == 0x04) {
                name = "CP-Compatible System Elem";
            }
            if (key[12] == 0x14) {
                name = "GC-Compatible System Elem";
            }
            if (key[14] == 0x01) {
                name = name + " (FIRST)";
            }
            return true;
        }
        return false;
    }

    private boolean matchGenericContainerEssenceElement() {
        if (!isGcKey()) {
            return false;
        }
        if (key[4] == 0x01 && key[5] == 0x02) {
            if (key[12] == 0x05 || key[12] == 0x15) {
                name = "Picture Elem";
                type = PICTURE_ELEMENT;
            } else if (key[12] == 0x06 || key[12] == 0x16) {
                name = "Sound Elem";
                type = SOUND_ELEMENT;
            } else if (key[12] == 0x07 || key[12] == 0x17) {
                name = "Data Elem";
                type = DATA_ELEMENT;
            } else if (key[12] == 0x18) {
                name = "Compound Elem";
                type = COMPOUND_ELEMENT;
            }
            return true;
        }
        return false;
    }

    public boolean isGcSystemElement() {
        return gcSystemItem;
    }

    public boolean isFirstGcSystemElementInCp() {
        return (isGcSystemElement() && key[14] == 0x01);
    }

    public boolean isGcEssenceElement() {
        return gcEssenceItem;
    }

    public long getTrackNumber() {
        if (!isGcEssenceElement()) {
            return -1;
        }
        return ((key[12] & 0xFF) << 24) | ((key[13] & 0xFF) << 16) | ((key[14] & 0xFF) << 8) | (key[15] & 0xFF);
    }

    public boolean isPictureEssence() {
        return (type == PICTURE_ELEMENT);
    }

    public boolean isSoundEssence() {
        return (type == SOUND_ELEMENT);
    }

    public boolean isDataEssence() {
        return (type == DATA_ELEMENT);
    }

    public boolean isCompoundEssence() {
        return (type == COMPOUND_ELEMENT);
    }

    public boolean isSetOrPack() {
        return false;
    }

    public boolean isUnknownGroup() {
        return false;
    }

    public boolean isUniversalSet() {
        return false;
    }

    public boolean isGlobalSet() {
        return false;
    }

    public boolean isLocalSet() {
        return false;
    }

    public boolean isVariableLengthPack() {
        return false;
    }

    public boolean isFixedLengthPack() {
        return false;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + key[4];
        result = 31 * result + key[5];
        for (int i = 8; i < 16; i++) {
            result = 31 * result + key[i];
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof UL)) {
            return false;
        }
        UL other = (UL)obj;
        return (key[4] == other.key[4]
                && key[5] == other.key[5]
                && key[8] == other.key[8]
                && key[9] == other.key[9]
                && key[10] == other.key[10]
                && key[11] == other.key[11]
                && key[12] == other.key[12]
                && key[13] == other.key[13]
                && key[14] == other.key[14]
                && key[15] == other.key[15]);
    }

    public String toString() {
        return name + ": " + Utils.bytesToString(key);
    }
}
