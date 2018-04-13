package org.foraci.mxf.mxfReader.registries;

import org.foraci.mxf.mxfReader.parsers.*;
import org.foraci.mxf.mxfReader.NumericUL;

/**
 * Metadata dictionary registry
 * @author jforaci
 */
public class Metadata extends MetadataGen {
    static void init() {
        MetadataGen.init();
        GUID.setParserClass(UIDParser.class);
        GenerationID.setParserClass(UIDParser.class);
        LinkedGenerationID.setParserClass(UIDParser.class);
        // preface
        ContainerLastModificationDateTime.setParserClass(TimestampParser.class);
        PrimaryPackage.setParserClass(UIDParser.class);
        IdentificationList.setParserClass(UIDBatchParser.class);
        Packages.setParserClass(UIDBatchParser.class);
        EssenceContainers.setParserClass(ULBatchParser.class);
        Content.setParserClass(UIDParser.class);
        OperationalPatternUL.setParserClass(ULParser.class);
        DescriptiveMetadataSchemes.setParserClass(ULBatchParser.class);
        // identification
        ModificationDateTime.setParserClass(TimestampParser.class);
        ApplicationSupplierName1.setParserClass(UTF16StringParser.class);
        ApplicationName1.setParserClass(UTF16StringParser.class);
        ApplicationVersionString1.setParserClass(UTF16StringParser.class);
        ApplicationProductID.setParserClass(UIDParser.class);
        ApplicationPlatform1.setParserClass(UTF16StringParser.class);
        // content storage
        EssenceData.setParserClass(UIDBatchParser.class);
        // packages
        PackageID.setParserClass(UMIDParser.class);
        PackageName.setParserClass(ISO88591StringParser.class);
        PackageName1.setParserClass(UTF16StringParser.class);
        CreationDateTime.setParserClass(TimestampParser.class);
        PackageLastModificationDateTime.setParserClass(TimestampParser.class);
        Tracks.setParserClass(UIDBatchParser.class);
        // source package
        EssenceDescription.setParserClass(UIDParser.class);
        // generic descriptor
        EssenceLocators.setParserClass(UIDBatchParser.class);
        // network locator
        URL1.setParserClass(UTF16StringParser.class);
        // essence container data
        LinkedPackageID.setParserClass(UMIDParser.class);
        EssenceStreamID = Registry.add(new NumericUL(EssenceStreamID.getName(), EssenceStreamID.getKey(), false, 4));
        IndexStreamID = Registry.add(new NumericUL(IndexStreamID.getName(), IndexStreamID.getKey(), false, 4));
        // tracks
        TrackName.setParserClass(ISO88591StringParser.class);
        TrackName1.setParserClass(UTF16StringParser.class);
        TrackNumber = Registry.add(new NumericUL(TrackNumber.getName(), TrackNumber.getKey(), false, 4));
        TrackID = Registry.add(new NumericUL(TrackID.getName(), TrackID.getKey(), false, 4));
        TimelineEditRate.setParserClass(RationalParser.class);
        Origin = Registry.add(new NumericUL(Origin.getName(), Origin.getKey(), true, 8));
        Segment.setParserClass(UIDParser.class);
        // sequence
        ComponentDataDefinition.setParserClass(ULParser.class);
        ComponentLength = Registry.add(new NumericUL(ComponentLength.getName(), ComponentLength.getKey(), true, 8));
        ComponentsinSequence.setParserClass(UIDBatchParser.class);
        // timecode component
        RoundedTimecodeTimebase = Registry.add(new NumericUL(RoundedTimecodeTimebase.getName(), RoundedTimecodeTimebase.getKey(), false, 2));
        StartTimecode = Registry.add(new NumericUL(StartTimecode.getName(), StartTimecode.getKey(), true, 8));
        DropFrame = Registry.add(new NumericUL(DropFrame.getName(), DropFrame.getKey(), false, 1));
        // source clip
        StartTimeRelativetoReference1 = Registry.add(new NumericUL(StartTimeRelativetoReference1.getName(), StartTimeRelativetoReference1.getKey(), true, 8));
        SourcePackageID.setParserClass(UMIDParser.class);
        SourceTrackID = Registry.add(new NumericUL(SourceTrackID.getName(), SourceTrackID.getKey(), false, 4));
        // multiple descriptor
        FileDescriptors.setParserClass(UIDBatchParser.class);

        // Agility audio
        SampleRate.setParserClass(RationalParser.class);
        EssenceLength = Registry.add(new NumericUL(EssenceLength.getName(), EssenceLength.getKey(), true, 8));
        EssenceContainerFormat.setParserClass(ULParser.class);
        BitsPerAudioSample = Registry.add(new NumericUL(BitsPerAudioSample.getName(), BitsPerAudioSample.getKey(), false, 4));
        ChannelCount = Registry.add(new NumericUL(ChannelCount.getName(), ChannelCount.getKey(), false, 4));
        AudioSampleRate1.setParserClass(RationalParser.class);
        BlockAlign = Registry.add(new NumericUL(BlockAlign.getName(), BlockAlign.getKey(), false, 2));
        AverageBytesPerSecond = Registry.add(new NumericUL(AverageBytesPerSecond.getName(), AverageBytesPerSecond.getKey(), false, 4));
        LinkedTrackID = Registry.add(new NumericUL(LinkedTrackID.getName(), LinkedTrackID.getKey(), false, 4));
        // Agility video
        PresentationAspectRatio.setParserClass(RationalParser.class);
        FrameLayout = Registry.add(new NumericUL(FrameLayout.getName(), FrameLayout.getKey(), false, 1)); // TODO: enum
        HorizontalSubsampling = Registry.add(new NumericUL(HorizontalSubsampling.getName(), HorizontalSubsampling.getKey(), false, 4));
        ColorSiting = Registry.add(new NumericUL(ColorSiting.getName(), ColorSiting.getKey(), false, 1)); // TODO: enum
        SampledHeight = Registry.add(new NumericUL(SampledHeight.getName(), SampledHeight.getKey(), false, 4));
        SampledWidth = Registry.add(new NumericUL(SampledWidth.getName(), SampledWidth.getKey(), false, 4));
        SampledXOffset = Registry.add(new NumericUL(SampledXOffset.getName(), SampledXOffset.getKey(), true, 4));
        SampledYOffset = Registry.add(new NumericUL(SampledYOffset.getName(), SampledYOffset.getKey(), true, 4));
        DisplayHeight = Registry.add(new NumericUL(DisplayHeight.getName(), DisplayHeight.getKey(), false, 4));
        DisplayWidth = Registry.add(new NumericUL(DisplayWidth.getName(), DisplayWidth.getKey(), false, 4));
        DisplayXOffset = Registry.add(new NumericUL(DisplayXOffset.getName(), DisplayXOffset.getKey(), true, 4));
        DisplayYOffset = Registry.add(new NumericUL(DisplayYOffset.getName(), DisplayYOffset.getKey(), true, 4));
        StoredHeight = Registry.add(new NumericUL(StoredHeight.getName(), StoredHeight.getKey(), false, 4));
        StoredWidth = Registry.add(new NumericUL(StoredWidth.getName(), StoredWidth.getKey(), false, 4));
        BlackReferenceLevel = Registry.add(new NumericUL(BlackReferenceLevel.getName(), BlackReferenceLevel.getKey(), false, 4));
        WhiteReferenceLevel = Registry.add(new NumericUL(WhiteReferenceLevel.getName(), WhiteReferenceLevel.getKey(), false, 4));
        CaptureGammaEquation2.setParserClass(ULParser.class);
        FieldDominance = Registry.add(new NumericUL(FieldDominance.getName(), FieldDominance.getKey(), false, 1));
        // ... skipped some here ...
        BitRate = Registry.add(new NumericUL(BitRate.getName(), BitRate.getKey(), false, 4));
        //SignalStandard = ...? // TODO: enum
    }
}
