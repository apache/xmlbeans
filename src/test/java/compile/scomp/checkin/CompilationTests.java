/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package compile.scomp.checkin;

import compile.scomp.common.CompileCommon;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.XmlOptions.BeanMethod;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.tool.*;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static common.Common.getRootFile;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;


@SuppressWarnings({"SpellCheckingInspection", "ResultOfMethodCallIgnored"})
public class CompilationTests {
    private static final File fwroot = new File(getRootFile());

    //location of files under "cases folder"
    private static final String fileLocation = CompileCommon.fileLocation;
    private static final File outputroot = new File(fwroot, "build/test/output");


    private static final String[] invalidSchemas = {
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <xs:complexType name='base' final='extension'/>\n" +
        "  <xs:complexType name='ext'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:extension base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <xs:complexType name='base' final='#all'/>\n" +
        "  <xs:complexType name='ext'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:extension base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='#all'>\n" +
        "  <xs:complexType name='base'/>\n" +
        "  <xs:complexType name='rest'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:restriction base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='restriction'>\n" +
        "  <xs:complexType name='base'/>\n" +
        "  <xs:complexType name='rest'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:restriction base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",
    };

    static String[] validSchemas = {
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <xs:complexType name='base' final='extension'/>\n" +
        "  <xs:complexType name='rest'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:restriction base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <xs:complexType name='base' final='restriction'/>\n" +
        "  <xs:complexType name='ext'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:extension base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='restriction'>\n" +
        "  <xs:complexType name='base'/>\n" +
        "  <xs:complexType name='ext'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:extension base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='extension'>\n" +
        "  <xs:complexType name='base'/>\n" +
        "  <xs:complexType name='rest'>\n" +
        "    <xs:complexContent>\n" +
        "      <xs:restriction base='base'/>\n" +
        "    </xs:complexContent>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>\n",
    };


    @Test
    public void testJ2EE() {
        deltree(xbeanOutput("compile/scomp/j2ee"));
        // First, compile schema
        File srcdir = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml/src");
        File classesdir = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml/classes");
        File outputjar = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml.jar");
        Parameters params = new Parameters();
        params.setXsdFiles(
            xbeanCase("j2ee/application-client_1_4.xsd"),
            xbeanCase("j2ee/application_1_4.xsd"),
            xbeanCase("j2ee/connector_1_5.xsd"),
            xbeanCase("j2ee/ejb-jar_2_1.xsd"),
            xbeanCase("j2ee/j2ee_1_4.xsd"),
            xbeanCase("j2ee/jsp_2_0.xsd"),
            xbeanCase("j2ee/web-app_2_4.xsd"),
            xbeanCase("j2ee/XML.xsd"));
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        params.setMdefNamespaces(Collections.singleton("http://java.sun.com/xml/ns/j2ee"));
        List<XmlError> errors = new ArrayList<>();
        params.setErrorListener(errors);
        boolean result = SchemaCompiler.compile(params);
        StringWriter message = new StringWriter();
        if (!result)
            dumpErrors(errors, new PrintWriter(message));
        assertTrue("Build failed:" + message, result);
        assertTrue("Cannot find " + outputjar, outputjar.exists());
    }

