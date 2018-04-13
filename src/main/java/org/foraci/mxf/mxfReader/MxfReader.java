package org.foraci.mxf.mxfReader;

import org.foraci.mxf.mxfReader.registries.Registry;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.mxf.mxfReader.entities.Package;
import org.foraci.mxf.mxfReader.entities.*;
import org.foraci.mxf.mxfReader.parsers.*;
import org.foraci.mxf.mxfReader.io.TsOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * A parser for an <code>MxfInputStream</code>
 * @author jforaci
 */
public class MxfReader {
    static {
        Registry.initialize();
    }

    private static final Logger log = LoggerFactory.getLogger(MxfReader.class);

    private enum ExpectedData {
        NonHeaderPartition,
        HeaderPartition,
        BodyPartition,
        FooterPartition,
        PrimerPack,
        HeaderData,
        PartitionData,
        IndexData,
        RIP
    }

    public enum MetadataReadMode {
        All,
        FromFooterAll,
        HeaderOnly,
        FooterOnly,
    }

    private final File file;
    private boolean debugPrint = false;
    private MetadataReadMode metadataReadMode = MetadataReadMode.HeaderOnly;
    private boolean useRip = true;
    private boolean parseEssenceElements = false;
    private boolean parseSystemElements = false;
    private OutputStream essenceOutputStream = null;
    private boolean enableRuninAnywhere = false;

    protected MxfInputStream in;
    protected EssenceContainerOutputController fout;
    private PartitionPackManifest partitionPacks;
    private RandomIndexPack randomIndexPack;
    private RipEntry lastRipEntry = null;
    private boolean rewind;
    protected ExpectedData expected;
    private Map addressables;
    private boolean resolvedAddressables;
    protected int level = 1;
    private Map ecdMap;
    private Registry registry;
    private static final int ESSENCE_DUMP_BUFF_SIZE = 256 * 1024;
    private byte[] essenceDumpBuff = null;

    public MxfReader(File file) {
        this.file = file;
    }

    public void readAll() throws IOException {
        partitionPacks = new PartitionPackManifest();
        randomIndexPack = null;
        rewind = false;
        lastRipEntry = null;
        addressables = new HashMap();
        resolvedAddressables = false;
        ecdMap = new HashMap();
        registry = new Registry();
        FileDescriptor fd = checkForRip(registry);
        fout = createEssenceFileOutputController(file);
        if (!checkSkipToNextPartition()) {
            FileInputStream fin = new FileInputStream(file);
            in = createInputStream(fin, registry, 0);
            expected = ExpectedData.HeaderPartition;
        }
        boolean warnNonEmptyEssence = false,
            warnNonEmptyIndex = false;
        Key key;
        try {
            do {
                if (isPastPartitionHeader()) {
                    // header set has been read
                    headerMetadataSetReadImpl();
                    if (metadataReadMode == MetadataReadMode.HeaderOnly) {
                        debug("done - " + MetadataReadMode.HeaderOnly + " set");
                        return;
                    } else if (checkSkipToNextPartition()) {
                        continue;
                    } else if (!partitionPacks.current().getIndexByteCount().equals(BigInteger.ZERO)) {
                        expected = ExpectedData.IndexData;
                    } else if (partitionPacks.current().getBodySid() != 0) {
                        expected = ExpectedData.PartitionData;
                    } else {
                        expected = ExpectedData.NonHeaderPartition;
                    }
                }
                if (isPastPartitionIndex()) {
                    // index has been read
                    indexReadImpl();
                    if (partitionPacks.current().getBodySid() != 0) {
                        expected = ExpectedData.PartitionData;
                    } else {
                        expected = ExpectedData.NonHeaderPartition;
                    }
                }
                key = in.readKey();
                long lastKeyOffset = in.getLastKeyOffsetInStream();
                debug(key + " (0x" + Long.toHexString(lastKeyOffset) + ")");
                level = 1;
                UL ul = key.getUL();
                if (ul.equals(Groups.RandomIndexPack)) {
                    handleRip(key);
                    debug("done - past RIP");
                    return;
                } else if (Metadata.FillerData.equals(ul)) {
                    skipKeyValue(key, key.parser(in));
                    continue;
                } else if (lookForPartitionPack(key)) {
                    continue;
                } else if (lookForPrimerPack(key)) {
                    continue;
                } else if (Groups.IndexTableSegment.equals(ul)) {
                    if (!assertExpected(ExpectedData.IndexData)) {
                        warnContext("header data set ran short");
                    }
                    if (!warnNonEmptyIndex && partitionPacks.current().getIndexSid() == 0) {
                        warnContext("index segment in partition with no index");
                        warnNonEmptyIndex = true;
                    }
                    parseKey(key);
                } else if (ul.isGcEssenceElement() || ul.isGcSystemElement()) {
                    if (!assertExpected(ExpectedData.PartitionData)) {
                        warnContext("header data set ran short");
                    }
                    if (!warnNonEmptyEssence && partitionPacks.current().getBodySid() == 0) {
                        warnContext("essence data in partition with no body");
                        warnNonEmptyEssence = true;
                    }
                    if (checkSkipToNextPartition()) {
                        continue;
                    }
                    if (ul.isGcEssenceElement()) {
                        if (!partitionPacks.hasCurrent()) {
                            throw new IllegalStateException("no partition pack found before essence");
                        }
                        parseEssenceElement(key);
                    } else if (ul.isGcSystemElement()) {
                        if (!partitionPacks.hasCurrent()) {
                            throw new IllegalStateException("no partition pack found before essence");
                        }
                        parseSystemElement(key);
                    }
                } else { // assume it's header metadata
                    assertExpected(ExpectedData.HeaderData);
                    parseKey(key);
                }
            } while (true);
        } catch (EOFException eof) {
            // done
            debug("done - EOF:" + eof.getMessage());
        } finally {
            postAssert();
            debug("position: 0x" + Long.toHexString(in.getStreamOffset()));
            in.close();
            fout.close();
        }
    }

