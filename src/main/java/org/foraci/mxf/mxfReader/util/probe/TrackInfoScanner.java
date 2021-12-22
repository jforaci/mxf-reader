package org.foraci.mxf.mxfReader.util.probe;

import org.foraci.mxf.mxfReader.MxfReader;
import org.foraci.mxf.mxfReader.MxfTreeReader;
import org.foraci.mxf.mxfReader.UMID;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Scans an S377 file for a list of tracks
 *
 * @author Joe Foraci
 */
public class TrackInfoScanner {
    private final File file;

    private TrackInfoScanner(File file) {
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        new TrackInfoScanner(new File(args[0])).scan();
    }

    public void scan() throws IOException {
        MxfTreeReader reader = new MxfTreeReader(file);
        reader.setMetadataReadMode(MxfReader.MetadataReadMode.HeaderOnly);
        reader.readAll();
        readTracks(reader);
        scanEssence();
    }

    private void scanEssence() throws IOException {
        MxfTrackInfoReader reader = new MxfTrackInfoReader(file, exportableTrackList);
        reader.setMetadataReadMode(MxfReader.MetadataReadMode.All);
        reader.setParseEssenceElements(true);
        reader.setParseSystemElements(true);
        reader.readAll();
    }

    List<EssenceTrack> exportableTrackList = new ArrayList<EssenceTrack>();

    private void readTracks(MxfTreeReader reader) {
        exportableTrackList.clear();
        parseGroupsForEssenceContainers(reader.getGroups());
//        if (loadOptionsPanel.isFollowExternalReferences()) {
//            for (File file : getExternalFileList()) {
//                MxfTreeReader reader = new MxfTreeReader(file);
////                reader.setReadHeaderOnly(true);
//                try {
//                    reader.readAll();
//                } catch (Exception e) {
//                    error("Error parsing external file " + file, e);
//                    continue;
//                }
//                parseGroupsForEssenceContainers(file, reader.getGroups());
//            }
//        }
    }

    private void parseGroupsForEssenceContainers(Set<GroupNode> groups) {
        for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.EssenceContainerData.equals(group.ul())) {
                Number bodySid = (Number) group.value(Metadata.EssenceStreamID);
                UMID filePackageId = (UMID) group.value(Metadata.LinkedPackageID);
                addTracks(groups, bodySid, filePackageId);
            }
        }
    }

    private void addTracks(Set<GroupNode> groups, Number bodySid, UMID filePackageId) {
        for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.SourcePackage.equals(group.ul())) {
                GroupNode descriptor = group.ref(Metadata.EssenceDescription);
                if (descriptor == null || descriptor.find(Metadata.Locators) != null) {
                    continue;
                }
                UMID packageId = (UMID) group.value(Metadata.PackageID);
                if (packageId.equals(filePackageId)) {
                    LeafNode tracksNode = (LeafNode) group.find(Metadata.PackageTracks);
                    for (Iterator<GroupNode> t = tracksNode.refs().iterator(); t.hasNext();) {
                        GroupNode track = t.next();
                        // SMPTE 377 says to treat non-zero track numbers in Lower-level Source
                        // Packages (and Material Packages) as "dark" metadata; however we're
                        // currently looking at Source Packages that are referenced as a File
                        // Package from the Essence Container Data set, so we're ok
                        long trackNumber = ((Number)track.value(Metadata.EssenceTrackNumber)).longValue();
                        long trackId = ((Number)track.value(Metadata.TrackID)).longValue();
                        if (trackNumber == 0) {
                            continue;
                        }
                        exportableTrackList.add(
                                new EssenceTrack(file, bodySid.longValue(), trackNumber, trackId, group, track));
                    }
                }
            }
        }
    }
}
