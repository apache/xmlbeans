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
package misc.detailed;

import misc.common.JiraTestBase;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xmlsoap.schemas.ws.x2004.x08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws.x2004.x08.addressing.ReferencePropertiesType;
import junit.framework.Assert;
import xmlbeans06.Area;
import xmlbeans06.StateProvince;

import javax.xml.namespace.QName;

import xmlbeans48.FeedInfoType;

/**
 *
 */
public class JiraRegression1_50Test extends JiraTestBase {

    public JiraRegression1_50Test(String name) {
        super(name);
    }

    public void setUp() {

        // directories for the SchemaCompiler.Parameter class members
        if (schemaCompSrcDir == null) {
            schemaCompSrcDir = new File(schemaCompOutputDirPath + P + "src");
            if (!schemaCompSrcDir.exists()) {
                schemaCompSrcDir.mkdirs();
            }
        }
        //&& (!schemaCompSrcDir.exists()))
        if (schemaCompClassesDir == null) {
            schemaCompClassesDir = new File(schemaCompOutputDirPath + P + "classes");
            if (!schemaCompClassesDir.exists()) {
                schemaCompClassesDir.mkdirs();
            }
        }

        if (schemaCompOutputDirFile == null) {
            schemaCompOutputDirFile = new File(schemaCompOutputDirPath + P);
            if (!schemaCompOutputDirFile.exists()) {
                schemaCompOutputDirFile.mkdirs();
            }
        }
    }

    /*
    * [XMLBEANS-2] Problem with XmlError.forObject(String,int,XmlObject)
    */
    public void test_jira_xmlbeans02() throws Exception {
        StringBuffer xmlstringbuf = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xmlstringbuf.append("<test>");
        xmlstringbuf.append("<testchild attr=\"abcd\"> Jira02 </testchild>");
        xmlstringbuf.append("</test>");

        XmlObject myxmlobj = null;
        List errors = new ArrayList();
        XmlOptions options = new XmlOptions().setErrorListener(errors);
        try {
            myxmlobj = XmlObject.Factory.parse(xmlstringbuf.toString(), options);
            XmlCursor cur = myxmlobj.newCursor();
            XmlError xmlerr = XmlError.forObject("This is my custom error message", XmlError.SEVERITY_ERROR, myxmlobj);

            // call an API on the cursor : verification of cursor not being disposed
            System.out.println("Cursor Text Value: " + cur.getTextValue());

        } catch (XmlException xme) {
            if (!xme.getErrors().isEmpty()) {
                for (Iterator itr = xme.getErrors().iterator(); itr.hasNext();) {
                    System.out.println("Parse Errors :" + itr.next());
                }
            }

        } catch (NullPointerException npe) {
            Assert.fail("test_jira_xmlbeans02() : Null Pointer Exception thrown !");
        }

        printOptionErrMsgs(errors);
    }