    private boolean assertExpected(ExpectedData seen) {
        if (seen != expected) {
            warnContext("unexpected " + seen + ", expected " + expected);
            expected = seen;
            partitionPacks.current().setEndOfHeader(null);
            partitionPacks.current().setEndOfIndex(null);
            return false;
        }
        return true;
    }

    private boolean lookForPartitionPack(Key key) throws IOException {
        if (key instanceof PartitionPackKey) {
            parsePartitionPack((PartitionPackKey) key);
            return true;
        }
        // assert that if we were looking for a partition, that we found it; otherwise bail
        if (expected == ExpectedData.HeaderPartition || expected == ExpectedData.BodyPartition
                || expected == ExpectedData.FooterPartition || expected == ExpectedData.NonHeaderPartition) {
            throw new IOException("no partition pack key found");
        }
        return false;
    }

    private boolean lookForPrimerPack(Key key) throws IOException {
        UL ul = key.getUL();
        if (ul.equals(Groups.PrimerPack)) {
            parsePrimerPack(key);
            return true;
        }
        // assert that if we were looking for a primer, that we found it; otherwise bail
        if (expected == ExpectedData.PrimerPack) {
            throw new IOException("no primer found");
        }
        return false;
    }

    private void handleRip(Key key) throws IOException {
        if (randomIndexPack == null
                || in.getLastKeyOffset() != randomIndexPack.getOffset()) {
            warnContext("RIP was not expected, expecting " + expected);
        } else if (randomIndexPack != null
                && randomIndexPack.getEntries().size() != partitionPacks.count()) {
            warnContext("RIP entry count doesn't match observed number of partitions");
        }
        in.skip(key.getLength());
        if (in.getStreamOffset() < file.length()) {
            warnContext("there's data past the end of the RIP; ignoring");
        }
    }

    private boolean isPastPartitionHeader() {
        return (partitionPacks.hasCurrent() && partitionPacks.current().getEndOfHeader() != null
                && partitionPacks.current().getEndOfHeader().compareTo(
                    BigInteger.valueOf(in.getOffset())) <= 0);
    }

    private boolean isPastPartitionIndex() {
        return (partitionPacks.hasCurrent() && partitionPacks.current().getEndOfIndex() != null
                && partitionPacks.current().getEndOfIndex().compareTo(
                    BigInteger.valueOf(in.getOffset())) <= 0);
    }

    private boolean canUseRip() {
        return randomIndexPack != null && isUseRip();
    }

