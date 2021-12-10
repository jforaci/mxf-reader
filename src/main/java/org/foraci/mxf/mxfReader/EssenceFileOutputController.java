package org.foraci.mxf.mxfReader;

import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;
import org.foraci.mxf.mxfReader.anc.Mxf436AncTrackReader;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;

/**
 * Controller to manage output streams for extracting essence tracks to files
 * @author jforaci
 */
public class EssenceFileOutputController implements EssenceContainerOutputController {
    protected Map fout = new HashMap();
    protected final File baseName;

    public EssenceFileOutputController(File baseName) {
        this.baseName = baseName;
    }

    public boolean needEssence() {
        return true;
    }

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul)
            throws IOException {
        long trackNumber = ul.getTrackNumber();
//        if (ul.isDataEssence() && (trackNumber == SMPTE436_VBI_TRACK || trackNumber == SMPTE436_ANC_TRACK)) {
//            return getOutputForSmpte436Scc(bodySid, ul);
//        }
        String key = "b" + String.valueOf(bodySid) + "t" + String.valueOf(trackNumber);
        FileOutputStream fos = (FileOutputStream) fout.get(key);
        if (fos == null) {
            String suffix = "-" + key;
            if (ul.isPictureEssence()) {
                suffix += "-" + "picture";
            } else if (ul.isSoundEssence()) {
                suffix += "-" + "sound";
            } else if (ul.isDataEssence()) {
                suffix += "-" + "data";
            }
            fos = new FileOutputStream(new File(baseName.getParentFile(), baseNameFilename() + suffix), false);
            fout.put(key, fos);
        }
        return fos;
    }

    private OutputStream getOutputForSmpte436Scc(long bodySid, UL ul)
            throws IOException {
        long trackNumber = ul.getTrackNumber();
        String key = "b" + String.valueOf(bodySid) + "t" + String.valueOf(trackNumber);
        PipedOutputStream os = (PipedOutputStream) fout.get(key);
        if (os == null) {
            String suffix = "-" + key + ".ccd";
            os = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(os);
            File cc608out = new File(baseName.getParentFile(), baseNameFilename() + suffix);
            Mxf436AncTrackReader ancReader = new Mxf436AncTrackReader(
                    in, Mxf436AncTrackReader.Type.ANC,
                    Timecode.fromEditUnits(TimecodeBase.NTSC, 0));
//            ancReader.setDebug(true);
            ancReader.setCcd608OutputStream(new BufferedOutputStream(new FileOutputStream(cc608out)));
            new Thread(ancReader, "ANC-READER").start();
            fout.put(key, os);
        }
        return os;
    }

    public OutputStream getOutputForSystemElement(long bodySid, UL ul) throws IOException
    {
        return null;
    }

    public void close() throws IOException {
        for (Iterator i = fout.values().iterator(); i.hasNext();) {
            OutputStream fos = (OutputStream) i.next();
            fos.flush();
            fos.close();
        }
    }

    protected String baseNameFilename() {
        String name;
        int extIndex = baseName.getName().lastIndexOf('.');
        if (extIndex != -1) {
            name = baseName.getName().substring(0, extIndex);
        } else {
            name = baseName.getName();
        }
        return name;
    }
}
