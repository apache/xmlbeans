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

package compile.scomp.detailed;

import junit.framework.TestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3.x2001.xmlSchema.TopLevelComplexType;
import org.apache.xmlbeans.SchemaBookmark;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import tools.util.TestRunUtil;


public class CompilationTests extends TestCase
{
    public CompilationTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(CompilationTests.class); }


    public void testJ2EE() throws Throwable
    {
        deltree(xbeanOutput("compile/scomp/j2ee"));
        // First, compile schema
        File srcdir = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml/src");
        File classesdir = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml/classes");
        File outputjar = xbeanOutput("compile/scomp/j2ee/j2eeconfigxml.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[] {
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
        if (!result)
        {
            // Display the errors
            for (int i = 0; i < errors.size(); i++)
            {
                XmlError error = (XmlError) errors.get(i);
                if (error.getSeverity() == XmlError.SEVERITY_ERROR)
                    System.out.println(error.toString());
            }
        }
        Assert.assertTrue("Build failed", result);
        Assert.assertTrue("Cannot find " + outputjar, outputjar.exists());
    }


    public void testSchemaBookmarks() throws Throwable
    {
        File srcSchema = xbeanCase("simple/person.xsd");
        // Parse
        SchemaDocument.Schema parsed = SchemaDocument.Factory.parse(srcSchema).getSchema();
        // Navigate to the type definition
        TopLevelComplexType[] cTypes = parsed.getComplexTypeArray();
        boolean found = false;
        int i;
        for (i = 0; i < cTypes.length; i++)
            if ("person".equals(cTypes[i].getName()))
            {
                found = true;
                break;
            }
        Assert.assertTrue("Could not find the \"person\" complex type", found);
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


    public void __testSimple() throws Throwable
    {
        deltree(xbeanOutput("compile/scomp/simple"));
        // First, compile schema

        // First, compile schema
        File inputfile1 = xbeanCase("simple/person.xsd");
        File inputfile2 = xbeanCase("simple/simplec.xsd");

         File srcdir = xbeanOutput("simple/simpletypes/src");


        File classesdir = xbeanOutput("compile/scomp/simple/simpletypes/classes");
        File outputjar = xbeanOutput("compile/scomp/simple/simpletypes.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[] { inputfile1, inputfile2 });
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        Assert.assertTrue("Build failed", SchemaCompiler.compile(params));

        // Then, compile java classes
        File javasrc = xbeanCase("simple/javasrc");
        File javaclasses = xbeanOutput("compile/scomp/simple/javaclasses");
        javaclasses.mkdirs();
        List testcp = new ArrayList();
        testcp.addAll(Arrays.asList(CodeGenUtil.systemClasspath()));
        testcp.add(outputjar);
        CodeGenUtil.externalCompile(Arrays.asList(new File[] { javasrc }), javaclasses, (File[])testcp.toArray(new File[testcp.size()]), true);

        // Then run the test
        testcp.add(javaclasses);
        TestRunUtil.run("SimplePersonTest", new File[] { outputjar, javaclasses });
    }

    public void __testDownload() throws Throwable
    {
        deltree(xbeanOutput("compile/scomp/include"));

        {
            // First, compile schema without download and verify failure
            File srcdir = xbeanOutput("compile/scomp/include/shouldfail/src");
            File classesdir = xbeanOutput("compile/scomp/include/shouldfail/classes");
            File outputjar = xbeanOutput("compile/scomp/include/shouldfail.jar");
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setXsdFiles(new File[] {
                 xbeanCase ("compile/scomp/j2ee/j2ee_1_4.xsd")
            });
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            Assert.assertTrue("Build should have failed", !SchemaCompiler.compile(params));
            Assert.assertTrue("Should not have created " + outputjar, !outputjar.exists());
        }

        {
            // now turn on download and verify success
            File srcdir = xbeanOutput("compile/scomp/include/shouldsucceed/src");
            File classesdir = xbeanOutput("compile/scomp/include/shouldsucceed/classes");
            File outputjar = xbeanOutput("compile/scomp/include/shouldsucceed.jar");
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setDownload(true);
            params.setXsdFiles(new File[] {
                xbeanCase("compile/scomp/j2ee/j2ee_1_4.xsd")
            });
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            Assert.assertTrue("Build failed", SchemaCompiler.compile(params));
            Assert.assertTrue("Cannout find " + outputjar, outputjar.exists());
        }
    }

    public void __testPricequote() throws Throwable
    {
        deltree(xbeanOutput("compile/scomp/pricequote"));
        // First, compile schema
        File srcdir = xbeanOutput("compile/scomp/pricequote/src");
        File classesdir = xbeanOutput("compile/scomp/pricequote/classes");
        File outputjar = xbeanOutput("compile/scomp/pricequote/pricequote.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[] {
            xbeanCase("compile/scomp/pricequote/PriceQuote.xsd") });
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        Assert.assertTrue("Build failed "+fwroot, SchemaCompiler.compile(params));
        Assert.assertTrue("Cannout find " + outputjar, outputjar.exists());
    }

    static String [] invalidSchemas = {
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

    static String [] validSchemas = {
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

    public void __testFinal() throws Throwable
    {
        SchemaDocument[] schemas = new SchemaDocument[invalidSchemas.length];

        // Parse the invalid schema files
        for (int i = 0 ; i < invalidSchemas.length ; i++)
            schemas[i] = SchemaDocument.Factory.parse(invalidSchemas[i]);

        // Now compile the invalid schemas, test that they fail
        for (int i = 0 ; i < schemas.length ; i++)
        {
            try {
                XmlBeans.loadXsd(new XmlObject[] {schemas[i]});
                fail("Schema should have failed to compile:\n" + invalidSchemas[i]);
            }
            catch (XmlException success) {}
        }

        // Parse the valid schema files
        schemas = new SchemaDocument[validSchemas.length];
        for (int i = 0 ; i < validSchemas.length ; i++)
            schemas[i] = SchemaDocument.Factory.parse(validSchemas[i]);

        // Compile the valid schemas. They should not fail
        for (int i = 0 ; i < schemas.length ; i++)
        {
            try {
                XmlBeans.loadXsd(new XmlObject[]{schemas[i]});
            }
            catch (XmlException fail) {
                fail("Failed to compile schema:\n" + validSchemas[i]);
            }
        }
    }

    //TESTENV:

     private static File fwroot = getRootFile();
    private static File caseroot = new File(fwroot, "test/cases");

    //location of files under "cases folder"
    static String  fileLocation="/xbean/compile/scomp/";
    private static File outputroot = new File(fwroot, "build/test/output");

    public static File getRootFile() throws IllegalStateException
    {
        try
        {
            return new File( System.getProperty( "xbean.rootdir" ) ).getCanonicalFile();
        }
        catch( IOException e )
        {
            throw new IllegalStateException(e.toString());
        }
    }

    public static File xbeanCase(String str)
    {
        return (new File(caseroot.getPath()+fileLocation, str));
    }

    public static File xbeanOutput(String str)
    {
        File result = (new File(outputroot, str));
        File parentdir = result.getParentFile();
        parentdir.mkdirs();
        return result;
    }

    public static void deltree(File dir)
    {
        if (dir.exists())
        {
            if (dir.isDirectory())
            {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++)
                    deltree(new File(dir, list[i]));
            }
            if (!dir.delete())
                throw new IllegalStateException("Could not delete " + dir);
        }
    }
}