    private boolean checkSkipToNextPartition() throws IOException {
        if ((isParseEssenceElements() || isParseSystemElements())
                && fout.needEssence()) { // TODO: get rid of needEssence()
            return false;
        }
        // if RIP is present and usable, and if readHeaderOnly, parseSystemElements
        // and parseEssenceElements are all NOT set, then use RIP to skip to additional partitions
        if (metadataReadMode == MetadataReadMode.FooterOnly) {
            if ((partitionPacks.hasCurrent()
                    && partitionPacks.current().getKind() == PartitionPackKind.Footer)) {
                rewind = true;
                partitionPacks.pivot();
            }
            if (rewind) {
                if ((partitionPacks.hasCurrent()
                        && partitionPacks.current().getKind() == PartitionPackKind.Header)) { // done!
                    throw new EOFException("done - rewind");
                } else if (canUseRip()) {
                    if (skipToPreviousRipPartition(registry, toLong(partitionPacks.current().getOffset()))) {
                        return true;
                    }
                } else {
                    if (skipToPreviousOffsetPartition(registry)) {
                        return true;
                    }
                }
                throw new IOException("can not rewind through file; bailing...");
            } else if (canUseRip()) {
                return skipToFooterRipPartition(registry);
            } else {
                return skipToFooterPartition(registry);
            }
        } else if (canUseRip() && skipToNextRipPartition(registry, (in != null) ? in.getOffset() : 0)) {
            return true;
        }
        return false;
    }

    private boolean skipToFooterPartition(Registry registry) throws IOException {
        if (partitionPacks.getKnownFooterOffset() != null) {
            in.close();
            this.in = createOffsetInputStream(in, registry, partitionPacks.getKnownFooterOffset());
            expected = ExpectedData.FooterPartition;
            return true;
        }
        return false;
    }

    private boolean skipToPreviousOffsetPartition(Registry registry) throws IOException {
        in.close();
        BigInteger offset = partitionPacks.current().getPreviousPartition();
        partitionPacks.push();
        this.in = createOffsetInputStream(in, registry, offset);
        if (!offset.equals(BigInteger.ZERO)) { // TODO: if we previously found a partition with a zero prev partition and we have no RIP, then maybe we shouldn't try a rewind!
            expected = ExpectedData.BodyPartition;
        } else {
            expected = ExpectedData.HeaderPartition;
        }
        return true;
    }

    private boolean skipToFooterRipPartition(Registry registry) throws IOException {
        RipEntry ripEntry = randomIndexPack.getEntries().get(randomIndexPack.getEntries().size() - 1);
        in.close();
        lastRipEntry = ripEntry;
        this.in = createOffsetInputStream(in, registry, ripEntry.getBodyOffset());
        expected = ExpectedData.FooterPartition;
        return true;
    }

    private boolean skipToNextRipPartition(Registry registry, long offset) throws IOException {
        int count = 0;
        for (RipEntry ripEntry : randomIndexPack.getEntries()) {
            ++count;
            if (ripEntry.getBodyOffset().longValue() < offset) {
                continue;
            }
            in.close();
            lastRipEntry = ripEntry;
            partitionPacks.push();
            this.in = createOffsetInputStream(in, registry, ripEntry.getBodyOffset());
            if (count == 1) {
                expected = ExpectedData.HeaderPartition;
            } else if (count == randomIndexPack.getEntries().size()) {
                expected = ExpectedData.FooterPartition;
            } else {
                expected = ExpectedData.BodyPartition;
            }
            return true;
        }
        return false;
    }

    private boolean skipToPreviousRipPartition(Registry registry, long offset) throws IOException {
        int count = 0;
        for (int i = randomIndexPack.getEntries().size() - 1; i >= 0; i--) {
            RipEntry ripEntry = randomIndexPack.getEntries().get(i);
            ++count;
            if (ripEntry.getBodyOffset().longValue() >= offset) {
                continue;
            }
            in.close();
            lastRipEntry = ripEntry;
            partitionPacks.push();
            this.in = createOffsetInputStream(in, registry, ripEntry.getBodyOffset());
            if (count == 1) {
                expected = ExpectedData.FooterPartition;
            } else if (count == randomIndexPack.getEntries().size()) {
                expected = ExpectedData.HeaderPartition;
            } else {
                expected = ExpectedData.BodyPartition;
            }
            return true;
        }
        return false;
    }

