package org.foraci.mxf;

import org.foraci.mxf.mxfReader.MxfReader;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class AppTest
{
    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void testMxfReader() throws IOException {
        String[] files = { getClass().getResource("/freeMXF-mxf1a.mxf").getFile() };
        MxfReader mxfReader;
        for (String fileName : files) {
            File file = new File(fileName);
            mxfReader = new MxfReader(file);
            mxfReader.setDebugPrint(true);
            mxfReader.setMetadataReadMode(MxfReader.MetadataReadMode.All);
            mxfReader.setParseEssenceElements(true);
            mxfReader.setParseSystemElements(true);
//            mxfReader.setEnableRuninAnywhere(true);
            long startTime = System.currentTimeMillis();
            mxfReader.readAll();
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(String.format("%s: elapsed time: %d ms", fileName, elapsedTime));
            Assert.assertTrue(file.length() == mxfReader.getStreamOffset());
        }
    }
}
