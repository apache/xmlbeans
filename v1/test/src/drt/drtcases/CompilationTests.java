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

package drtcases;

import junit.framework.TestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class CompilationTests extends TestCase
{
    public CompilationTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(CompilationTests.class); }

    public void __testJ2EE() throws Throwable
    {
        TestEnv.deltree(TestEnv.xbeanOutput("schema/j2ee"));
        // First, compile schema
        File srcdir = TestEnv.xbeanOutput("schema/j2ee/j2eeconfigxml/src");
        File classesdir = TestEnv.xbeanOutput("schema/j2ee/j2eeconfigxml/classes");
        File outputjar = TestEnv.xbeanOutput("schema/j2ee/j2eeconfigxml.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[] {
            TestEnv.xbeanCase("schema/j2ee/application-client_1_4.xsd"),
            TestEnv.xbeanCase("schema/j2ee/application_1_4.xsd"),
            TestEnv.xbeanCase("schema/j2ee/connector_1_5.xsd"),
            TestEnv.xbeanCase("schema/j2ee/ejb-jar_2_1.xsd"),
            TestEnv.xbeanCase("schema/j2ee/j2ee_1_4.xsd"),
            TestEnv.xbeanCase("schema/j2ee/jsp_2_0.xsd"),
            TestEnv.xbeanCase("schema/j2ee/web-app_2_4.xsd"),
            TestEnv.xbeanCase("schema/j2ee/XML.xsd"),
        });
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        Assert.assertTrue("Build failed", SchemaCompiler.compile(params));
        Assert.assertTrue("Cannout find " + outputjar, outputjar.exists());
    }


    /* test not needed because bootstrap build tests this better
    public void testNewBuild()
    {
        TestEnv.deltree(TestEnv.xbeanOutput("schema/s4s"));
        File inputfile1 = TestEnv.xbeanCase("schema/s4s/XML.xsd");
        File inputfile2 = TestEnv.xbeanCase("schema/s4s/XMLSchema.xsd");
        File outputjar = TestEnv.xbeanOutput("schema/s4s/s4stypes.jar");
        SchemaCompiler.compile(new File[] { inputfile1, inputfile2 }, new File[0], new File[0], new File[0], null, outputjar, "schema.s4s", null, null, null, true, false, false);
        Assert.assertTrue("Cannout find " + outputjar, outputjar.exists());
    }
    */

    public void __testSimple() throws Throwable
    {
        TestEnv.deltree(TestEnv.xbeanOutput("schema/simple"));
        // First, compile schema
        File inputfile1 = TestEnv.xbeanCase("schema/simple/person.xsd");
        File inputfile2 = TestEnv.xbeanCase("schema/simple/simplec.xsd");
        File srcdir = TestEnv.xbeanOutput("schema/simple/simpletypes/src");
        File classesdir = TestEnv.xbeanOutput("schema/simple/simpletypes/classes");
        File outputjar = TestEnv.xbeanOutput("schema/simple/simpletypes.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[] { inputfile1, inputfile2 });
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        Assert.assertTrue("Build failed", SchemaCompiler.compile(params));

        // Then, compile java classes
        File javasrc = TestEnv.xbeanCase("schema/simple/javasrc");
        File javaclasses = TestEnv.xbeanOutput("schema/simple/javaclasses");
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
        TestEnv.deltree(TestEnv.xbeanOutput("schema/include"));

        {
            // First, compile schema without download and verify failure
            File srcdir = TestEnv.xbeanOutput("schema/include/shouldfail/src");
            File classesdir = TestEnv.xbeanOutput("schema/include/shouldfail/classes");
            File outputjar = TestEnv.xbeanOutput("schema/include/shouldfail.jar");
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setXsdFiles(new File[] {
                TestEnv.xbeanCase("schema/j2ee/j2ee_1_4.xsd"),
            });
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            Assert.assertTrue("Build should have failed", !SchemaCompiler.compile(params));
            Assert.assertTrue("Should not have created " + outputjar, !outputjar.exists());
        }

        {
            // now turn on download and verify success
            File srcdir = TestEnv.xbeanOutput("schema/include/shouldsucceed/src");
            File classesdir = TestEnv.xbeanOutput("schema/include/shouldsucceed/classes");
            File outputjar = TestEnv.xbeanOutput("schema/include/shouldsucceed.jar");
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setDownload(true);
            params.setXsdFiles(new File[] {
                TestEnv.xbeanCase("schema/j2ee/j2ee_1_4.xsd"),
            });
            params.setSrcDir(srcdir);
            params.setClassesDir(classesdir);
            params.setOutputJar(outputjar);
            Assert.assertTrue("Build failed", SchemaCompiler.compile(params));
            Assert.assertTrue("Cannout find " + outputjar, outputjar.exists());
        }
    }

    public void testPricequote() throws Throwable
    {
        TestEnv.deltree(TestEnv.xbeanOutput("schema/pricequote"));
        // First, compile schema
        File srcdir = TestEnv.xbeanOutput("schema/pricequote/src");
        File classesdir = TestEnv.xbeanOutput("schema/pricequote/classes");
        File outputjar = TestEnv.xbeanOutput("schema/pricequote/pricequote.jar");
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[] { TestEnv.xbeanCase("schema/pricequote/PriceQuote.xsd") });
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        Assert.assertTrue("Build failed", SchemaCompiler.compile(params));
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

    public void testFinal() throws Throwable
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

}
