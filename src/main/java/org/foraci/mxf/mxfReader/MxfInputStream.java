package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.util.io.CountingInputStream;
import org.foraci.mxf.mxfReader.registries.Registry;
import org.foraci.mxf.mxfReader.entities.PrimerPackEntry;
import org.foraci.mxf.mxfReader.parsers.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

/**
 * Reads a stream of MXF data
 * @author jforaci
 */
public class MxfInputStream extends DataInputStream {
    private static final Logger log = LoggerFactory.getLogger(MxfInputStream.class);
    private static final int MAX_RUNIN_LENGTH = 65536;

    private boolean enableRuninAnywhere = false;
    private long lastKeyOffset = 0;
    private Map primer = null;
    private final Registry registry;
    private int runInLength;
    private final long streamOffset;
    private boolean debug = false;

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     * @param registry The metadata registry to use
     */
    public MxfInputStream(InputStream in, Registry registry) {
        this(new CountingInputStream(in), registry, 0);
    }

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     * @param registry The metadata registry to use
     * @param streamOffset The offset at which the byte-counter for the stream starts
     */
    MxfInputStream(InputStream in, Registry registry, long streamOffset) {
        super(new CountingInputStream(in));
        this.registry = registry;
        this.streamOffset = streamOffset;
        this.runInLength = 0;
    }

    /**
     * Creates a new <code>MxfInputStream</code> shifted
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     * @param registry The metadata registry to use
     * @param offset The relative offset from the first byte of the first partition pack key
     */
    MxfInputStream offset(InputStream in, Registry registry, long offset) {
        MxfInputStream newStream = new MxfInputStream(
                new CountingInputStream(in), registry, offset + this.runInLength);
        newStream.runInLength = this.runInLength;
        return newStream;
    }

    public boolean isEnableRuninAnywhere() {
        return enableRuninAnywhere;
    }