    private MxfInputStream createInputStream(FileInputStream fin, Registry registry,
                                             final long streamOffset) {
        MxfInputStream in = new MxfInputStream(new BufferedInputStream(fin), registry, streamOffset);
        in.setEnableRuninAnywhere(isEnableRuninAnywhere());
        in.setDebug(isDebugPrint());
        return in;
    }

    private MxfInputStream createOffsetInputStream(MxfInputStream old, Registry registry, final BigInteger bigOffset)
            throws IOException {
        final long offset = toLong(bigOffset);
        // quick check for seeking past EOF
        if (offset > file.length()) {
            throw new IOException("seeked past EOF; file ends prematurely");
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        randomAccessFile.seek(offset);
        FileInputStream fin = new FileInputStream(randomAccessFile.getFD());
        MxfInputStream in = old.offset(new BufferedInputStream(fin), registry, offset);
        in.setEnableRuninAnywhere(isEnableRuninAnywhere());
        in.setDebug(isDebugPrint());
        return in;
    }

    protected EssenceContainerOutputController createEssenceFileOutputController(File file) {
        return new DummyOutputController();
    }

    private void parseSystemElement(Key key) throws IOException {
        if (isParseSystemElements() && fout.needEssence()) {
            UL ul = key.getUL();
            OutputStream out = fout.getOutputForSystemElement(partitionPacks.current().getBodySid(), ul);
            if (out == null) {
                in.skip(key.getLength());
            } else {
                byte[] buff = new byte[toInt(key.getLength())];
                in.readFully(buff);
                out.write(buff);
            }
        } else {
            in.skip(key.getLength());
        }
    }

    private void parseEssenceElement(Key key) throws IOException {
        UL ul = key.getUL();
        if (isParseEssenceElements() && fout.needEssence()) {
            OutputStream fos;
            if (essenceOutputStream != null) {
                if (ul.isPictureEssence()) {
                    fos = essenceOutputStream;
                    TsOutputStream tsout = (TsOutputStream) fos;
                    byte[] buff = new byte[toInt(key.getLength())];
                    in.readFully(buff);
                    int pid = 0x1E1;
                    tsout.writeEs(pid, 0xE0, buff);
                } else if (ul.isSoundEssence()) {
                    fos = essenceOutputStream;
                    TsOutputStream tsout = (TsOutputStream) fos;
                    byte[] buff = new byte[toInt(key.getLength())];
                    in.readFully(buff);
                    int pid = 0x1E2;
                    tsout.writeEs(pid, 0xBD, buff);
                } else {
                    byte[] buff = new byte[toInt(key.getLength())];
                    in.readFully(buff);
                    // ...and do nothing
                }
            } else {
                fos = fout.getOutputForBodySidAndTrack(partitionPacks.current().getBodySid(), ul);
                if (fos == null) {
                    in.skip(key.getLength());
                } else {
//                    byte[] buff = new byte[toInt(key.getLength())];
//                    in.readFully(buff);
//                    fos.write(buff);

                    int len = toInt(key.getLength());
                    do {
                        int size = Math.min(ESSENCE_DUMP_BUFF_SIZE, len);
                        in.readFully(essenceDumpBuff, 0, size);
                        fos.write(essenceDumpBuff, 0, size);
                        len -= size;
                    } while (len > 0);
                }
            }
        } else {
            in.skip(key.getLength());
        }
    }

    private void parsePartitionPack(PartitionPackKey packKey) throws IOException {
        debug("found partition pack");
        if (expected == ExpectedData.PartitionData
                || expected == ExpectedData.NonHeaderPartition) {
            expected = calculateNextExpectedPartition(in.getLastKeyOffset());
        } else if (expected != ExpectedData.HeaderPartition
                && expected != ExpectedData.BodyPartition
                && expected != ExpectedData.FooterPartition) {
            warnContext("a partition pack was not expected, found kind: " + packKey.getPackKind());
            expected = calculateNextExpectedPartition(in.getLastKeyOffset());
        }
        PartitionPackParser parser = packKey.parser(in);
        parser.setPackKey(packKey);
        partitionPacks.set(parser.read(), rewind);
        if (parser.read() != null) {
            throw new IOException("empty partition pack");
        }
        partitionPacks.current().setOffset(BigInteger.valueOf(in.getLastKeyOffset()));
        if (!partitionPacks.current().getFooterPartition().equals(BigInteger.ZERO)) {
            if (partitionPacks.getKnownFooterOffset() != null
                    && !partitionPacks.getKnownFooterOffset().equals(
                        partitionPacks.current().getFooterPartition())) {
                warnContext("found different non-zero footer location of"
                        + " 0x" + Long.toHexString(toLong(partitionPacks.current().getFooterPartition()))
                        + " in part at 0x" + Long.toHexString(in.getLastKeyOffsetInStream()));
            } else if (partitionPacks.getKnownFooterOffset() == null) {
                partitionPacks.setKnownFooterOffset(partitionPacks.current().getFooterPartition());
            }
        }
        assertPartitionParameters(partitionPacks.current());
//        assertPartitionPackRecordedInRip(partitionPacks.current());
        if (isDebugPrint()) {
            debug(partitionPacks.current().getKind() + ": " + partitionPacks.current().getHeaderByteCount());
            debug(level, Utils.dump(partitionPacks.current(), level));
        }
        partitionPackRead(partitionPacks.current());
        // set the expected type of the next section of the stream
        if (partitionPacks.current().getHeaderByteCount().equals(BigInteger.ZERO)) {
            if (!checkSkipToNextPartition()) {
                calculateHeaderSetAndIndexPositions(in.getOffset()); // header is not present, no Primer Pack
                if (!partitionPacks.current().getIndexByteCount().equals(BigInteger.ZERO)) {
                    expected = ExpectedData.IndexData;
                } else if (partitionPacks.current().getBodySid() != 0) {
                    expected = ExpectedData.PartitionData;
                } else {
                    expected = ExpectedData.NonHeaderPartition;
                }
            }
        } else {
            expected = ExpectedData.PrimerPack;
        }
    }

    /**
     * Calculates a definitive expected partition type expected, i.e. Header, Body, Footer, any non-Header.
     * @param offset the offset of the partition's pack key
     * @return <code>ExpectedData.HeaderPartition, BodyPartition, FooterPartition, NonHeaderPartition</code>
     */
    private ExpectedData calculateNextExpectedPartition(long offset) {
        if (partitionPacks.count() == 0) {
            return ExpectedData.HeaderPartition;
        } else {
            if (partitionPacks.getKnownFooterOffset() != null) {
                if (partitionPacks.getKnownFooterOffset().equals(
                        BigInteger.valueOf(offset))) {
                    return ExpectedData.FooterPartition;
                } else if (partitionPacks.getKnownFooterOffset().compareTo(
                        BigInteger.valueOf(offset)) < 0) {
                    warnContext("past expected footer partition offset");
                    return ExpectedData.FooterPartition;
                }
            }
//            return ExpectedData.BodyPartition;
            return ExpectedData.NonHeaderPartition;
        }
    }

    private void assertPartitionParameters(PartitionPack current) {
        // check if this partition type was unexpected
        if (expected == ExpectedData.NonHeaderPartition) {
//            throw new IllegalStateException(
//                    "expected partition shouldn't be " + ExpectedData.NonHeaderPartition);
            if (current.getKind() == PartitionPackKind.Header) {
                warnContext("unexpected header partition instead of a Body or Footer");
            }
        } else if (current.getKind() == PartitionPackKind.Header && expected != ExpectedData.HeaderPartition
                || current.getKind() == PartitionPackKind.Body && expected != ExpectedData.BodyPartition
                || current.getKind() == PartitionPackKind.Footer && expected != ExpectedData.FooterPartition) {
            warnContext("unexpected partition pack " + current.getKind() + ", expected " + expected);
        }
        // check partition required values
        if (!current.getThisPartition().equals(current.getOffset())) {
            warnContext("" + current.getKind() + " partition's offset doesn't match its found offset");
        }
        if (current.getKind() == PartitionPackKind.Header) {
            if (current.getHeaderByteCount().equals(BigInteger.ZERO)) {
                warnContext("Header partition must contain header metadata set");
            }
            if (!current.getPreviousPartition().equals(BigInteger.ZERO)) {
                warnContext("Header partition must have its previous partition set to zero");
            }
        } else if (current.getKind() == PartitionPackKind.Footer) {
            if (!current.getFooterPartition().equals(current.getThisPartition())) {
                warnContext("Footer partition's offsets don't agree");
            }
            if (!current.getBodyOffset().equals(BigInteger.ZERO)) {
                warnContext("Footer partition must have a body offset of zero");
            }
            if (current.getBodySid() != 0) {
                warnContext("Footer partition must not be marked as containing essence data");
            }
        }
    }

    private void postAssert() {
        boolean hasFooter = false;
        if (partitionPacks.count() > 0 && partitionPacks.getList().get(
                partitionPacks.count() - 1).getKind() == PartitionPackKind.Footer) {
            hasFooter = true;
        }
        if (randomIndexPack != null && metadataReadMode != MetadataReadMode.HeaderOnly) {
            if (randomIndexPack.getEntries().size() != partitionPacks.count()) {
                warnContext("Partition count and RIP count do not match");
            }
        }
        BigInteger lastOffset = BigInteger.ZERO;
        for (int i = 0; i < partitionPacks.count(); i++) {
            PartitionPack partitionPack = partitionPacks.getList().get(i);
            int count = i + 1;
            if (!partitionPack.getPreviousPartition().equals(lastOffset)) {
                warnContext("Partition #" + count + "'s previous partition offset"
                        + " doesn't match #" + i + "'s offset");
            }
            lastOffset = partitionPack.getOffset();
            postAssertPartitionParameters(partitionPack, hasFooter);
            postAssertPartitionPackRecordedInRip(partitionPack, count);
        }
    }

    private void postAssertPartitionParameters(PartitionPack partitionPack, boolean hasFooter) {
        if (hasFooter && partitionPack.isClosed() && partitionPack.getFooterPartition().equals(BigInteger.ZERO)) {
            warnContext("Footer Partition offset is not set even though this partition is marked closed");
        }
    }

    private void postAssertPartitionPackRecordedInRip(PartitionPack current, int order) {
        if (randomIndexPack == null) {
            return;
        }
        int count = 0;
        for (RipEntry ripEntry : randomIndexPack.getEntries()) {
            ++count;
            if (ripEntry.getBodyOffset().equals(current.getOffset())) {
                if (count == order) {
                    return;
                }
                warnContext("found partition in RIP at wrong location"
                        + ", part #" + order
                        + ", RIP #" + count);
                return;
            }
        }
        warnContext("can't find part #" + partitionPacks.count() + " in RIP");
    }

    private void parsePrimerPack(Key key) throws IOException {
        if (expected != ExpectedData.PrimerPack) {
            warnContext("primer found where none was expected, ignoring");
            skipKeyValue(key, key.parser(in));
            return;
        }
        debug("adding primer pack entries");
        if (!partitionPacks.hasCurrent()) {
            throw new IllegalStateException("no partition pack found before primer pack");
        }
        calculateHeaderSetAndIndexPositions(in.getLastKeyOffset());
        PrimerPackParser parser = (PrimerPackParser) key.parser(in);
        PrimerPackEntry ppEntry;
        Vector ppEntries = new Vector();
        while ((ppEntry = parser.read()) != null) {
            ppEntries.add(ppEntry);
        }
        in.setPrimerPack(ppEntries);
        expected = ExpectedData.HeaderData;
    }

    private void calculateHeaderSetAndIndexPositions(long offset) {
        if (!partitionPacks.current().getHeaderByteCount().equals(BigInteger.ZERO)) {
            partitionPacks.current().setEndOfHeader(BigInteger.valueOf(offset)
                    .add(partitionPacks.current().getHeaderByteCount()));
        }
        if (!partitionPacks.current().getIndexByteCount().equals(BigInteger.ZERO)) { // index is present
            partitionPacks.current().setEndOfIndex(
                    BigInteger.valueOf(offset)
                            .add(partitionPacks.current().getHeaderByteCount())
                            .add(partitionPacks.current().getIndexByteCount()));
        }
    }

    private FileDescriptor checkForRip(Registry registry) throws IOException {
        RandomAccessFile randomFile = new RandomAccessFile(file, "r");
        randomFile.seek(randomFile.length() - 4);
        long ripPackLength = randomFile.readInt() & ((1L << 32) - 1);
        long offset = randomFile.length() - ripPackLength;
        if (offset < 0) {
            debug("rip pack length is greater than total file length");
            return null;
        }
        randomFile.seek(offset);
        FileInputStream fin = new FileInputStream(randomFile.getFD());
        this.in = createInputStream(fin, registry, offset);
        Key key;
        try {
            key = in.readKey();
        } catch (IOException e) {
            randomFile.close();
            return null;
        }
        UL ul = key.getUL();
        if (ul.equals(Groups.RandomIndexPack)) {
            parseRandomIndexPack(key, ripPackLength);
        }
        randomFile.close();
        return null;
//        randomFile.seek(0);
//        return randomFile.getFD();
    }

    private void parseRandomIndexPack(Key key, long ripPackLength) throws IOException {
        debug("found RIP");
        long lastRipOffset = in.getLastKeyOffset();
        // validate the overall length against the key's length
        long valueLength = key.getLength().longValue();
        long packLength = in.getOffset() - lastRipOffset + valueLength;
        if (ripPackLength != packLength) {
            warnContext("RIP key length doesn't match its total length field " + ripPackLength + "!=" + packLength);
            return;
        } else if ((valueLength - 4) % 12 != 0) {
            warnContext("RIP entries are incomplete");
            return;
        }
        RandomIndexPackParser parser = (RandomIndexPackParser) key.parser(in);
        parser.setEntryCount(valueLength / 12);
        randomIndexPack = parser.read();
        randomIndexPack.setOffset(lastRipOffset);
        randomIndexPackRead(randomIndexPack);
    }

    private void parseKey(Key key) throws IOException {
        Parser parser = key.parser(in);
        Object value;
        if (parser instanceof SetParser) {
            groupSetStarted(key);
        }
        while ((value = parser.read()) != null) {
            if (value instanceof Key) {
                String offset = " (0x" + Long.toHexString(in.getLastKeyOffsetInStream()) + ")";
                debug(level, String.valueOf(value) + offset);
                level++;
                parseKey((Key)value);
                level--;
            } else {
                valueRead(key, value);
                debug(level, String.valueOf(value));
            }
        }
        if (parser instanceof SetParser) {
            groupSetEnded(key);
        }
    }

    private Object readKeyValue(Key key, Parser parser) throws IOException {
        String offset = " (0x" + Long.toHexString(in.getLastKeyOffsetInStream()) + ")";
        Object value = parser.read();
        parser.skip();
        valueRead(key, value);
        debug(level, key + offset + ": " + String.valueOf(value));
        return value;
    }

    private List readAllKeyValue(Key key, Parser parser) throws IOException {
        debug(level, key + " (0x" + Long.toHexString(in.getLastKeyOffsetInStream()) + ")" + ":");
        List list = parser.readAll();
        for (Object value : list) {
            valueRead(key, value);
        }
        debug(level + 1, String.valueOf(list));
        return list;
    }

    private void skipKeyValue(Key key, Parser parser) throws IOException {
        debug(level, key + " (0x" + Long.toHexString(in.getLastKeyOffsetInStream()) + ")" + " (skip)");
        parser.skip();
    }

    public void groupSetStarted(Key key) {
    }

    public void groupSetEnded(Key key) {
    }

    public void valueRead(Key key, Object value) {
    }

    public void partitionPackRead(PartitionPack partitionPack) {
    }

    private void headerMetadataSetReadImpl() {
        // warn if header set size was incorrect
        if (partitionPacks.current().getEndOfHeader().compareTo(BigInteger.valueOf(in.getOffset())) < 0) {
            if (expected == ExpectedData.HeaderData) { // TODO this right?
                warnContext("header data set ran long");
            }
        }
        headerMetadataSetRead(partitionPacks.current());
        in.clearPrimerPack();
        partitionPacks.current().setEndOfHeader(null);
    }

    private void indexReadImpl() {
        // warn if header set size was incorrect
        if (partitionPacks.current().getEndOfIndex().compareTo(BigInteger.valueOf(in.getOffset())) < 0) {
            if (expected == ExpectedData.HeaderData) { // TODO this right?
                warnContext("index ran long");
            }
        }
        indexRead(partitionPacks.current());
        partitionPacks.current().setEndOfIndex(null);
    }

    public void headerMetadataSetRead(PartitionPack partitionPack) {
    }

    public void indexRead(PartitionPack partitionPack) {
    }

    public void randomIndexPackRead(RandomIndexPack randomIndexPack) {
    }

    private int toInt(BigInteger big) {
        int value = big.intValue();
        if (value < 0) {
            throw new IllegalArgumentException("overflow");
        }
        return value;
    }

    private long toLong(BigInteger big) {
        long value = big.longValue();
        if (value < 0) {
            throw new IllegalArgumentException("overflow");
        }
        return value;
    }

    protected void debug(String message) {
        debug(0, message);
    }

    private void debug(Object o) {
        debug(0, Utils.dump(o, 0));
    }

    private void debug(int level, String message) {
        if (!isDebugPrint()) {
            return;
        }
        String pre = "";
        if (level > 0) {
            switch (level) {
                case 1:
                    pre = "\t";
                    break;
                case 2:
                    pre = "\t\t";
                    break;
                default:
                    char[] clevel = new char[level];
                    Arrays.fill(clevel, '\t');
                    pre = new String(clevel);
            }
        }
        log(pre + message);
    }

    // TODO revisit to see if debug level is appropriate since changing logger
    protected void log(String message) {
        log.debug(message);
    }

    protected void warnContext(String message) {
        String partition;
        if (partitionPacks.hasCurrent()) {
            partition = partitionPacks.current().getKind() + " partition at 0x"
                    + Long.toHexString(toLong(partitionPacks.current().getOffset()));
        } else {
            partition = "";
        }
        warn(message + " [" + partition + " @0x" + Long.toHexString(in.getLastKeyOffset()) + "]");
    }

    protected void warn(String message) {
        log.warn(message);
    }

    protected void error(String message, Exception exception) {
        log.error("MxfReader error: " + message, exception);
    }

    public File getFile() {
        return file;
    }

    public long getLastKeyOffset() {
        return in.getLastKeyOffset();
    }

    public long getStreamOffset() {
        return in.getStreamOffset();
    }

    /**
     * Gets whether debug print of all found keys and groups' keys are printed for debug
     */
    public boolean isDebugPrint() {
        return debugPrint;
    }

    /**
     * Sets whether debug print of all found keys, groups' keys, etc. are printed for debug
     * @param debugPrint Set to print debug messages
     */
    public void setDebugPrint(boolean debugPrint) {
        this.debugPrint = debugPrint;
    }

    public boolean isUseRip() {
        return useRip;
    }

    public void setUseRip(boolean useRip) {
        this.useRip = useRip;
    }

    /**
     * Sets whether to stop parsing after reading header metadata
     * @param mode Set to only read header, footer or all partitions' metadata
     */
    public void setMetadataReadMode(MetadataReadMode mode) {
        this.metadataReadMode = mode;
    }

    /**
     * Gets whether to parse essence data. Currently MPG and raw BWA.
     */
    public boolean isParseEssenceElements() {
        return parseEssenceElements;
    }

    /**
     * Sets whether to parse essence data. This will unset <code>readHeaderOnly</code>.
     * @param parseEssenceElements Set to parse; unset to not parse essence
     */
    public void setParseEssenceElements(boolean parseEssenceElements) {
        this.parseEssenceElements = parseEssenceElements;
        if (parseEssenceElements) {
            this.essenceDumpBuff = new byte[ESSENCE_DUMP_BUFF_SIZE];
        } else {
            this.essenceDumpBuff = null;
        }
    }

    /**
     * Gets whether to parse system elements
     */
    public boolean isParseSystemElements() {
        return parseSystemElements;
    }

    /**
     * Sets whether to parse system elements. This will unset <code>readHeaderOnly</code>.
     * @param parseSystemElements Set to parse; unset to not parse system elements
     */
    public void setParseSystemElements(boolean parseSystemElements) {
        this.parseSystemElements = parseSystemElements;
    }

    public boolean isEnableRuninAnywhere() {
        return enableRuninAnywhere;
    }

    public void setEnableRuninAnywhere(boolean enableRuninAnywhere) {
        this.enableRuninAnywhere = enableRuninAnywhere;
    }

    public void setEssenceOutputStream(OutputStream essenceOutputStream) {
        this.essenceOutputStream = essenceOutputStream;
    }

    public List<PartitionPack> getPartitionPacks() {
        return Collections.unmodifiableList(partitionPacks.getList());
    }
}