    @Test
    public void testIncrementalCompilation() throws IOException, XmlException {
        File[] files = new File[]{
            xbeanCase("incr/incr1.xsd"),
            xbeanCase("incr/incr3.xsd"),
            xbeanCase("incr/incr4.xsd"),
            xbeanCase("incr/incr2.xsd"),
            xbeanCase("incr/incr2-1.xsd")
        };
        int n = files.length;
        SchemaDocument.Schema[] schemas = new SchemaDocument.Schema[n - 1];
        SchemaTypeSystem system;
        deltree(xbeanOutput("compile/scomp/incr"));
        File out = xbeanOutput("compile/scomp/incr/out");
        File outincr = xbeanOutput("compile/scomp/incr/outincr");

        for (int i = 0; i < n - 2; i++)
            schemas[i] = SchemaDocument.Factory.parse(files[i]).getSchema();
        // Compile incrementally
        // Initial compile
        schemas[n - 2] = SchemaDocument.Factory.parse(files[n - 2]).getSchema();
        List<XmlError> errors = new ArrayList<>();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
        system = XmlBeans.compileXsd(schemas, builtin, options);
        Assert.assertNotNull("Compilation failed during inititial compile.", system);
        System.out.println("-= Initial Compile =-");

        for (int i = 0; i < system.globalTypes().length; i++) {
            System.out.println("[" + i + "]-" + system.globalTypes()[i].getName());
        }
        for (int i = 0; i < system.globalAttributes().length; i++) {
            System.out.println("[" + i + "]-" + system.globalAttributes()[i].getName());
        }
        for (int i = 0; i < system.globalElements().length; i++) {
            System.out.println("[" + i + "]=" + system.globalElements()[i].getName());
        }

        // Incremental compile
        String url = schemas[n - 2].documentProperties().getSourceName();
        SchemaDocument.Schema[] schemas1 = new SchemaDocument.Schema[1];
        schemas1[0] = SchemaDocument.Factory.parse(files[n - 1]).getSchema();
        schemas1[0].documentProperties().setSourceName(url);
        errors.clear();
        system = XmlBeans.compileXsd(system, schemas1, builtin, options);
        Assert.assertNotNull("Compilation failed during incremental compile.", system);
        SchemaCodeGenerator.saveTypeSystem(system, outincr, null, null, null);
        System.out.println("-= Incremental Compile =-");
        for (int i = 0; i < system.globalTypes().length; i++) {
            System.out.println("[" + i + "]-" + system.globalTypes()[i].getName());
        }
        for (int i = 0; i < system.globalAttributes().length; i++) {
            System.out.println("[" + i + "]-" + system.globalAttributes()[i].getName());
        }
        for (int i = 0; i < system.globalElements().length; i++) {
            System.out.println("[" + i + "]=" + system.globalElements()[i].getName());
        }
        // Now compile non-incrementally for the purpose of comparing the result
        errors.clear();
        schemas[n - 2] = schemas1[0];
        system = XmlBeans.compileXsd(schemas, builtin, options);
        Assert.assertNotNull("Compilation failed during reference compile.", system);
        Filer filer = new FilerImpl(out, null, null, false, false);
        system.save(filer);

        System.out.println("-= Sanity Compile =-");
        for (int i = 0; i < system.globalTypes().length; i++) {
            System.out.println("[" + i + "]-" + system.globalTypes()[i].getName());
        }
        for (int i = 0; i < system.globalAttributes().length; i++) {
            System.out.println("[" + i + "]-" + system.globalAttributes()[i].getName());
        }
        for (int i = 0; i < system.globalElements().length; i++) {
            System.out.println("[" + i + "]=" + system.globalElements()[i].getName());
        }

        // Compare the results
        String oldPropValue = System.getProperty("xmlbeans.diff.diffIndex");
        System.setProperty("xmlbeans.diff.diffIndex", "false");
        errors.clear();
        List<String> diffs = new ArrayList<>();
        Diff.dirsAsTypeSystems(out, outincr, diffs);
        System.setProperty("xmlbeans.diff.diffIndex", oldPropValue == null ? "true" : oldPropValue);
        assertEquals("Differences encountered:" + String.join("\n", diffs), 0, diffs.size());
    }

