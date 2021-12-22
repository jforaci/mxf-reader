package org.foraci.mxf.mxfReader.util;

import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;
import org.foraci.mxf.mxfReader.MxfTreeReader;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * A helper class to aggregate some common operations on MXF files
 *
 * @author jforaci
 */
public class MxfReaderUtility {
    private final File mxfFile;
    private boolean hasBeenRead = false;
    private MxfTreeReader reader;
    private Set<GroupNode> groups = null;
    private boolean materialPackageAttributesRead = false;
    private int mpStart, mpDuration;
    private boolean mpDropFrame;
    private int roundedTimecodeBase;

    public static void main(String[] args) throws IOException {
        for (File file : new File(args[0]).listFiles()) {
            if (!file.getName().toLowerCase().endsWith(".mxf")) {
                continue;
            }
            MxfReaderUtility util = new MxfReaderUtility(file);
            System.out.println(file.getName() + ": " + util.getStartTimecode());
        }
    }

    public MxfReaderUtility(final File mxfFile) {
        this.mxfFile = mxfFile;
    }

    public MxfReaderUtility(final Set<GroupNode> groups) {
        this.mxfFile = null;
        this.groups = groups;
        hasBeenRead = true;
    }

    public int getMpStart() throws IOException {
        checkAccess();
        return mpStart;
    }

    public int getMpDuration() throws IOException {
        checkAccess();
        return mpDuration;
    }

    public boolean isMpDropFrame() throws IOException {
        checkAccess();
        return mpDropFrame;
    }

    private Timecode editUnitToTimecode(int editUnits) throws IOException {
        Timecode timecode = Timecode.fromEditUnits(
                getTimecodeBase(),
                editUnits);
        return timecode;
    }

    public TimecodeBase getTimecodeBase() throws IOException {
        checkAccess();
        return TimecodeBase.forParams(roundedTimecodeBase, mpDropFrame);
    }

    public Timecode getStartTimecode() throws IOException {
        checkAccess();
        return editUnitToTimecode(mpStart);
    }

    public Timecode getDurationTimecode() throws IOException {
        checkAccess();
        return editUnitToTimecode(mpDuration);
    }

    private void checkAccess() throws IOException {
        if (!materialPackageAttributesRead) {
            readSingleMaterialAttributes();
        }
    }

    private void checkRead() throws IOException {
        if (hasBeenRead) {
            return;
        }
        reader = new MxfTreeReader(mxfFile);
        reader.setReadClosedCompleteMetadataOnly(true);
        reader.readAll();
        groups = reader.getGroups();
        hasBeenRead = true;
    }

    private void readSingleMaterialAttributes() throws IOException {
        checkRead();
        boolean foundMaterialPackage = false;
        boolean foundTimelineClip = false;
        for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.MaterialPackage.equals(group.ul())) {
                if (foundMaterialPackage) {
                    throw new RuntimeException("found more than one MP");
                }
                foundMaterialPackage = true;
                for (Iterator<GroupNode> ti = group.refs(Metadata.PackageTracks).iterator(); ti.hasNext();) {
                    GroupNode track = ti.next();
                    GroupNode seq = track.ref(Metadata.TrackSegment);
                    for (Iterator<GroupNode> ci = seq.refs(Metadata.ComponentObjects).iterator(); ci.hasNext();) {
                        GroupNode clip = ci.next();
                        if (Groups.TimecodeComponent.equals(clip.ul())) {
                            if (foundTimelineClip) {
                                throw new RuntimeException("found more than one timeline clip");
                            }
                            foundTimelineClip = true;
                            Number editUnitDuration = (Number) clip.value(Metadata.ComponentLength); // duration in edit units
                            int editUnitStart = ((Number)clip.value(Metadata.StartTimecode)).intValue();
                            mpDropFrame = (((Number)clip.value(Metadata.DropFrame)).intValue() == 1);
                            mpDuration = editUnitDuration.intValue();
                            mpStart = editUnitStart;
                            roundedTimecodeBase = ((Number)clip.value(Metadata.FramesPerSecond)).intValue();
                        }
                    }
                }
            }
        }
        if (!foundTimelineClip) {
            throw new RuntimeException("could not find timeline clip");
        }
        materialPackageAttributesRead = true;
    }
}
