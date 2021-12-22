package org.foraci.mxf.mxfReader.registries.gen;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Generates the dictionaries for the metadata and labels registries
 *
 * @author jforaci
 */
public class RegistryGenerator
{
    private static final int maxStatements = 500;

    private static class LabelXmlHandler extends DefaultHandler {
        Map<String,String> names = new HashMap<>();
        String last;
        StringBuilder name, value = null;
        boolean isLeaf = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            last = qName;
            switch (qName) {
                case "Entry":
                    break;
                case "Symbol": name = new StringBuilder(); break;
                case "UL": value = new StringBuilder(); break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            last = "";
            switch (qName) {
                case "Entry":
                    if (isLeaf) {
                        String clean = value.toString().substring(value.toString().lastIndexOf(':')+1);
                        clean = clean.replaceAll("\\.", "");
                        StringBuilder bytes = new StringBuilder();
                        for (int i = 0; i < clean.length(); i+=2) {
                            bytes.append("0x").append(clean.charAt(i)).append(clean.charAt(i+1)).append(",");
                        }
                        names.put(name.toString(), bytes.toString());
                    }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch (last) {
                case "UL": value.append(ch, start, length); break;
                case "Symbol": name.append(ch, start, length); break;
                case "Kind":
                    isLeaf = "LEAF".equals(String.valueOf(ch, start, length));
                    break;
            }
        }

        public Map<String, String> getNames() {
            return names;
        }
    }

    private static void parseLabelsRegistry(File file) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        System.out.println("package org.foraci.mxf.mxfReader.registries;");
        System.out.println("public class Labels {");
        LabelXmlHandler dh = new LabelXmlHandler();
        try {
            parser.parse(new FileInputStream(file), dh);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        for (String name : dh.getNames().keySet()) {
            System.out.println("public static UL " + name + ";");
        }
        int count = 0, methodCount = 0;
        for (String name : dh.getNames().keySet()) {
            if (count == maxStatements) {
                System.out.println("}");
                count = 0;
                methodCount++;
            }
            if (count == 0) {
                System.out.println("private static void init" + methodCount + "() {");
            }
            System.out.println(name + "=Registry.add(\"" + name.toString() + "\","
                                + "new byte[] {"+ dh.getNames().get(name) + "});");
            count++;
        }
        System.out.println("}\nstatic void init(){");
        for (int i = 0; i < methodCount+1; i++) {
            System.out.println("init" + i + "();");
        }
        System.out.println("}}");
    }

    private static void parseMetadataRegistry(File file) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        System.out.println("package org.foraci.mxf.mxfReader.registries;");
        System.out.println("public class MetadataGen {");
        LabelXmlHandler dh = new LabelXmlHandler();
        try {
            parser.parse(new FileInputStream(file), dh);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        for (String name : dh.getNames().keySet()) {
            System.out.println("public static UL " + name + ";");
        }
        int count = 0, methodCount = 0;
        for (String name : dh.getNames().keySet()) {
            if (count == maxStatements) {
                System.out.println("}");
                count = 0;
                methodCount++;
            }
            if (count == 0) {
                System.out.println("private static void init" + methodCount + "() {");
            }
            System.out.println(name + "=Registry.add(\"" + name.toString() + "\","
                    + "new byte[] {"+ dh.getNames().get(name) + "});");
            count++;
        }
        System.out.println("}\nstatic void init(){");
        for (int i = 0; i < methodCount+1; i++) {
            System.out.println("init" + i + "();");
        }
        System.out.println("}}");
    }

    public static void main(String[] args) throws IOException
    {
        parseLabelsRegistry(new File("labels.xml"));
        parseMetadataRegistry(new File("elements.xml"));
    }
}
