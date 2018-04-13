package org.foraci.mxf.mxfReader.anc;

import org.foraci.anc.anc.*;
import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;
import org.foraci.mxf.mxfReader.MxfReader;
import org.foraci.mxf.mxfReader.util.MxfReaderUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Reads ancillary data from a MXF 436m track and creates a CCD file
 *
 * @author jforaci
 */
public class AncillaryReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(AncillaryReader.class);

    private final File file;
    private final File ccdFile;
    private PipedOutputStream ancTrackOutputStream;
    private static long packetCount;

    public AncillaryReader(File file, File ccdFile) {
        this.file = file;
        this.ccdFile = ccdFile;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0 || args.length > 1) {
            log.error("Please specify a file containing a demuxed MXF 436m track");
            System.exit(1);
        }
        packetCount = 0;
        AncTrackReader reader = new Mxf436AncTrackReader(
                new FileInputStream(args[0]),
                Mxf436AncTrackReader.Type.ANC,
                Timecode.fromEditUnits(TimecodeBase.NTSC, 0));
        reader.setCcd608OutputStream(new BufferedOutputStream(new FileOutputStream(new File("test.ccd"))));
        reader.addAncPacketListener(new AncPacketListener() {
            @Override
            public void packet(AncPacketHeader header, AncPacketUserData payload, TrackAttributes trackAttributes) {
                packetCount++;
            }
        });
        log.info("reading " + args[0]);
        reader.read();
        log.info("packetCount: " + packetCount);
    }

    public void read() throws IOException, InterruptedException {
        MxfReaderUtility mxfReaderUtility = new MxfReaderUtility(file);
        Timecode timecode = Timecode.fromTimecode(
                TimecodeBase.NTSC,
                mxfReaderUtility.getStartTimecode().getLabel());
        ancTrackOutputStream = new PipedOutputStream();
        InputStream ancTrackInputStream = new PipedInputStream(ancTrackOutputStream);
        Thread mxfReaderThread = new Thread(this);
        mxfReaderThread.start();
        Mxf436AncTrackReader ancReader = new Mxf436AncTrackReader(
                ancTrackInputStream, Mxf436AncTrackReader.Type.ANC, timecode);
        ancReader.setDebug(false);
        ancReader.setCcd608OutputStream(new BufferedOutputStream(new FileOutputStream(ccdFile)));
        ancReader.read();
        mxfReaderThread.join();
    }

    @Override
    public void run() {
        MxfVancDemuxer demuxer = new MxfVancDemuxer(file, ancTrackOutputStream);
        demuxer.setMetadataReadMode(MxfReader.MetadataReadMode.All);
        demuxer.setParseEssenceElements(true);
        try {
            demuxer.readAll();
            ancTrackOutputStream.close();
        } catch (IOException e) {
            log.error("I/O error", e);
        } catch (Throwable t) {
            log.error("error", t);
        }
    }
}
