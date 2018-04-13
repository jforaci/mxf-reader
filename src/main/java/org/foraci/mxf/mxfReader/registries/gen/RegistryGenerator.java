package org.foraci.mxf.mxfReader.registries.gen;

import java.io.*;
import java.util.HashSet;

/**
 * Generates the dictionaries for the metadata and labels registries
 *
 * @author jforaci
 */
public class RegistryGenerator
{
    private static void parseMetadataDictionary(File file) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        HashSet<String> names = new HashSet<String>();
        System.out.println("package org.foraci.mxf.mxfReader.registries;");
        System.out.println("public class MetadataGen {");
        final int maxStatments = 500;
        int stmtCount = 0;
        int classCount = 0;
        while ((line = in.readLine()) != null) {
            String[] fields = line.split(",");
            if (!"Metadata".equals(fields[1])) {
                System.err.print(line);
                continue;
            }
            if (!"Leaf".equals(fields[3])) {
                if (!"Node".equals(fields[3])) {
                    System.err.print(line);
                }
                continue;
            }
            StringBuffer bytesBuffer = new StringBuffer("{");
            for (int i = 5; i < 5 + 16; i++) {
                bytesBuffer.append("0x").append(fields[i]).append(",");
            }
            bytesBuffer.append("}");
            String baseName = fields[30].replaceAll("[./@]+", "_").replaceAll("[\" ,()\t-]+", "");
            String name = baseName;
            int count = 1;
            while (names.contains(name)) {
                name = baseName + count;
                count++;
            }
            if (stmtCount == 0) {
                stmtCount = maxStatments;
                if (classCount > 0) {
                    System.out.println("}");
                }
                classCount++;
//                System.out.println("interface Metadata" + classCount + " {");
                System.out.println("private static void init" + classCount + "() {");
            }
//            System.out.println("public static final Key " + name + " = Registry.add(\"" + name + "\", new byte[] " + bytesBuffer.toString() + ");");
            System.out.println(name + " = Registry.add(\"" + name + "\", new byte[] " + bytesBuffer.toString() + ");");
            names.add(name);
            stmtCount--;
        }
        System.out.println("}");
        for (String name : names) {
            System.out.println("public static UL " + name + ";");
        }
        System.out.println("static void init() {");
        for (int i = 1; i <= classCount; i++) {
            System.out.println("init" + i + "();");
        }
        System.out.println("}"); // end of init()
        System.out.println("}"); // Metadata end of class
//        System.out.println("public class Metadata implements");
//        for (int i = 1; i <= classCount; i++) {
//            System.out.print(" Metadata" + i);
//            if (i < classCount) {
//                System.out.print(",");
//            }
//        }
//        System.out.println("{}");
        in.close();
    }

    private static void parseLabelsRegistry(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        HashSet<String> names = new HashSet<String>();
        System.out.println("package org.foraci.mxf.mxfReader.registries;");
        System.out.println("public class Labels {");
        final int maxStatments = 500;
        int stmtCount = 0;
        int classCount = 0;
        while ((line = in.readLine()) != null) {
            String[] fields = line.split(",");
            if (!"Leaf".equals(fields[1])) {
                if (!"Node".equals(fields[1]) && !"Spacer".equals(fields[1]) && !"Depr".equals(fields[1])) {
                    System.err.print(line);
                }
                continue;
            }
            StringBuffer bytesBuffer = new StringBuffer("{");
            String[] octets = (fields[2] + "." + fields[3]).split("\\.");
            for (int i = 0; i < octets.length; i++) {
                bytesBuffer.append("0x").append(octets[i]).append(",");
            }
            bytesBuffer.append("}");
            String baseName = fields[5].replaceAll("[./@]+", "_").replaceAll("[\" ,()\t-]+", "");
            String name = baseName;
            int count = 1;
            while (names.contains(name)) {
                name = baseName + count;
                count++;
            }
            if (stmtCount == 0) {
                stmtCount = maxStatments;
                if (classCount > 0) {
                    System.out.println("}");
                }
                classCount++;
//                System.out.println("interface Metadata" + classCount + " {");
                System.out.println("private static void init" + classCount + "() {");
            }
//            System.out.println("public static final Key " + name + " = Registry.add(\"" + name + "\", new byte[] " + bytesBuffer.toString() + ");");
            String description = fields[6].replaceAll("[\"]+", "");
            System.out.println(name + " = Registry.add(\"" + name + "\", new byte[] " + bytesBuffer.toString() + ", \"" + description + "\");");
            names.add(name);
            stmtCount--;
        }
        System.out.println("}");
        for (String name : names) {
            System.out.println("public static UL " + name + ";");
        }
        System.out.println("static void init() {");
        for (int i = 1; i <= classCount; i++) {
            System.out.println("init" + i + "();");
        }
        System.out.println("}"); // end of init()
        System.out.println("}"); // Labels end of class
        in.close();
    }

    public static void main(String[] args) throws IOException
    {
        parseMetadataDictionary(new File("RP210v11.csv"));
        parseLabelsRegistry(new File("RP224v10-publication-20081215.csv"));
    }
}