    /*
    * [XMLBEANS-4] xs:decimal size greater than 18 results in uncompilable java code
    *
    */
    public void test_jira_xmlbeans04() {
        List errors = new ArrayList();

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_04.xsd_")});
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);

        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans04() : Errors found when executing scomp");
        }
    }

    /*
    * [XMLBEANS-6] getter for enumeration array throws ArrayStoreException
    *
    */
    public void test_jira_xmlbeans06() throws Exception {
        // refer xsd : xmlbeans_06.xsd, xbeans06.jar from scomp
        try {
            Area area = Area.Factory.newInstance();
            area.addState(StateProvince.OR);
            area.addState(StateProvince.WA);
            StateProvince.Enum[] enumStates = area.getStateArray();
            for (int i = 0; i < enumStates.length; i++) {
                System.out.println("State Elem enums are :" + enumStates[i]);
            }
        } catch (ArrayStoreException ae) {
            ae.getMessage();
            ae.printStackTrace();
            Assert.fail("Array Store Exception thrown for repro of Bug Jira06. Not expected");
        }
    }

    /*
    * [XMLBEANS-8] NullPointerException @SchemaTypeImpl.setShortJavaName when using scomp
    *
    */
    public void test_jira_xmlbeans08() {
        // invoking scomp via 'testbuild ant directive resilts in NPE for these schemas (xmlbeans_08a.xsd, xmlbeans_08.xsd
        /*Assert.fail("scomp fails with NPE. The Error is : " +
        "scomp:\n" +
        "     [echo] Compiling D:\\SVNNEW\\xmlbeans\\trunk/build/test/schemas/xbean/misc using compiler from: org.apache.xmlbeans.impl.tool.SchemaCompiler\n" +
        "     [java] xmlbeans_08a.xsd:8: error: rcase-RecurseLax.2: Invalid Restriction.  The following particles of the derived <choice> cannot be mapped to the base <choice>'s particles: <element name=\"metadata@http://www.w3.org/2001/10/synthesis\">\n" +
        "     [java] Exception in thread \"main\" java.lang.NullPointerException\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.SchemaTypeImpl.setShortJavaName(SchemaTypeImpl.java:535)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.StscJavaizer.assignJavaAnonymousTypeNames(StscJavaizer.java:357)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.StscJavaizer.javaizeType(StscJavaizer.java:267)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.StscJavaizer.javaizeType(StscJavaizer.java:223)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.StscJavaizer.javaizeAllTypes(StscJavaizer.java:63)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler.compileImpl(SchemaTypeSystemCompiler.java:310)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler.compile(SchemaTypeSystemCompiler.java:181)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.tool.SchemaCompiler.loadTypeSystem(SchemaCompiler.java:947)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.tool.SchemaCompiler.compile(SchemaCompiler.java:1067)\n" +
        "     [java] \tat org.apache.xmlbeans.impl.tool.SchemaCompiler.main(SchemaCompiler.java:367)\n" +
        "     [echo] jar.file: D:\\SVNNEW\\xmlbeans\\trunk/build/test/lib/schemajars/misc.jar");
        */

        List errors = new ArrayList();

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_08a.xsd_")});
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);

        try {
            SchemaCompiler.compile(params);
        } catch (NullPointerException npe) {
            if (!errors.isEmpty()) {
                for (Iterator itr = errors.iterator(); itr.hasNext();) {
                    System.out.println("scomp errors: " + itr.next());
                }
            }

            Assert.fail("test_jira_xmlbeans08() :Null Pointer Exception thrown with above errors!");
        }

    }

    /*
    * [XMLBEANS-9] Null Pointer Exception when running validate from cmd line
    *
    */
    public void test_jira_xmlbeans09() throws Exception {
        // Exec validate script from cmd line - Refer xmlbeans_09.xsd, xmlbeans_09.xml

        StringBuffer sb = new StringBuffer(" ");
        sb.append(System.getProperty("xbean.rootdir") + P + "bin" + P + "validate.cmd ");
        sb.append(scompTestFilesRoot + "xmlbeans_09.xsd_" + " " + scompTestFilesRoot + "xmlbeans_09.xml");
        Process validator_proc = null;
        try {
            validator_proc = Runtime.getRuntime().exec(sb.toString());
        } catch (NullPointerException npe) {
            Assert.fail("test_jira_xmlbeans09() : Null Pointer Exception when running validate for schema");
        }

        System.out.println("cmd:" + sb);
        BufferedInputStream inbuf = new BufferedInputStream(validator_proc.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inbuf));
        String eachline = reader.readLine();
        try {

            while (reader.readLine() != null) {
                System.out.println("output: " + eachline);
            }
        } catch (IOException ioe) {
            ioe.getMessage();
            ioe.printStackTrace();
        }

    }

    /*
    * [XMLBEANS-11]: Calling getUnionMemberTypes() on SchemaType for non-union types results in NullPointerException
    * status : fixed
    */
    public void test_jira_xmlbeans11() throws Exception {

        StringBuffer xsdAsString = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        xsdAsString.append(" <!-- W3C Schema generated by XML Spy v4.3 U (http://www.xmlspy.com)\n");
        xsdAsString.append("  --> \n");
        xsdAsString.append(" <xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\n");
        xsdAsString.append("   <xs:simpleType name=\"restrictsString\">\n");
        xsdAsString.append("      <xs:restriction base=\"xs:string\">\n");
        xsdAsString.append("          <xs:length value=\"10\" /> \n");
        xsdAsString.append("      </xs:restriction>\n");
        xsdAsString.append("    </xs:simpleType>\n");
        xsdAsString.append("  </xs:schema>");

        // load schema file as SchemaDocument XmlObject
        SchemaDocument sd = SchemaDocument.Factory.parse(xsdAsString.toString());

        // compile loaded XmlObject
        SchemaTypeSystem sts = XmlBeans.compileXsd((XmlObject[]) Collections.singletonList(sd).toArray(new XmlObject[]{}),
                XmlBeans.getContextTypeLoader(),
                new XmlOptions());
        sts.resolve();

        SchemaType[] st = sts.globalTypes();

        System.out.println("NUMBER OF GLOBAL TYPES: " + st.length);

        try {
            for (int i = 0; i < st.length; i++)
                    // check if it is union type
            {
                System.out.println("IS UNION TYPE: " + (st[i].getUnionMemberTypes() != null));
            }
        } catch (NullPointerException npe) {
            Assert.fail("test_jira_xmlbeans11(): Null Pointer Exception thrown !");
        }

    }

    /*
    * [XMLBEANS-14]: newDomNode() throws NullPointerException
    *
    */
    public void test_jira_xmlbeans14() throws Exception {
        /*
        <?xml version="1.0" encoding="UTF-8"?>
        <xml-fragment><some:SomeName1 xmlns:some="Some uri">SomeValue1</some:SomeName1><some:SomeName xmlns:some="Some uri">Some
        Value</some:SomeName></xml-fragment>Exception in thread "main" java.lang.NullPointerException
        at org.apache.xmlbeans.impl.store.Saver$DomSaver.emitContainer(Saver.java:4514)
        at org.apache.xmlbeans.impl.store.Saver.processContainer(Saver.java:775)
        at org.apache.xmlbeans.impl.store.Saver.process(Saver.java:518)
        at org.apache.xmlbeans.impl.store.Saver$DomSaver.exportDom(Saver.java:4461)
        at org.apache.xmlbeans.impl.store.Cursor.newDomNode(Cursor.java:2954)
        at org.apache.xmlbeans.impl.values.XmlObjectBase.newDomNode(XmlObjectBase.java:154)
        at org.apache.xmlbeans.impl.values.XmlObjectBase.newDomNode(XmlObjectBase.java:151)
        at MyClass.getNode(MyClass.java:27)
        at MyClass.main(MyClass.java:44)
        */

        // the wsdl schema http://schemas.xmlsoap.org/ws/2004/08/addressing is compiled into xmlbeans_14.jar
        EndpointReferenceType lEndPointXmlBeanObject = EndpointReferenceType.Factory.newInstance();
        ReferencePropertiesType m_TargetObject = lEndPointXmlBeanObject.addNewReferenceProperties();

        // add element
        XmlCursor xCursor = m_TargetObject.newCursor();
        xCursor.toFirstContentToken();
        xCursor.insertElementWithText(new QName("Some uri", "SomeName"), "SomeValue");
        xCursor.insertElementWithText(new QName("Some uri", "SomeName1"), "SomeValue1");
        xCursor.dispose();

        // debug
        m_TargetObject.save(System.out);

        // throws npe in v1
        try {
            m_TargetObject.newDomNode();
        } catch (NullPointerException npe) {
            Assert.fail("test_jira_xmlbeans14() : Null Pointer Exception when create Dom Node");
        }

    }


    /*
    * [XMLBEANS-16]: newDomNode creates a DOM with empty strings for namespace URIs for unprefixed
    * attributes (rather than null)
    * status : fails with crimson and not with Xerces
    */
    public void test_jira_xmlbeans16() throws Exception {
        StringBuffer sb = new StringBuffer(100);
        sb.append("<?xml version='1.0'?>\n");
        sb.append("<test noprefix='nonamespace' \n");
        sb.append("      ns:prefix='namespace' \n");
        sb.append("      xmlns:ns='http://xml.apache.org/xmlbeans'>value</test>");

        // Parse it using XMLBeans
        XmlObject xdoc = XmlObject.Factory.parse(sb.toString());

        // Convert to a DOM Element
        Element firstChild = (Element) xdoc.newDomNode().getFirstChild();

        // We expect to find a null namespace for the first attribute and 'ns' for the second
        NamedNodeMap attributes = firstChild.getAttributes();
        System.out.println("Prefix for attr noprefix is:" + attributes.getNamedItem("noprefix").getPrefix() + ":");
        assertNull("Expected null namespace for attribute noprefix", attributes.getNamedItem("noprefix").getPrefix());
        assertEquals("Wrong namespace for attribute prefix", "ns", attributes.getNamedItem("ns:prefix").getPrefix());

        // We should be able to lookup 'prefix' by specifying the appropriate URI
        String prefix = firstChild.getAttributeNS("http://xml.apache.org/xmlbeans", "prefix");
        assertEquals("Wrong value for prefixed attribute", "namespace", prefix);

        // And 'noprefix' by specifying a null namespace URI
        String noprefix = firstChild.getAttributeNS(null, "noprefix");
        assertEquals("Wrong value for unprefixed attribute", "nonamespace", noprefix); // This assertion fails under Crimson

    }

    /*
    * [XMLBEANS-33]:  insertions occur in improper order when subclassing schema types
    *
    */
    public void test_jira_xmlbeans33() throws Exception {

        xbeansJira33B.SubjectType subject =
                xbeansJira33B.SubjectType.Factory.newInstance();
        subject.addNewIDPProvidedNameIdentifier();
        subject.addNewSubjectConfirmation().addConfirmationMethod("foo");
        subject.addNewNameIdentifier();
        XmlOptions options = new XmlOptions();
        ArrayList list = new ArrayList();
        options.setErrorListener(list);

        boolean bResult = subject.validate(options);
        System.out.println(bResult ? "valid" : "invalid");

        // print out errors
        for (int i = 0; i < list.size(); i++) {
            System.out.println("Validation Error : " + list.get(i));
        }
        Assert.assertTrue("Validation Failed, should pass", bResult);

    }

    /*
    * [XMLBEANS-34]:  error compiling classes when using a schema with a redefined subelement
    *
    */
    public void test_jira_xmlbeans34() throws Exception {
        List errors = new ArrayList();

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_34b.xsd_")});
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);
        params.setDownload(true);
        params.setNoPvr(true);

        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans34() : Errors found when executing scomp");
        }

    }

    /*
    * [XMLBEANS-35]: OutOfMemoryError with this schema : Circular reference to Model Groups
    *
    */
    public void test_jira_xmlbeans35() {
        XmlOptions options = new XmlOptions();
        List errors = new ArrayList();
        options.setErrorListener(errors);

        try {
            XmlObject xobjs [] = new XmlObject[]{compileXsdFile(scompTestFilesRoot + "xmlbeans_35.xsd_")};

            SchemaTypeSystem sts = XmlBeans.compileXsd(xobjs, null, options);

            sts.saveToDirectory(schemaCompOutputDirFile);
        } catch (XmlException xme) {
            if (!errors.isEmpty()) {
                for (Iterator itr = errors.iterator(); itr.hasNext();) {
                    System.out.println("scomp errors: ");
                }
            }

        }

        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans34() : Errors found when executing scomp");
        }

        Assert.fail("Fails due to Out of Memory");
    }



    /**
     * BUGBUG: [XMLBEANS-38]
     * [XMLBEANS-38]   Does not support xs:key (keyRef NoIllegalEntries)
     */
    public void test_jira_xmlbeans38() throws Exception {
        String keyrefXSD = "<?xml version=\"1.0\"?>" +
                "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                "<xsd:element name=\"supermarket\">" +
                "<xsd:complexType>" +
                "<xsd:sequence> " +
                "<xsd:element name=\"aisle\" maxOccurs=\"unbounded\"> \n" +
                "<xsd:complexType> " +
                "<xsd:sequence>" +
                "<xsd:element name=\"item\" maxOccurs=\"unbounded\"> \n" +
                "<xsd:complexType> " +
                "<xsd:simpleContent>" +
                "<xsd:extension base=\"xsd:string\"> \n" +
                "<xsd:attribute name=\"code\" type=\"xsd:positiveInteger\"/> \n" +
                "<xsd:attribute name=\"quantity\" type=\"xsd:positiveInteger\"/> \n" +
                "<xsd:attribute name=\"price\" type=\"xsd:decimal\"/> \n" +
                "</xsd:extension> \n" +
                "</xsd:simpleContent> \n" +
                "</xsd:complexType> \n" +
                "</xsd:element> \n" +
                "</xsd:sequence> \n" + //"<!-- Attribute Of Aisle --> \n" +
                "<xsd:attribute name=\"name\" type=\"xsd:string\"/> \n" +
                "<xsd:attribute name=\"number\" type=\"xsd:positiveInteger\"/> \n" + //"<!-- Of Aisle --> \n" +
                "</xsd:complexType> \n" +
                "<xsd:keyref name=\"NoIllegalEntries\" refer=\"itemKey\"> \n" +
                "<xsd:selector xpath=\"item\"/> \n" +
                "<xsd:field xpath=\"@code\"/> \n" +
                "</xsd:keyref> \n" +
                "</xsd:element> \n" +
                "<xsd:element name=\"items\"> \n" +
                "<xsd:complexType> \n" +
                "<xsd:sequence> \n" +
                "<xsd:element name=\"item\" maxOccurs=\"unbounded\"> \n" +
                "<xsd:complexType> \n" +
                "<xsd:simpleContent> \n" +
                "<xsd:extension base=\"xsd:string\"> \n" +
                "<xsd:attribute name=\"code\" type=\"xsd:positiveInteger\"/> \n" +
                "</xsd:extension> \n" +
                "</xsd:simpleContent> \n" +
                "</xsd:complexType> \n" +
                "</xsd:element> \n" +
                "</xsd:sequence> \n" +
                "</xsd:complexType> \n" +
                "</xsd:element> \n" +
                "</xsd:sequence> \n" +
                "<xsd:attribute name=\"name\" type=\"xsd:string\"/> \n" +
                "</xsd:complexType> \n" +
                "<xsd:key name=\"itemKey\"> \n" +
                "<xsd:selector xpath=\"items/item\"/> \n" +
                "<xsd:field xpath=\"@code\"/> \n" +
                "</xsd:key> \n" +
                "</xsd:element> \n" +
                "</xsd:schema>";


        String keyRefInstance = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                "<supermarket xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"C:\\tmp\\Supermarket.xsd\" name=\"String\"> \n" +
                "<aisle name=\"String\" number=\"2\"> \n" +
                "<item code=\"1234\" quantity=\"2\" price=\"3.1415926535897932384626433832795\">String</item> \n" +
                "</aisle> \n" +
                "<items> \n" +
                "<item code=\"1234\">Java</item> \n" +
                "</items> \n" +
                "</supermarket>";

        validateInstance(new String[]{keyrefXSD}, new String[]{keyRefInstance}, null);
    }


    /**
     * Loads the class at runtime and inspects for appropriate methods
     * Statically using methods (class.getGeneration()) would stop build
     * if the bug resurfaced.
     * <p/>
     * [XMLBEANS-45]   <xsd:redefine> tag is not supported
     */
    public void test_jira_XmlBeans45() throws Exception {
        //this class is built during the build.schemas target
        Class cls = Class.forName("xmlbeans45.PersonName");
        //check for methods in class
        //getGeneration()
        if (cls.getMethod("getGeneration", null) == null)
            throw new Exception("getGeneration() was not found in class");
        //getTitle()
        if (cls.getMethod("getTitle", null) == null)
            throw new Exception("getTitle() was not found in class");
        //getForenameArray()
        if (cls.getMethod("getForenameArray", null) == null)
            throw new Exception("getForenameArray() was not found in class");

    }

    /**
     * Could not Repro this
     * [XMLBEANS-46] Regex validation fails in multi-threaded, multi-processor environment
     */
    public void test_jira_XmlBeans46() throws Exception {
        RegexThread[] threads = new RegexThread[45];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new RegexThread();
            System.out.println("Thread[" + i + "]-starting ");
            threads[i].start();
        }

        Thread.sleep(6000);
        System.out.println("Done with RegEx Threading Test...");

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].getException() != null)
                sb.append(threads[i].getException().getMessage() + "\n");
        }

        if (sb.length() > 0)
            throw new Exception("Threaded Regex Validation Failed\n" + sb.toString());
    }


    /**
     * Incorrect XML
     * [XMLBEANS-48]   Bug with Root.fetch ( Splay parent, QName name, QNameSet set, int n )
     */
    public void test_jira_XmlBeans48() throws Exception {
        String incorrectXml = "<sch:Feed xmlns:sch=\"http://xmlbeans_48\">" +
                "<sch:Feed>" +
                "<sch:Location>http://xmlbeans.apache.org</sch:Location>" +
                "<sch:TimeEntered>2004-08-11T15:50:23.064-04:00</sch:TimeEntered>" +
                "</sch:Feed>" +
                "</sch:Feed>";

        xmlbeans48.FeedDocument feedDoc = (xmlbeans48.FeedDocument) XmlObject.Factory.parse(incorrectXml);
        FeedInfoType feedInfoType = feedDoc.getFeed();
        String location = feedInfoType.getLocation();
        System.out.println("Location: " + location);
        if (location != null)
            throw new Exception("Location value should not have been populated");

        String correctXml = "<sch:Feed xmlns:sch=\"http://xmlbeans_48\">" +
                "<sch:Location>http://xmlbeans.apache.org</sch:Location>" +
                "<sch:TimeEntered>2004-08-11T15:50:23.064-04:00</sch:TimeEntered>" +
                "</sch:Feed>";

        feedDoc = (xmlbeans48.FeedDocument) XmlObject.Factory.parse(correctXml);
        feedInfoType = feedDoc.getFeed();
        location = feedInfoType.getLocation();
        System.out.println("Location: " + location);
        if (location == null)
            throw new Exception("Location value should have been populated");
    }

    /*
    * [XMLBEANS-49]: Schema compiler won't compile portlet.xsd from jsr168/pluto
    *
    */
    public void test_jira_xmlbeans49() {
        List errors = new ArrayList();
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_49.xsd_")});
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);

        // needs network downloads enabled
        params.setDownload(true);

        try {
            SchemaCompiler.compile(params);
        } catch (Exception ex) {
            if (!errors.isEmpty()) {
                for (Iterator itr = errors.iterator(); itr.hasNext();) {
                    System.out.println("scomp errors: ");
                }
            }

            Assert.fail("test_jira_xmlbeans49() :Exception thrown with above errors!");
        }

        // view errors
        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans49() : Errors found when executing scomp");
        }
    }



}
