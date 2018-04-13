package org.foraci.mxf.mxfReader.util.probe;

import org.foraci.anc.util.io.MultiplexingInputStream;
import org.foraci.dolby.DolbyEReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Simple Dolby E sink to read the first frame of E (if it's present) and then exit
 */
public class DolbyEConsumer extends DolbyEReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(DolbyEConsumer.class);

    private MultiplexingInputStream in;
    private final boolean align;
    private final boolean probe;
    private final EssenceTrack track;
    private volatile boolean done;
    private volatile boolean kill;

    public DolbyEConsumer(MultiplexingInputStream in,
                          int sampleSize, boolean align, boolean probe, EssenceTrack track) {
        super(in, sampleSize, false);
        this.in = in;
        this.align = align;
        this.probe = probe;
        this.track = track;
    }

    public void run() {
        log.info("Looking for AES/DolbyE on " + track);
        try {
            if (align) {
                in.align();
            }
            if (probe) {
                probeForAESFrame(1024 * 1024);
            }
            readFrame();
            log.info(
                    "DolbyE found on file " + track.getFile().getName()
                            + ", track " + track.getTrackId());
        } catch (Exception e) {
            log.warn("Failed to find DolbyE on " + track + ": " + e.getMessage());
        }
        done = true;
        try {
            while (in.skip(1024*1024) > 0);
            while (!kill) {
                synchronized (this) {
                    wait();
                }
            }
//            System.out.print(trackNumber + " dead");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDone() {
        return done;
    }

    public void kill()
    {
        this.kill = true;
    }

    @Override
    protected void info(String message) {
    }

    @Override
    protected void warn(String message) {
    }
}
