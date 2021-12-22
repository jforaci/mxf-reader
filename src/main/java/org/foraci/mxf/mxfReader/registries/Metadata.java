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
        InstanceID.setParserClass(UIDParser.class);
        GenerationID.setParserClass(UIDParser.class);
        LinkedGenerationID.setParserClass(UIDParser.class);
        // preface
        FileLastModified.setParserClass(TimestampParser.class);
        PrimaryPackage.setParserClass(UIDParser.class);
        IdentificationList.setParserClass(UIDBatchParser.class);
        Packages.setParserClass(UIDBatchParser.class);
        EssenceContainers.setParserClass(ULBatchParser.class);
        ContentStorageObject.setParserClass(UIDParser.class);
        OperationalPattern.setParserClass(ULParser.class);
        DescriptiveSchemes.setParserClass(ULBatchParser.class);
        // identification
        FileModificationDate.setParserClass(TimestampParser.class);
        ApplicationSupplierName.setParserClass(UTF16StringParser.class);
        ApplicationName.setParserClass(UTF16StringParser.class);
        ApplicationVersionString.setParserClass(UTF16StringParser.class);
        ApplicationProductID.setParserClass(UIDParser.class);
        ApplicationPlatform.setParserClass(UTF16StringParser.class);
        // content storage
        EssenceDataObjects.setParserClass(UIDBatchParser.class);
        // packages
        PackageID.setParserClass(UMIDParser.class);
        PackageName_ISO7.setParserClass(ISO88591StringParser.class);
        PackageName.setParserClass(UTF16StringParser.class);
        CreationTime.setParserClass(TimestampParser.class);
        PackageLastModified.setParserClass(TimestampParser.class);
        PackageTracks.setParserClass(UIDBatchParser.class);
        // source package
        EssenceDescription.setParserClass(UIDParser.class);
        // generic descriptor
        Locators.setParserClass(UIDBatchParser.class);
        // network locator
        URL.setParserClass(UTF16StringParser.class);
        // essence container data
        LinkedPackageID.setParserClass(UMIDParser.class);
        EssenceStreamID = Registry.add(new NumericUL(EssenceStreamID.getName(), EssenceStreamID.getKey(), false, 4));
        IndexStreamID = Registry.add(new NumericUL(IndexStreamID.getName(), IndexStreamID.getKey(), false, 4));
        // tracks
        TrackName_ISO7.setParserClass(ISO88591StringParser.class);
        TrackName.setParserClass(UTF16StringParser.class);
        EssenceTrackNumber = Registry.add(new NumericUL(EssenceTrackNumber.getName(), EssenceTrackNumber.getKey(), false, 4));
        TrackID = Registry.add(new NumericUL(TrackID.getName(), TrackID.getKey(), false, 4));
        EditRate.setParserClass(RationalParser.class);
        Origin = Registry.add(new NumericUL(Origin.getName(), Origin.getKey(), true, 8));
        TrackSegment.setParserClass(UIDParser.class);
        // sequence
        ComponentDataDefinition.setParserClass(ULParser.class);
        ComponentLength = Registry.add(new NumericUL(ComponentLength.getName(), ComponentLength.getKey(), true, 8));
        ComponentObjects.setParserClass(UIDBatchParser.class);
        // timecode component
        FramesPerSecond = Registry.add(new NumericUL(FramesPerSecond.getName(), FramesPerSecond.getKey(), false, 2));
        StartTimecode = Registry.add(new NumericUL(StartTimecode.getName(), StartTimecode.getKey(), true, 8));
        DropFrame = Registry.add(new NumericUL(DropFrame.getName(), DropFrame.getKey(), false, 1));
        // source clip
        StartPosition = Registry.add(new NumericUL(StartPosition.getName(), StartPosition.getKey(), true, 8));
        SourcePackageID.setParserClass(UMIDParser.class);
        SourceTrackID = Registry.add(new NumericUL(SourceTrackID.getName(), SourceTrackID.getKey(), false, 4));
        // multiple descriptor
        FileDescriptors.setParserClass(UIDBatchParser.class);

        // Agility audio
        SampleRate.setParserClass(RationalParser.class);
        EssenceLength = Registry.add(new NumericUL(EssenceLength.getName(), EssenceLength.getKey(), true, 8));
        EssenceContainerFormat.setParserClass(ULParser.class);
        QuantizationBits = Registry.add(new NumericUL(QuantizationBits.getName(), QuantizationBits.getKey(), false, 4));
        ChannelCount = Registry.add(new NumericUL(ChannelCount.getName(), ChannelCount.getKey(), false, 4));
        AudioSampleRate.setParserClass(RationalParser.class);
        BlockAlign = Registry.add(new NumericUL(BlockAlign.getName(), BlockAlign.getKey(), false, 2));
        AverageBytesPerSecond = Registry.add(new NumericUL(AverageBytesPerSecond.getName(), AverageBytesPerSecond.getKey(), false, 4));
        LinkedTrackID = Registry.add(new NumericUL(LinkedTrackID.getName(), LinkedTrackID.getKey(), false, 4));
        // Agility video
        ImageAspectRatio.setParserClass(RationalParser.class);
        FrameLayout.setParserClass(FrameLayoutParser.class);
        PictureCompression.setParserClass(ULParser.class);
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
        BlackRefLevel = Registry.add(new NumericUL(BlackRefLevel.getName(), BlackRefLevel.getKey(), false, 4));
        WhiteRefLevel = Registry.add(new NumericUL(WhiteRefLevel.getName(), WhiteRefLevel.getKey(), false, 4));
        TransferCharacteristic.setParserClass(ULParser.class);
        FieldDominance = Registry.add(new NumericUL(FieldDominance.getName(), FieldDominance.getKey(), false, 1));
        // ... skipped some here ...
        BitRate = Registry.add(new NumericUL(BitRate.getName(), BitRate.getKey(), false, 4));
        //SignalStandard = ...? // TODO: enum
    }
}
