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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument;
import org.apache.beehive.netui.tools.testrecorder.x2004.session.RecorderSessionDocument;
import org.oasisOpen.docs.wsdm.x2004.x04.muws05.schema.StateInformation;
import org.oasisOpen.docs.wsdm.x2004.x04.muws05.schema.ResourceStateDocument;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import tools.util.JarUtil;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import testDateAttribute.TestElementWithDateAttributeDocument;
import testDateAttribute.TestDatewTZone;
import xmlbeans48.FeedInfoType;


public class JiraRegressionsTest extends TestCase
{
    public static final int THREAD_COUNT = 150;
    public static final int ITERATION_COUNT = 2;
    private ArrayList errorList;
    private XmlOptions xmOpts;

    public JiraRegressionsTest(String name)
    {
        super(name);
        errorList = new ArrayList();
        xmOpts = new XmlOptions();
        xmOpts.setErrorListener(errorList);
    }

    public static Test suite()
    {
        return new TestSuite(JiraRegressionsTest.class);
    }

    ///**
    // * [XMLBEANS-##]  <BUG TITLE>
    // */
    //public void test_jira_XmlBeans45() throws Exception
    //{
    //
    //}


    /**
     * Repro case for jira issue
     * [XMLBEANS-38]   Does not support xs:key (keyRef NoIllegalEntries)
     */
    public void test_jira_xmlbeans38() throws Exception
    {
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

    private void validateInstance(String[] schemas, String[] instances, QName docType) throws Exception
    {
        SchemaTypeLoader stl = makeSchemaTypeLoader(schemas);
        XmlOptions options = new XmlOptions();

        if (docType != null) {
            SchemaType docSchema = stl.findDocumentType(docType);
            Assert.assertTrue(docSchema != null);
            options.put(XmlOptions.DOCUMENT_TYPE, docSchema);
        }

        for (int i = 0; i < instances.length; i++) {
            XmlObject x =
                    stl.parse((String) instances[i], null, options);

            //if (!startOnDocument) {
            //    XmlCursor c = x.newCursor();
            //    c.toFirstChild();
            //    x = c.getObject();
            //    c.dispose();
            //}

            List xel = new ArrayList();

            options.put(XmlOptions.ERROR_LISTENER, xel);

            boolean isValid = x.validate(options);

            if (!isValid) {
                StringBuffer errorTxt = new StringBuffer("Invalid doc, expected a valid doc: ");
                errorTxt.append("Instance(" + i + "): ");
                errorTxt.append(x.xmlText());
                errorTxt.append("Errors: ");
                for (int j = 0; j < xel.size(); j++)
                    errorTxt.append(xel.get(j) + "\n");
                System.err.println(errorTxt.toString());
                throw new Exception("Instance not valid\n" + errorTxt.toString());
            }
        }
    }

    private SchemaTypeLoader makeSchemaTypeLoader(String[] schemas)
            throws Exception
    {
        XmlObject[] schemaDocs = new XmlObject[schemas.length];

        for (int i = 0; i < schemas.length; i++) {
            schemaDocs[i] =
                    XmlObject.Factory.parse(schemas[i]);
        }

        return XmlBeans.loadXsd(schemaDocs);
    }

    /**
     * Loads the class at runtime and inspects for appropriate methods
     * Statically using methods (class.getGeneration()) would stop build
     * if the bug resurfaced.
     * <p/>
     * [XMLBEANS-45]   <xsd:redefine> tag is not supported
     */
    public void test_jira_XmlBeans45() throws Exception
    {
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
    public void test_jira_XmlBeans46() throws Exception
    {
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
    public void test_jira_XmlBeans48() throws Exception
    {
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
        
    /**
     * [XMLBEANS-52]   Validator loops when schema has certain conditions
     */
    public void test_jira_XmlBeans52() throws Exception{
     //reusing code from method test_jira_XmlBeans48()
     String correctXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
             "<!--Sample XML file generated by XMLSPY v5 rel. 4 U (http://www.xmlspy.com)--/> \n" +
             "<aList xmlns=\"http://pfa.dk/dummy/errorInXmlBeansValidation.xsd\" " +
             "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
             "xsi:schemaLocation=\"http://pfa.dk/dummy/errorInXmlBeansValidation.xsd \n" +
             "C:\\pfa\\techr3\\TransformationWorkbench\\schema\\errorInXmlBeansValidation.xsd\"> \n" +
             "<myListEntry> \n" +
             "<HelloWorld>Hello World</HelloWorld> \n" +
             "</myListEntry> \n" +
             "</aList> ";


     }

    /**
     * [XMLBEANS-56] samples issue with easypo schema and config file
     */
    public void test_jira_XmlBeans56() throws Exception
    {
        String xsdConfig = "<xb:config " +
                " xmlns:xb=\"http://xml.apache.org/xmlbeans/2004/02/xbean/config\"\n" +
                "    xmlns:ep=\"http://openuri.org/easypo\">\n" +
                "    <xb:namespace uri=\"http://openuri.org/easypo\">\n" +
                "        <xb:package>com.easypo</xb:package>\n" +
                "    </xb:namespace>\n" +
                "    <xb:namespace uri=\"##any\">\n" +
                "        <xb:prefix>Xml</xb:prefix>\n" +
                "        <xb:suffix>Bean</xb:suffix>\n" +
                "    </xb:namespace>\n" +
                "    <xb:extension for=\"com.easypo.XmlCustomerBean\">\n" +
                "        <xb:interface name=\"myPackage.Foo\">\n" +
                "            <xb:staticHandler>myPackage.FooHandler</xb:staticHandler>\n" +
                "        </xb:interface>\n" +
                "    </xb:extension>\n" +
                "    <xb:qname name=\"ep:purchase-order\" javaname=\"purchaseOrderXXX\"/>\n" +
                "</xb:config> ";
        ConfigDocument config =
                ConfigDocument.Factory.parse(xsdConfig);
        xmOpts.setErrorListener(errorList);
        if (config.validate(xmOpts)) {
            System.out.println("Config Validated");
            return;
        } else {
            System.err.println("Config File did not validate");
            for (Iterator iterator = errorList.iterator(); iterator.hasNext();) {
                System.out.println("Error: " + iterator.next());
            }
            throw new Exception("Config File did not validate");
        }

    }

    /**
     * [XMLBEANS-57]   scomp failure for XSD namespace "DAV:"
     */
    public void test_jira_XmlBeans57() throws Exception
    {
        String P = File.separator;
        String outputDir = System.getProperty("xbean.rootdir") + P + "build" +
                P + "test" + P + "output" + P + "dav";

        File srcDir = new File(outputDir + P + "src");
        srcDir.mkdirs();
        File classDir = new File(outputDir + P + "classes");
        classDir.mkdirs();

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{JarUtil.getResourceFromJarasFile("xbean/misc/xmlbeans_57.xml")});
        params.setErrorListener(errorList);
        params.setSrcDir(srcDir);
        params.setClassesDir(classDir);
        SchemaCompiler.compile(params);
        Collection errs = params.getErrorListener();
        boolean outTextPresent = true;

        if (errs.size() != 0) {
            for (Iterator iterator = errs.iterator(); iterator.hasNext();) {
                Object o = iterator.next();
                String out = o.toString();
                System.out.println("Dav: " + out);
                if (out.startsWith("Compiled types to"))
                    outTextPresent = false;
            }
        }

        //cleanup gen'd dirs
        srcDir.deleteOnExit();
        classDir.deleteOnExit();

        if (outTextPresent)
            System.out.println("No errors when running schemacompiler with DAV namespace");
        else
            throw new Exception("There were errors while compiling XSD with DAV " +
                    "namespace. See sys.out for more info");
    }

    /**
     * [XMLBEANS-62]   Avoid class cast exception when compiling older schema namespace
     */
    public void test_jira_XmlBeans62() throws Exception
    {
        String P = File.separator;
        String outputDir = System.getProperty("xbean.rootdir") + P + "build" +
                P + "test" + P + "output" + P + "x1999";

        File srcDir = new File(outputDir + P + "src");
        srcDir.mkdirs();
        File classDir = new File(outputDir + P + "classes");
        classDir.mkdirs();

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setWsdlFiles(new File[]{JarUtil.getResourceFromJarasFile("xbean/misc/xmlbeans_62.xml")});
        params.setErrorListener(errorList);
        params.setSrcDir(srcDir);
        params.setClassesDir(classDir);
        SchemaCompiler.compile(params);
        Collection errs = params.getErrorListener();
        boolean warningPresent = false;
        for (Iterator iterator = errs.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            String out = o.toString();
            if (out.endsWith("did not have any schema documents in namespace 'http://www.w3.org/2001/XMLSchema'")) ;
            warningPresent = true;
        }

        //cleanup gen'd dirs
        srcDir.deleteOnExit();
        classDir.deleteOnExit();

        //validate error present
        if (!warningPresent)
            throw new Exception("Warning for 1999 schema was not found when compiling srcs");
        else
            System.out.println("Warning Present, test Passed");
    }

    /**
     * [XMLBEANS-64] ArrayIndexOutOfBoundsException during validation
     */
    public void test_jira_XmlBeans64() throws Exception
    {
        // load the document
        File inst = JarUtil.getResourceFromJarasFile("xbean/misc/xmlbeans_64.xml");
        XmlObject doc = RecorderSessionDocument.Factory.parse(inst);
        // validate
        XmlOptions validateOptions = new XmlOptions();
        validateOptions.setLoadLineNumbers();
        ArrayList errorList = new ArrayList();
        validateOptions.setErrorListener(errorList);
        boolean isValid = doc.validate(validateOptions);

        if (!isValid)
            throw new Exception("Errors: " + errorList);
    }

    /**
     * [XMLBEANS-66]   NullPointerException when restricting a union with one of the union members
     */
    public void test_jira_XmlBeans66() throws Exception
    {
        String reproXsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                "<xsd:schema targetNamespace=\"http://www.w3.org/2003/12/XQueryX\" \n" +
                "      xmlns=\"http://www.w3.org/2003/12/XQueryX\" \n" +
                "      xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
                "      elementFormDefault=\"qualified\" \n" +
                "      attributeFormDefault=\"qualified\"> \n" +
                "  <!-- Kludge for anySimpleType --> \n" +
                "  <xsd:simpleType name=\"constantValueType\"> \n" +
                "    <xsd:union memberTypes=\"xsd:integer xsd:decimal xsd:string xsd:double\"/> \n" +
                "  </xsd:simpleType> \n" +
                "  <!-- constant expressions. We have 4 different subclasses for this --> \n" +
                "  <xsd:complexType name=\"constantExpr\"> \n" +
                "        <xsd:sequence> \n" +
                "          <xsd:element name=\"value\" type=\"constantValueType\"/> \n" +
                "        </xsd:sequence> \n" +
                "  </xsd:complexType> \n" +
                "  <xsd:complexType name=\"integerConstantExpr\"> \n" +
                "    <xsd:complexContent> \n" +
                "      <xsd:restriction base=\"constantExpr\"> \n" +
                "        <xsd:sequence> \n" +
                "          <xsd:element name=\"value\" type=\"xsd:integer\"/> \n" +
                "        </xsd:sequence> \n" +
                "      </xsd:restriction> \n" +
                "    </xsd:complexContent> \n" +
                "  </xsd:complexType>" +
                "<!-- added for element validation -->" +
                "<xsd:element name=\"Kludge\" type=\"integerConstantExpr\" />\n" +
                "</xsd:schema> ";

        SchemaTypeLoader stl = makeSchemaTypeLoader(new String[]{reproXsd});
        QName reproQName = new QName("http://www.w3.org/2003/12/XQueryX", "Kludge");
        SchemaGlobalElement elVal = stl.findElement(reproQName);
        Assert.assertTrue("Element is null or not found", (elVal != null));

        String reproInst = "<Kludge xmlns=\"http://www.w3.org/2003/12/XQueryX\"><value>12</value></Kludge>";
        validateInstance(new String[]{reproXsd}, new String[]{reproInst}, null);
    }

    /**
     * [XMLBEANS-68] GDateBuilder outputs empty string when used without time or timezone
     */
    public void test_jira_XmlBeans68() throws Exception
    {
        Calendar cal = Calendar.getInstance();
        GDateBuilder gdateBuilder = new GDateBuilder(cal);
        gdateBuilder.clearTime();
        gdateBuilder.clearTimeZone();
        GDate gdate = gdateBuilder.toGDate();
        TestDatewTZone xdate = TestDatewTZone.Factory.newInstance();
        xdate.setGDateValue(gdate);
        TestElementWithDateAttributeDocument doc =
                TestElementWithDateAttributeDocument.Factory.newInstance();
        TestElementWithDateAttributeDocument.TestElementWithDateAttribute root =
                doc.addNewTestElementWithDateAttribute();

        root.xsetSomeDate(xdate);
        System.out.println("Doc: " + doc);
        System.out.println("Date: " + xdate.getStringValue());

        if (xdate.getStringValue().compareTo("") == 0 ||
                xdate.getStringValue().length() <= 1)
            throw new Exception("Date without TimeZone should not be empty");
        if (root.getSomeDate().getTimeInMillis() != gdate.getCalendar().getTimeInMillis())
            throw new Exception("Set Dates were not equal");
    }

    /**
     * This issue needed an elementFormDefault=qualified added to the schema
     * [XMLBEANS-71] when trying to retrieve data from a XMLBean with Input from a XML Document, we cannot get any data from the Bean.
     */
    public void test_jira_XmlBeans71() throws Exception
    {
        //schema src lives in cases/xbean/xmlobject/xmlbeans_71.xsd
        abc.BazResponseDocument doc = abc.BazResponseDocument.Factory.parse(JarUtil.getResourceFromJarasFile("xbean/misc/xmlbeans_71.xml"), xmOpts);
        xmOpts.setErrorListener(errorList);
        abc.BazResponseDocument.BazResponse baz = doc.getBazResponse();

        if (!doc.validate(xmOpts))
            System.out.println("DOC-ERRORS: " + errorList + "\n" + doc.xmlText());
        else
            System.out.println("DOC-XML:\n" + doc.xmlText());

        errorList.removeAll(errorList);
        xmOpts.setErrorListener(errorList);

        if (!baz.validate(xmOpts))
            System.out.println("BAZ-ERRORS: " + errorList + "\n" + baz.xmlText());
        //throw new Exception("Response Document did not validate\n"+errorList);
        else
            System.out.println("BAZ-XML:\n" + baz.xmlText());

        if (baz.getStatus().compareTo("SUCCESS") != 0)
            throw new Exception("Status was not loaded properly");
        else
            System.out.println("Sucess was recieved correctly");
    }


    /**
     * [XMLBEANS-72]   Document properties are lost
     */
    public void test_jira_XmlBeans72() throws Exception
    {
        String docTypeName = "struts-config";
        String docTypePublicID = "-//Apache Software Foundation//DTD Struts Configuration 1.1//EN";
        String docTypeSystemID = "http://jakarta.apache.org/struts/dtds/struts-config_1_1.dtd";
        String fileName = "xmlbeans72.xml";

        //create instance and set doc properties
        PurchaseOrderDocument po = PurchaseOrderDocument.Factory.newInstance();
        org.apache.xmlbeans.XmlDocumentProperties docProps = po.documentProperties();
        docProps.setDoctypeName(docTypeName);
        docProps.setDoctypePublicId(docTypePublicID);
        docProps.setDoctypeSystemId(docTypeSystemID);
        po.addNewPurchaseOrder();
        po.save(new File(fileName));

        //parse saved out file and verify values set above are present
        PurchaseOrderDocument po2 = PurchaseOrderDocument.Factory.parse(new File(fileName));
        //XmlObject po2 = XmlObject.Factory.parse(new File(fileName));

        org.apache.xmlbeans.XmlDocumentProperties doc2Props = po2.documentProperties();

        //verify information using DOM
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(fileName));

        DocumentType docType = document.getDoctype();

        //System.out.println("Name: "+ doc2Props.getDoctypeName() +" = " + docType.getName());
        //System.out.println("System: "+ doc2Props.getDoctypeSystemId() + " = " + docType.getSystemId());
        //System.out.println("Public: "+ doc2Props.getDoctypePublicId()+ " = " + docType.getPublicId());

        StringBuffer compareText = new StringBuffer();
        //check values - compare to expected and DOM
        if (doc2Props != null) {
            if (doc2Props.getDoctypeName() == null ||
                    doc2Props.getDoctypeName().compareTo(docTypeName) != 0 ||
                    doc2Props.getDoctypeName().compareTo(docType.getName()) != 0)
                compareText.append("docTypeName was not as " +
                        "expected in the document properties " +
                        doc2Props.getDoctypeName()+"\n");

            if (doc2Props.getDoctypePublicId() == null ||
                    doc2Props.getDoctypePublicId().compareTo(docTypePublicID) != 0 ||
                    doc2Props.getDoctypePublicId().compareTo(docType.getPublicId()) != 0)
                compareText.append("docTypePublicID was not as " +
                        "expected in the document properties " +
                        doc2Props.getDoctypePublicId()+"\n");

            if (doc2Props.getDoctypeSystemId() == null ||
                    doc2Props.getDoctypeSystemId().compareTo(docTypeSystemID) != 0 ||
                    doc2Props.getDoctypeSystemId().compareTo(docType.getSystemId()) != 0)
                compareText.append("docTypeSystemID was not as " +
                        "expected in the document properties "+
                        doc2Props.getDoctypeSystemId()+"\n" );
        } else {
            compareText.append("Document Properties were null, should have been set");
        }

        //cleanup
        po2 = null;
        po = null;
        new File(fileName).deleteOnExit();

        if (compareText.toString().length() > 1)
            throw new Exception("Doc properties were not saved or read correctly\n" + compareText.toString());
    }

    /**
     * XMLBEANS-78 - NPE when processing XMLStreamReader Midstream
     *
     * @throws Exception
     */
    public void test_jira_xmlbeans78() throws Exception
    {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        FileInputStream fis = new FileInputStream(JarUtil.getResourceFromJarasFile("xbean/misc/xmlbeans_78.xml"));
        XMLStreamReader reader = factory.createXMLStreamReader(fis);
        skipToBody(reader);
        XmlObject o = XmlObject.Factory.parse(reader);
    }

    /**
     * Move reader to element of SOAP Body
     *
     * @param reader
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    private void skipToBody(XMLStreamReader reader) throws javax.xml.stream.XMLStreamException
    {
        while (true) {
            int event = reader.next();
            switch (event) {
                case XMLStreamReader.END_DOCUMENT:
                    return;
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("Body")) {
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Repro case for jira issue
     * XMLBEANS-80  problems in XPath selecting with namespaces and Predicates.
     */
    public void test_jira_xmlbeans80() throws Exception
    {
        String xpathDoc = "<?xml version=\"1.0\"?> \n" +
                "<doc xmlns:ext=\"http://somebody.elses.extension\"> \n" +
                "  <ext:a test=\"test\" /> \n" +
                "  <b attr1=\"a1\" attr2=\"a2\" \n" +
                "  xmlns:java=\"http://xml.apache.org/xslt/java\"> \n" +
                "    <a> \n" +
                "    </a> \n" +
                "  </b> \n" +
                "</doc> ";
        XmlObject xb80 = XmlObject.Factory.parse(xpathDoc);
        XmlObject[] resSet = xb80.selectPath("declare namespace " +
                "ext='http://somebody.elses.extension'; $this//ext:a[@test='test']");

        Assert.assertTrue(resSet.length == 1);
        System.out.println("Result was: " + resSet[0].xmlText());
    }

    /**
     * Repro case for jira issue
     * XMLBEANS-81  Cursor selectPath() method not working with predicates
     */
    public void test_jira_xmlbeans81() throws Exception
    {
        String xpathDoc = "<MatchedRecords>" +
                "  <MatchedRecord>" +
                "     <TableName>" +
                "ABC" +
                "</TableName>" +
                "  </MatchedRecord>" +
                "  <MatchedRecord>" +
                "     <TableName>\n" +
                "       BCD \n" +
                "     </TableName> \n" +
                "  </MatchedRecord> \n" +
                "</MatchedRecords> ";
        XmlObject xb81 = XmlObject.Factory.parse(xpathDoc);
        XmlObject[] resSet = xb81.selectPath("$this//MatchedRecord[TableName=\"ABC\"]/TableName");
        assertEquals(resSet.length , 1);
        XmlCursor cursor = xb81.newCursor();
        cursor.selectPath("$this//MatchedRecord[TableName=\"ABC\"]/TableName");
    }

    /**
     * XMLBeans-84 Cannot run XmlObject.selectPath using Jaxen in multi threaded environment
     */
    public void test_jira_XmlBeans84() throws Exception
    {
        XPathThread[] threads = new XPathThread[15];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new XPathThread();
            System.out.println("Thread[" + i + "]-starting ");
            threads[i].start();
        }

        Thread.sleep(6000);
        System.out.println("Done with XPaths?...");

        for (int i = 0; i < threads.length; i++) {
            Assert.assertNull(threads[i].getException());
        }

    }


    /**
     * For Testing jira issue 46
     */
    public static class RegexThread extends TestThread
    {
        private xmlbeans46.UsPhoneNumberDocument phone;
        Random rand;

        public RegexThread()
        {
            super();
            phone = xmlbeans46.UsPhoneNumberDocument.Factory.newInstance();
            rand = new Random();
        }

        /**
         * Validates a type that uses the following pattern
         * <xs:restriction base="xs:string">
         * <xs:pattern value="\d{3}\-\d{3}\-\d{4}"/>
         * </xs:restriction>
         */
        public void run()
        {
            try {

                for (int i = 0; i < 9; i++) {
                    int pre = rand.nextInt(999);
                    int mid = rand.nextInt(999);
                    int post = rand.nextInt(9999);
                    String testVal = ((pre > 100) ? String.valueOf(pre) : "128") + "-" +
                            ((mid > 100) ? String.valueOf(mid) : "256") + "-" +
                            ((post > 1000) ? String.valueOf(post) : "1024");

                    String xmlData = "<xb:usPhoneNumber xmlns:xb=\"http://xmlbeans_46\">" +
                            testVal +
                            "</xb:usPhoneNumber>";
                    //cannot repro using this method
                    //phone.setUsPhoneNumber(testVal);
                    //if (!phone.validate(xm)) {
                    //    _throwable = new Throwable("Multi Threaded Regular " +
                    //            "Expression did not validate - " + testVal);
                    //    if (errors != null && errors.size() > 0)
                    //        System.err.println("ERROR: " + errors);
                    //}

                    boolean validated = parseAndValidate(xmlData);
                    if (!validated) {
                        System.out.println("Not Valid!!!");
                    }
                    System.out.println("Validated " + testVal + " successfully ");
                }
                _result = true;

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        private boolean parseAndValidate(String val) throws XmlException
        {
            xmlbeans46.UsPhoneNumberDocument xml = xmlbeans46.UsPhoneNumberDocument.Factory.parse(val);
            return validate(xml);
        }

        private boolean validate(xmlbeans46.UsPhoneNumberDocument rdd)
        {
            Collection errors = new ArrayList();
            XmlOptions validateOptions = new XmlOptions();
            validateOptions.setErrorListener(errors);
            boolean valid = rdd.validate(validateOptions);
            if (!valid) {
                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    XmlError xmlError = (XmlError) iterator.next();
                    System.out.println("XML Error - " + xmlError.getMessage() + " at\n" + xmlError.getCursorLocation().xmlText());
                }

            }
            return valid;
        }
    }

    /**
     * For Testing jira issue 84
     */
    public static class XPathThread extends TestThread
    {
        public XPathThread()
        {
            super();
        }

        public void run()
        {

            try {
                for (int i = 0; i < ITERATION_COUNT; i++) {
                    switch (i % 2) {
                        case 0:
                            runStatusXPath();
                            break;
                        case 1:
                            runDocXPath();
                            break;
                        default:
                            System.out.println("Val: " + i);
                            break;
                    }

                }
                _result = true;

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        public void runStatusXPath()
        {
            try {
                System.out.println("Testing Status");
                String statusDoc = "<statusreport xmlns=\"http://openuri.org/enumtest\">\n" +
                        "  <status name=\"first\" target=\"all\">all</status>\n" +
                        "  <status name=\"second\" target=\"all\">few</status>\n" +
                        "  <status name=\"third\" target=\"none\">most</status>\n" +
                        "  <status name=\"first\" target=\"none\">none</status>\n" +
                        "</statusreport>";
                XmlObject path = XmlObject.Factory.parse(statusDoc, xm);
                XmlObject[] resSet = path.selectPath("//*:status");
                Assert.assertTrue( resSet.length+"",resSet.length == 4);
                resSet = path.selectPath("//*:status[@name='first']");
                Assert.assertTrue(resSet.length == 2);

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        public void runDocXPath()
        {
            try {
                System.out.println("Testing Doc");
                String docDoc = "<?xml version=\"1.0\"?>\n" +
                        "<doc xmlns:ext=\"http://somebody.elses.extension\">\n" +
                        "  <a test=\"test\" />\n" +
                        "  <b attr1=\"a1\" attr2=\"a2\"   \n" +
                        "  xmlns:java=\"http://xml.apache.org/xslt/java\">\n" +
                        "    <a>\n" +
                        "    </a> \n" +
                        "  </b>\n" +
                        "</doc><!-- -->  ";
                XmlObject path = XmlObject.Factory.parse(docDoc, xm);
                XmlObject[] resSet = path.selectPath("//a");
                Assert.assertTrue(resSet.length == 2);
                resSet = path.selectPath("//b[@attr2]");
                Assert.assertTrue(resSet.length == 1);

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }
    }

    public static abstract class TestThread extends Thread
    {
        protected Throwable _throwable;
        protected boolean _result;
        protected XmlOptions xm;
        protected ArrayList errors;

        public TestThread()
        {
            xm = new XmlOptions();
            ArrayList errors = new ArrayList();
            xm.setErrorListener(errors);
            xm.setValidateOnSet();
        }

        public Throwable getException()
        {
            return _throwable;
        }

        public boolean getResult()
        {
            return _result;
        }


    }


}