    public void setEnableRuninAnywhere(boolean enableRuninAnywhere) {
        this.enableRuninAnywhere = enableRuninAnywhere;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Reads a key prefix (used for the partition pack's key), seeking past the optional run-in
     */
    private void readKeyPrefix(byte[] prefix) throws IOException {
        int b;
        int counter = 0;
        int prefixMatchOffset = 0;
        while ((b = read()) != -1) {
            counter++;
            if (b == prefix[prefixMatchOffset]) {
                prefixMatchOffset++;
                if (prefixMatchOffset == prefix.length) {
                    // we found the first bytes of the key prefix
                    runInLength = counter - prefix.length;
                    break;
                }
            } else {
                if (counter > MAX_RUNIN_LENGTH) {
                    error("no partition pack found, max run-in length exceeded");
                    throw new IOException("No Partition Pack found, max run-in length exceeded");
                }
                if (b != prefix[0]) {
                    prefixMatchOffset = 0;
                } else {
                    prefixMatchOffset = 1;
                }
            }
        }
        if (b == -1) {
            error("no partition pack found, eof");
            throw new IOException("No Partition Pack found, EOF");
        }
    }

    public BatchHeader readBatch() throws IOException {
        long size = readUInt();
        long elementSize = readUInt();
        return new BatchHeader(size, elementSize);
    }

    public UL readUL() throws IOException {
        byte[] key = new byte[16];
        readFully(key);
        UL ul = registry.lookup(key);
        String name = null;
        if (ul != null) {
            name = ul.getName();
        }
        return new UL(name, key);
    }

    public Key readKey() throws IOException {
        lastKeyOffset = getOffset();
        byte[] key = new byte[16];
        if (streamOffset == 0 && lastKeyOffset == 0) {
            byte[] prefix = PartitionPackUL.PARTITION_KEY_PREFIX;
            readKeyPrefix(prefix);
            System.arraycopy(prefix, 0, key, 0, prefix.length);
            readFully(key, prefix.length, key.length - prefix.length);
        } else {
            if (enableRuninAnywhere) {
                // this is a quick hack to get around some bad files from Radiant Grid
                // where there are sometimes null bands inserted between partitions...
                int b;
                while (true) {
                    b = check(read());
                    if (b == 0) continue;
                    break;
                }
                for (int i = 0; i < key.length; i++) {
                    key[i] = (byte)b;
                    if (i<key.length-1) b = check(read());
                }
            } else {
                readFully(key);
            }
        }
        BigInteger length = readBerLength();
        // lookup a known UL (e.g. metadata, special sets/packs, etc)
        UL ul = registry.lookup(key);
        if (ul != null) {
            checkKey(ul);
            return ul.createKey(length);
        }
        // look up a partial key of a known UL
        PartialUL partialUL = registry.lookupPartial(key);
        if (partialUL != null) {
            ul = new UL(null, key);
            return partialUL.createKey(ul, length);
        }
        // match info in the key, if any, to create a key with a general UL
        ul = matchSpecialKey(key);
        checkKey(ul);
        return ul.createKey(length);
    }

    public LocalTag readLocalTag(int tagLength) throws IOException {
        byte[] localTag = new byte[tagLength];
        lastKeyOffset = getOffset();
        readFully(localTag);
        LocalTag tag = new LocalTag(localTag);
        return tag;
    }

    public long getLastKeyOffset() {
        return lastKeyOffset;
    }

    public long getLastKeyOffsetInStream() {
        return lastKeyOffset + runInLength;
    }

    private UL matchSpecialKey(byte[] key) {
        UL ul;
        if ((ul = GroupUL.match(null, key)) != null) {
            return ul;
        }
        return new UL(null, key);
    }

    private boolean checkKey(UL ul) {
        if (ul.isGlobalSet()) {
            // TODO even though MXF forbids Global Sets we should probably support them anyway
            warn("found a Global Set key (unsupported in MXF): " + ul);
        }
        if (ul.isUnknownGroup()) {
            warn("unknown set/pack: " + ul);
        }
        return true;
    }

    public Parser parser(Key key) throws IOException {
        return key.parser(this);
    }

    public BigInteger readBerLength() throws IOException {
        final int hibitMask = 0x80;
        final int lengthMask = 0x7F;
        int b = check(read());
        int len = b & lengthMask;
        if ((b & hibitMask) == 0) { // len holds the length value
            return BigInteger.valueOf(len);
        }
        // len actually holds the number of bytes of a big-endian, unsigned integer which in
        // turn holds the actual length value
        if (len == 0) { // a count of 0 bytes to hold a length value is illegal
            throw new IOException("illegal 1-byte BER length of 80h");
        } else if (len > 8) { // byte read + bytes to read > 9
            warn("illegal greater than 9-byte BER length");
        }
        return readBigInt(false, len);
    }

    public long readUInt() throws IOException {
        int i = readInt();
        long value = i & 0xFFFFFFFFL;
        return value;
    }

    public BigInteger readULong() throws IOException {
        byte[] longBytes = new byte[8];
        int count = 0;
        do {
            count += check(read(longBytes, count, longBytes.length - count));
        } while (count < longBytes.length);
        return new BigInteger(1, longBytes);
    }

    public BigInteger readBigInt(boolean signed, int len) throws IOException {
        if (len <= 0) {
            throw new IllegalArgumentException("readUBigInt: bad length");
        }
        int b;
        ByteArrayOutputStream accum = new ByteArrayOutputStream(4);
        int sign = 1;
        boolean checkSign = signed;
        while (len > 0) {
            b = check(read());
            if (checkSign) {
                if (b < 0) {
                    sign = -1;
                }
                checkSign = false;
            }
            accum.write(b);
            len--;
        }
        return new BigInteger(sign, accum.toByteArray());
    }

    protected long getOffset() {
        return ((CountingInputStream)this.in).getPosition() + streamOffset - runInLength;
    }

    protected long getStreamOffset() {
        return ((CountingInputStream)this.in).getPosition() + streamOffset;
    }

    public int getRunInLength() {
        return runInLength;
    }

    private int check(int i) throws IOException {
        if (i == -1) {
            throw new IOException();
        }
        return i;
    }

    private long check(long i) throws IOException {
        if (i == -1) {
            throw new IOException();
        }
        return i;
    }

    public void info(String message) {
        log.info(message);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void error(String message) {
        log.error(message);
    }

    public void error(Throwable throwable) {
        log.error("error", throwable);
    }

    public void clearPrimerPack() {
        primer = null;
    }

    public boolean isPrimerPackDefined() {
        return (primer != null);
    }

    public void setPrimerPack(Vector primerPack) {
        primer = new HashMap();
        for (int i = 0; i < primerPack.size(); i++) {
            PrimerPackEntry entry = (PrimerPackEntry) primerPack.elementAt(i);
            primer.put(entry.getLocalTag(), entry.getUL());
        }
    }

    public UL getPrimerEntry(LocalTag localTag) {
        UL ul = (UL) primer.get(localTag);
        if (ul == null) {
            return null;
        }
        UL known = registry.lookup(ul.getKey());
        if (known != null) {
            ul = known;
        }
        return ul;
    }

    public void skip(BigInteger length) throws IOException {
        do {
            long s;
            if (length.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                s = Long.MAX_VALUE;
                length = length.subtract(BigInteger.valueOf(s));
            } else {
                s = length.longValue();
                length = BigInteger.ZERO;
            }
            do {
                s -= skip(s);
            } while (s > 0);
        } while (length.compareTo(BigInteger.ZERO) > 0);
    }
}