    @Test
    public void testSchemaBookmarks() throws XmlException, IOException {
        File srcSchema = xbeanCase("../../simple/person/person.xsd");
        // Parse
        SchemaDocument.Schema parsed = SchemaDocument.Factory.parse(srcSchema).getSchema();
        // Navigate to the type definition
        TopLevelComplexType[] cTypes = parsed.getComplexTypeArray();
        boolean found = false;
        int i;
        for (i = 0; i < cTypes.length; i++)
            if ("person".equals(cTypes[i].getName())) {
                found = true;
                break;
            }
        assertTrue("Could not find the \"person\" complex type", found);
        // Set the bookmark
        try (XmlCursor c = cTypes[i].newCursor()) {
            SchemaBookmark sb = new SchemaBookmark("MyBookmark");
            c.setBookmark(sb);
        }
        // Compile it into STS
        SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{parsed},
            XmlBeans.getBuiltinTypeSystem(), null);
        Assert.assertNotNull("Could not compile person.xsd", sts);
        SchemaType personType = sts.findType(QNameHelper.forLNS("person", "http://openuri.org/mytest"));
        Assert.assertNotNull("Could not find the \"person\" schema type", personType);
        // Check that the bookmark made it through
        Object val = personType.getUserData();
        Assert.assertNotNull("Schema user data not found!", val);
        Assert.assertEquals("MyBookmark", val);
    }

    @Test
    public void testSimple() throws MalformedURLException, ClassNotFoundException {
        deltree(xbeanOutput("compile/scomp/simple"));
        // First, compile schema

        // First, compile schema
        File inputfile1 = xbeanCase("../../simple/person/person.xsd");
        File inputfile2 = xbeanCase("../../simple/person/simplec.xsd");

        File srcdir = xbeanOutput("simple/simpletypes/src");


        File classesdir = xbeanOutput("compile/scomp/simple/simpletypes/classes");
        File outputjar = xbeanOutput("compile/scomp/simple/simpletypes.jar");
        Parameters params = new Parameters();
        params.setXsdFiles(inputfile1, inputfile2);
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        assertTrue("Build failed", SchemaCompiler.compile(params));

        // Then, compile java classes
        File javasrc = new File(CompileCommon.fileLocation+"/simple");
        File javaclasses = xbeanOutput("compile/scomp/simple/javaclasses");
        javaclasses.mkdirs();
        File[] testcp = Stream.concat(Stream.of(CodeGenUtil.systemClasspath()), Stream.of(outputjar)).toArray(File[]::new);
        CodeGenUtil.externalCompile(singletonList(javasrc), javaclasses, testcp, true);

        // Then run the test
        URLClassLoader childcl = new URLClassLoader(new URL[]{outputjar.toURI().toURL()}, CompilationTests.class.getClassLoader());
        Class<?> cl = childcl.loadClass("scomp.simple.SimplePersonTest");
        Result result = JUnitCore.runClasses(cl);
        assertEquals(0, result.getFailureCount());
    }

    @Test
    @Ignore
    public void testDownload() {
        deltree(xbeanOutput("compile/scomp/include"));

        {
            // First, compile schema without download and verify failure
            File srcdir = xbeanOutput("compile/scomp/include/shouldfail/src");
            File classesdir = xbeanOutput("compile/scomp/include/shouldfail/classes");
            File outputjar = xbeanOutput("compile/scomp/include/shouldfail.jar");
            Parameters params = new Parameters();
            params.setXsdFiles(xbeanCase("compile/scomp/j2ee/j2ee_1_4.xsd"));
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            assertFalse("Build should have failed", SchemaCompiler.compile(params));
            assertFalse("Should not have created " + outputjar, outputjar.exists());
        }

        {
            // now turn on download and verify success
            File srcdir = xbeanOutput("compile/scomp/include/shouldsucceed/src");
            File classesdir = xbeanOutput("compile/scomp/include/shouldsucceed/classes");
            File outputjar = xbeanOutput("compile/scomp/include/shouldsucceed.jar");
            Parameters params = new Parameters();
            params.setDownload(true);
            params.setXsdFiles(xbeanCase("compile/scomp/j2ee/j2ee_1_4.xsd"));
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            assertTrue("Build failed", SchemaCompiler.compile(params));
            assertTrue("Cannout find " + outputjar, outputjar.exists());
        }
    }

    @Test
    public void testPricequote() {
        deltree(xbeanOutput("compile/scomp/pricequote"));
        // First, compile schema
        File srcdir = xbeanOutput("compile/scomp/pricequote/src");
        File classesdir = xbeanOutput("compile/scomp/pricequote/classes");
        File outputjar = xbeanOutput("compile/scomp/pricequote/pricequote.jar");
        Parameters params = new Parameters();
        params.setXsdFiles(xbeanCase("pricequote/PriceQuote.xsd"));
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        assertTrue("Build failed " + fwroot, SchemaCompiler.compile(params));
        assertTrue("Cannout find " + outputjar, outputjar.exists());
    }

    @Test
    public void testInvalid() throws XmlException {
        for (String schemaFile : invalidSchemas) {
            // Parse the invalid schema files
            SchemaDocument schema = SchemaDocument.Factory.parse(schemaFile);
            // Now compile the invalid schemas, test that they fail
            assertThrows("Schema should have failed to compile:\n" + schemaFile, XmlException.class,
                () -> XmlBeans.loadXsd(schema));
        }
    }

    @Test
    public void testValid() throws XmlException {
        for (String schemaFile : validSchemas) {
            // Parse the valid schema files
            SchemaDocument schema = SchemaDocument.Factory.parse(schemaFile);
            // Compile the valid schemas. They should not fail
            SchemaTypeLoader xs = XmlBeans.loadXsd(schema);
            assertNotNull(xs);
        }
    }

    @Test
    public void partials() throws InterruptedException, IOException {
        String[] files = {"partials/RootDocument.java", "partials/impl/RootDocumentImpl.java"};
        String[] templ = new String[files.length];
        for (int i=0; i<files.length; i++) {
            Path p = xbeanCase(files[i]).toPath();
            templ[i] = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        }

        deltree(xbeanOutput("compile/scomp/partials"));
        File srcdir = xbeanOutput("compile/scomp/partials/src");
        File classesdir = xbeanOutput("compile/scomp/partials/classes");
        File outputjar = xbeanOutput("compile/scomp/partials/partialMethods.jar");
        Parameters params = new Parameters();
        params.setXsdFiles(xbeanCase("partials/partialMethods.xsd"));
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        params.setName("Partials");

        // exclude each bean method and compare with the template with that method excluded too
        for (BeanMethod removeMethod : BeanMethod.values()) {
            Set<BeanMethod> partialMethods = new HashSet<>(Arrays.asList(BeanMethod.values()));
            partialMethods.remove(removeMethod);
            params.setPartialMethods(partialMethods);
            SchemaCompiler.compile(params);

            for (int i=0; i<files.length; i++) {
                Path p = new File(srcdir, files[i]).toPath();
                String act = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                String exp = templ[i];
                // remove marker + content
                exp = exp.replaceAll("(?m)^.*<" + removeMethod + ">(?s).+?</" + removeMethod + ">$\\n", "");
                // activate alternative content
                exp = exp.replaceAll("(?m)^.*//.*</?" + removeMethod + "_ELSE>$\\n", "");
                // remove other alternative content
                exp = exp.replaceAll("(?m)^.*<[^>]+_ELSE>(?s).+?</[^>]+_ELSE>$\\n", "");
                // remove unused markers
                exp = exp.replaceAll("(?m)^.*//.*<.*>$\\n", "");
                assertEquals(files[i] + " - " + removeMethod + " failed", exp, act);
            }
        }
    }

    @Test
    public void annotation2javadoc() throws Exception {
        deltree(xbeanOutput("compile/scomp/javadoc"));
        File srcdir = xbeanOutput("compile/scomp/javadoc/src");
        File classesdir = xbeanOutput("compile/scomp/javadoc/classes");
        File outputjar = xbeanOutput("compile/scomp/javadoc/javadoc.jar");
        Parameters params = new Parameters();
        params.setXsdFiles(xbeanCase("schemacompiler/javadoc.xsd"));
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        params.setName("javadoc");
        SchemaCompiler.compile(params);

        Path p = new File(srcdir, "javadoc/RootDocument.java").toPath();
        String act = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        assertFalse(act.contains("* / heck, I'm smart"));

        params.setCopyAnn(true);
        SchemaCompiler.compile(params);

        act = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        assertTrue(act.contains("* / heck, I'm smart"));
    }

    //TESTENV:

    private static void dumpErrors(List<XmlError> errors, PrintWriter out) {
        // Display the errors
        for (XmlError error : errors) {
            if (error.getSeverity() == XmlError.SEVERITY_ERROR)
                out.println(error.toString());
        }
    }

    private static File xbeanCase(String str) {
        return new File(fileLocation, str);
    }

    private static File xbeanOutput(String str) {
        File result = (new File(outputroot, str));
        File parentdir = result.getParentFile();
        parentdir.mkdirs();
        return result;
    }

    private static void deltree(File dir) {
        if (!dir.exists()) {
            return;
        }

        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                for (String s : list) {
                    deltree(new File(dir, s));
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            if (dir.delete()) {
                return;
            }
            try {
                System.out.println("Sleep 1s and try do delete it again: " + dir.getCanonicalPath());
                Thread.sleep(1000);
            } catch (InterruptedException|IOException ignored) {
            }
        }

        throw new IllegalStateException("Could not delete " + dir);
    }
}
