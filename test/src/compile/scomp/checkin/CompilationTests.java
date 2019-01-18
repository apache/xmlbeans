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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.tool.Diff;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tools.util.TestRunUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class CompilationTests {
    @Test
    public void testJ2EE() throws Throwable {
        deltree(xbeanOutput("compile/scomp/j2ee"));
        // First, compile schema
        File srcdir = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml/src");
        File classesdir = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml/classes");
        File outputjar = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{
            xbeanCase("j2ee/application-client_1_4.xsd"),
            xbeanCase("j2ee/application_1_4.xsd"),
            xbeanCase("j2ee/connector_1_5.xsd"),
            xbeanCase("j2ee/ejb-jar_2_1.xsd"),
            xbeanCase("j2ee/j2ee_1_4.xsd"),
            xbeanCase("j2ee/jsp_2_0.xsd"),
            xbeanCase("j2ee/web-app_2_4.xsd"),
            xbeanCase("j2ee/XML.xsd")
        });
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        params.setMdefNamespaces(Collections.singleton("http://java.sun.com/xml/ns/j2ee"));
        List errors = new ArrayList();
        params.setErrorListener(errors);
        boolean result = SchemaCompiler.compile(params);
        StringWriter message = new StringWriter();
        if (!result)
            dumpErrors(errors, new PrintWriter(message));
        assertTrue("Build failed:" + message, result);
        assertTrue("Cannot find " + outputjar, outputjar.exists());
    }

    @Test
    public void testIncrementalCompilation() throws Throwable {
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
        List errors = new ArrayList();
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
        SchemaCodeGenerator.saveTypeSystem(system, out, null, null, null);

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
        Diff.dirsAsTypeSystems(out, outincr, errors);
        System.setProperty("xmlbeans.diff.diffIndex", oldPropValue == null ? "true" : oldPropValue);
        if (errors.size() > 0) {
            StringWriter message = new StringWriter();
            for (int i = 0; i < errors.size(); i++)
                message.write(((String) errors.get(i)) + "\n");
            fail("Differences encountered:" + message);
        }

    }

    @Test
    public void testSchemaBookmarks() throws Throwable {
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
        SchemaBookmark sb = new SchemaBookmark("MyBookmark");
        cTypes[i].newCursor().setBookmark(sb);
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
    @Ignore
    public void testSimple() throws Throwable {
        deltree(xbeanOutput("compile/scomp/simple"));
        // First, compile schema

        // First, compile schema
        File inputfile1 = xbeanCase("../../simple/person.xsd");
        File inputfile2 = xbeanCase("../../simple/simplec.xsd");

        File srcdir = xbeanOutput("simple/simpletypes/src");


        File classesdir = xbeanOutput("compile/scomp/simple/simpletypes/classes");
        File outputjar = xbeanOutput("compile/scomp/simple/simpletypes.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{inputfile1, inputfile2});
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        assertTrue("Build failed", SchemaCompiler.compile(params));

        // Then, compile java classes
        File javasrc = xbeanCase("simple/javasrc");
        File javaclasses = xbeanOutput("compile/scomp/simple/javaclasses");
        javaclasses.mkdirs();
        List<File> testcp = Arrays.asList(CodeGenUtil.systemClasspath());
        testcp.add(outputjar);
        CodeGenUtil.externalCompile(Arrays.asList(javasrc), javaclasses, testcp.toArray(new File[0]), true);

        // Then run the test
        testcp.add(javaclasses);
        TestRunUtil.run("SimplePersonTest", new File[]{outputjar, javaclasses});
    }

    @Test
    @Ignore
    public void testDownload() throws Throwable {
        deltree(xbeanOutput("compile/scomp/include"));

        {
            // First, compile schema without download and verify failure
            File srcdir = xbeanOutput("compile/scomp/include/shouldfail/src");
            File classesdir = xbeanOutput("compile/scomp/include/shouldfail/classes");
            File outputjar = xbeanOutput("compile/scomp/include/shouldfail.jar");
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setXsdFiles(new File[]{
                xbeanCase("compile/scomp/j2ee/j2ee_1_4.xsd")
            });
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            assertTrue("Build should have failed", !SchemaCompiler.compile(params));
            assertTrue("Should not have created " + outputjar, !outputjar.exists());
        }

        {
            // now turn on download and verify success
            File srcdir = xbeanOutput("compile/scomp/include/shouldsucceed/src");
            File classesdir = xbeanOutput("compile/scomp/include/shouldsucceed/classes");
            File outputjar = xbeanOutput("compile/scomp/include/shouldsucceed.jar");
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setDownload(true);
            params.setXsdFiles(new File[]{
                xbeanCase("compile/scomp/j2ee/j2ee_1_4.xsd")
            });
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            assertTrue("Build failed", SchemaCompiler.compile(params));
            assertTrue("Cannout find " + outputjar, outputjar.exists());
        }
    }

    @Test
    @Ignore
    public void testPricequote() throws Throwable {
        deltree(xbeanOutput("compile/scomp/pricequote"));
        // First, compile schema
        File srcdir = xbeanOutput("compile/scomp/pricequote/src");
        File classesdir = xbeanOutput("compile/scomp/pricequote/classes");
        File outputjar = xbeanOutput("compile/scomp/pricequote/pricequote.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{
            xbeanCase("compile/scomp/pricequote/PriceQuote.xsd")});
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        assertTrue("Build failed " + fwroot, SchemaCompiler.compile(params));
        assertTrue("Cannout find " + outputjar, outputjar.exists());
    }

    private static String[] invalidSchemas = {
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
    @Ignore
    public void testFinal() throws Throwable {
        SchemaDocument[] schemas = new SchemaDocument[invalidSchemas.length];

        // Parse the invalid schema files
        for (int i = 0; i < invalidSchemas.length; i++)
            schemas[i] = SchemaDocument.Factory.parse(invalidSchemas[i]);

        // Now compile the invalid schemas, test that they fail
        for (int i = 0; i < schemas.length; i++) {
            try {
                XmlBeans.loadXsd(new XmlObject[]{schemas[i]});
                fail("Schema should have failed to compile:\n" + invalidSchemas[i]);
            } catch (XmlException success) {
            }
        }

        // Parse the valid schema files
        schemas = new SchemaDocument[validSchemas.length];
        for (int i = 0; i < validSchemas.length; i++)
            schemas[i] = SchemaDocument.Factory.parse(validSchemas[i]);

        // Compile the valid schemas. They should not fail
        for (int i = 0; i < schemas.length; i++) {
            try {
                XmlBeans.loadXsd(new XmlObject[]{schemas[i]});
            } catch (XmlException fail) {
                fail("Failed to compile schema:\n" + validSchemas[i]);
            }
        }
    }

    //TESTENV:

    private static File fwroot = getRootFile();
    private static File caseroot = new File(fwroot, "test/cases");

    //location of files under "cases folder"
    static String fileLocation = "/xbean/compile/scomp/";
    private static File outputroot = new File(fwroot, "build/test/output");

    private static void dumpErrors(List errors, PrintWriter out) {
        // Display the errors
        for (int i = 0; i < errors.size(); i++) {
            XmlError error = (XmlError) errors.get(i);
            if (error.getSeverity() == XmlError.SEVERITY_ERROR)
                out.println(error.toString());
        }
    }

    private static File getRootFile() throws IllegalStateException {
        try {
            return new File(System.getProperty("xbean.rootdir")).getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    private static File xbeanCase(String str) {
        return (new File(caseroot.getPath() + fileLocation, str));
    }

    private static File xbeanOutput(String str) {
        File result = (new File(outputroot, str));
        File parentdir = result.getParentFile();
        parentdir.mkdirs();
        return result;
    }

    private static void deltree(File dir)
        throws InterruptedException {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++)
                    deltree(new File(dir, list[i]));
            }
            if (!dir.delete()) {
                for (int i = 0; i < 5; i++) {
                    try {
                        System.out.println("Sleep 1s and try do delete it again: " + dir.getCanonicalPath());
                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                    }
                    Thread.currentThread().sleep(1000);
                    if (dir.delete())
                        return;
                }

                if (!dir.delete())
                    throw new IllegalStateException("Could not delete " + dir);
            }
        }
    }
}
