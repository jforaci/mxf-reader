package org.foraci.mxf.mxfReader.util.probe;

import org.foraci.anc.util.io.MultiplexingInputStream;
import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.UL;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Essence output controller
 */
public class TrackInfoEssenceController implements EssenceContainerOutputController
{
    protected Map<EssenceTrack, PipedOutputStream> fout = new HashMap<EssenceTrack, PipedOutputStream>();
    private final MxfTrackInfoReader ancReader;
    private final List<EssenceTrack> tracks;
    private final Map<String, EssenceTrack> trackMap;
    private boolean done;
    private int sysItemCount, cpCount;
    private long firstTrackNumber = 0;
    private boolean isScanningForDolbyE;

    public TrackInfoEssenceController(MxfTrackInfoReader ancReader, List<EssenceTrack> tracks) {
        this.ancReader = ancReader;
        this.tracks = tracks;
        this.trackMap = new HashMap<String, EssenceTrack>();
        for (EssenceTrack track : tracks) {
            String key = "b" + track.getBodySid() + "t" + track.getTrackNumber();
            trackMap.put(key, track);
        }
        this.done = false;
        this.sysItemCount = this.cpCount = 0;
        this.isScanningForDolbyE = true;
    }

    public boolean needEssence() {
        return !done;
    }

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) throws IOException {
        long trackNumber = ul.getTrackNumber();
        EssenceTrack track = getTrackIgnored(bodySid, ul);
        if (track == null) { // ignored track
            return null;
        }
        if (firstTrackNumber == 0) {
            firstTrackNumber = trackNumber;
        } else if (firstTrackNumber == trackNumber) {
            cpCount++;
            checkForDone();
        }
        if (ul.isDataEssence() && (trackNumber == SMPTE436_VBI_TRACK || trackNumber == SMPTE436_ANC_TRACK)) {
            return null; // TODO: ANC/VBI info can be hooked-into here
        } else if (ul.isSoundEssence() && isScanningForDolbyE) {
            return getDolbyESink(track);
        }
        return null;
    }

    private EssenceTrack getTrackIgnored(long bodySid, UL ul) {
        String key = "b" + String.valueOf(bodySid) + "t" + ul.getTrackNumber();
        return trackMap.get(key);
    }

    private Map<EssenceTrack, DolbyEConsumer> eReaderMap = new HashMap<EssenceTrack, DolbyEConsumer>();

    private OutputStream getDolbyESink(EssenceTrack track)
            throws IOException {
        PipedOutputStream os = fout.get(track);
        if (os == null) {
            os = new PipedOutputStream();
            BufferedInputStream in = new BufferedInputStream(new PipedInputStream(os));
            MultiplexingInputStream muxInputStream = new MultiplexingInputStream(new DataInputStream(in), null, 3);
            DolbyEConsumer consumer = new DolbyEConsumer(
                    muxInputStream, 3, false, true, track);
            new Thread(consumer, "DOLBYE-READER").start();
            fout.put(track, os);
            eReaderMap.put(track, consumer);
        } else if (eReaderMap.get(track).isDone()) {
            return null;
        }
        return os;
    }

    public OutputStream getOutputForSystemElement(long bodySid, UL ul) throws IOException {
        if (ul.isFirstGcSystemElementInCp()) {
            sysItemCount++;
        }
        if (sysItemCount % 10 == 0) {
            checkForDone();
        }
        return null;
    }

    private void checkForDone() {
        if (isScanningForDolbyE) {
            for (Iterator<DolbyEConsumer> i = eReaderMap.values().iterator(); i.hasNext();) {
                DolbyEConsumer consumer = i.next();
                if (!consumer.isDone()) {
                    return;
                }
            }
        }
        done = true;
    }

    public void close() throws IOException {
        for (Iterator i = fout.values().iterator(); i.hasNext();) {
            OutputStream fos = (OutputStream) i.next();
            fos.flush();
            fos.close();
        }
        for (Iterator<DolbyEConsumer> i = eReaderMap.values().iterator(); i.hasNext();) {
            DolbyEConsumer consumer = i.next();
            synchronized (consumer) {
                consumer.kill();
                consumer.notifyAll(); // let consumer die
            }
        }
        eReaderMap.clear();
    }
}
