# mxf-reader
A library to read and parse MXF media as specified by [SMPTE 377](https://en.wikipedia.org/wiki/Material_Exchange_Format) with support for the ["Generic Container"](https://en.wikipedia.org/wiki/Material_Exchange_Format#Generic_containers) (SMPTE 379M)

## Quick start
Reading an MXF file:
```Java
File file = new File(fileName);
mxfReader = new MxfReader(file);
mxfReader.setDebugPrint(true);
mxfReader.setMetadataReadMode(MxfReader.MetadataReadMode.All);
mxfReader.setParseEssenceElements(true); // force parse GC essence (media) elements
mxfReader.setParseSystemElements(true); // force parse of GC system elements
long startTime = System.currentTimeMillis();
mxfReader.readAll(); // start parsing
```

## Navigating the metadata
Use `MxfTreeReader` to have a DOM-like tree of metadata to navigate. This tree is made of groups, represented by `GroupNode` and values, represented by `LeafNode`. Call `reader.getGroups()` to get a set of all the top-level `GroupNode`s. You can search for specific groups by matching its `ul` tag with one from the `Groups` constants. For example, `Groups.EssenceContainerData` for the essence container data set. Likewise, `Metadata` can be used to reference individual values. For example, `Metadata.EssenceStreamID` which specifies the essence container's stream identifier. Putting it all together:
```Java
// "groups" is Set<GroupNode>
for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
	GroupNode group = i.next();
	if (Groups.EssenceContainerData.equals(group.ul())) {
		Number bodySid = (Number) group.value(Metadata.EssenceStreamID);
		UMID filePackageId = (UMID) group.value(Metadata.LinkedPackageID);
		addTracks(groups, bodySid, filePackageId);
	}
}
```
## Extracting essence
This is just a matter of implementing `EssenceContainerOutputController`. A default implementation that indiscriminately writes each physical track to its own file can be found in `EssenceFileOutputController`, and might be a good place to start implementing your own. In order to use the implementation, you will have to override `MxfReader.createEssenceFileOutputController(File file)` like this:
```Java
@Override
protected EssenceContainerOutputController createEssenceFileOutputController(File file)
{
	return new MyOutputController(outputStream);
}
```

MXF is allowed a preamble called a "run-in". Some non-compliant files may take more liberties and you may find it useful to set `mxfReader.setEnableRuninAnywhere(true)` in order to process them.
## Metadata read mode
The `MetadataReadMode` can be set to `All`, which will read everything including the metadata header, essence container and all system and essence elements. `MxfReader` will always attempt to use the RIP to hop through the file, if it's available. If you're just after the metadata, `FooterOnly` will read only the footer metadata set which should be final. Otherwise, `HeaderOnly` will only read the header partition;  (`FromFooterAll` is not used at this time).

## To-do
- Currently, the API is file-oriented. I'd like to change this to better support streams, e.g. `InputStream`, etc. This should be realatively cosmetic except for some places where I explicitly rely on an EOF.
