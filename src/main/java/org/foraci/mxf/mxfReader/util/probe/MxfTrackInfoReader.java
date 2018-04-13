package org.foraci.mxf.mxfReader.util.probe;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.MxfReader;

import java.io.File;
import java.util.List;

/**
 * An <code>MxfReader</code> that retrieves track information
 */
public class MxfTrackInfoReader extends MxfReader {

    private final List<EssenceTrack> tracks;

    public MxfTrackInfoReader(File file, List<EssenceTrack> tracks) {
        super(file);
        this.tracks = tracks;
    }

    @Override
    protected EssenceContainerOutputController createEssenceFileOutputController(File file) {
        return new TrackInfoEssenceController(this, tracks);
    }
}
