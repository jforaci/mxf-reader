package org.foraci.mxf.mxfReader.tests;

import org.foraci.anc.anc.AncTrackReader;
import org.foraci.mxf.mxfReader.anchd480.Hd480AncTrackReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Test reader for ancillary data from an EEG HD480
 *
 * @author Joe Foraci
 */
public class Hd480AncillaryReader
{
    private static final Logger log = LoggerFactory.getLogger(Hd480AncillaryReader.class);

    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length == 0 || args.length > 1) {
            log.error("Please specify a file containing captured HD 480 data");
            System.exit(1);
        }
        AncTrackReader reader = new Hd480AncTrackReader(new FileInputStream(args[0]));
        reader.setCcd608OutputStream(new BufferedOutputStream(new FileOutputStream(new File("test.ccd"))));
        reader.read();
    }
}
