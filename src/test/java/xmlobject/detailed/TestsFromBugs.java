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

package xmlobject.detailed;

import com.mytest.Bar;
import com.mytest.Foo;
import com.mytest.Info;
import com.mytest.TestDocument;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;

import org.junit.jupiter.api.Test;
import test.xmlobject.test36510.Test36510AppDocument;

import static org.apache.xmlbeans.XmlBeans.compileXmlBeans;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test file that implements test cases that come from closing bugs.
 */
public class TestsFromBugs {
    /**
     * Radar Bug: 36156
     * Problem with Namespace leaking into siblings
     */
    @Test
    void test36156()
        throws Exception {
        String str = "<x><y xmlns=\"bar\"><z xmlns=\"foo\"/></y><a/></x>";
        XmlObject x = XmlObject.Factory.parse(str);

        assertEquals(x.xmlText(), str, "Test 36156 failed: ");
    }

    /*
     * Radar Bug: 36510
     */
    @Test
    void test36510()
        throws Exception {
        String str =
            "<test36510-app version='1.0' " +
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
            " xsi:schemaLocation='http://test/xmlobject/test36510' " +
            "xmlns='http://test/xmlobject/test36510'>" +
            "<testConstraint>" +
            "<customConstraint>" +
            "<description>These portlets don't" +
            " require any guarantee</description>" +
            "<options>BEST</options>" +
            "</customConstraint></testConstraint>" +
            "</test36510-app>";

        Test36510AppDocument doc = Test36510AppDocument.Factory.parse(str);
        str = doc.getTest36510App().getTestConstraintArray()[0].
            getCustomConstraint().getOptions().toString();
        assertEquals("BEST", str, "Test 36510 failed: ");
    }


    /*
     * Radar Bug: 40907
     */
    @Test
    void test40907()
        throws Exception {
        String str =
            "<myt:Test xmlns:myt=\"http://www.mytest.com\">" +
            "<myt:foo>" +
            "<myt:fooMember>this is foo member</myt:fooMember>" +
            "</myt:foo>" +
            "</myt:Test>";
        TestDocument doc = TestDocument.Factory.parse(str);

        assertTrue(doc.validate(), "XML Instance did not validate.");

        Bar bar = Bar.Factory.newInstance();
        bar.setFooMember("new foo member");
        bar.setBarMember("new bar member");

        Info info = doc.getTest();

        Foo foo = info.addNewFoo();
        foo.set(bar);

        assertTrue(doc.validate(), "Modified XML instance did not validate.");
        str = "<myt:Test xmlns:myt=\"http://www.mytest.com\">" +
            "<myt:foo>" +
            "<myt:fooMember>this is foo member</myt:fooMember>" +
            "</myt:foo>" +
            "<myt:foo xsi:type=\"myt:bar\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<myt:fooMember>new foo member</myt:fooMember>" +
            "<myt:barMember>new bar member</myt:barMember>" +
            "</myt:foo>" +
            "</myt:Test>";
        assertEquals(doc.xmlText(), str, "XML instance is not as expected");

    }

    /**
     * Simple Compilation Tests - If the methods are not present,
     * - this class won't compile
     * Ensures method getSourceName is on SchemaComponent and
     * can be called from SchemaGlobalElement and SchemaGlobalAttribute
     */
    @Test
    void test199585() throws Exception {
        String str =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    targetNamespace=\"urn:lax.Doc.Compilation\"\n" +
            "    xmlns:tns=\"urn:lax.Doc.Compilation\"\n" +
            "    xmlns:pre=\"noResolutionNamespace\"\n" +
            "    elementFormDefault=\"qualified\"\n" +
            "    attributeFormDefault=\"unqualified\">\n" +
            "   <xs:attribute name=\"GlobalAtt\" type=\"xs:string\" />\n" +
            "   <xs:element name=\"QuantityElement\" type=\"tns:quantity\" />\n" +
            "   <xs:simpleType name=\"quantity\">\n" +
            "    <xs:restriction base=\"xs:NMTOKEN\">\n" +
            "      <xs:enumeration value=\"all\"/>\n" +
            "      <xs:enumeration value=\"most\"/>\n" +
            "      <xs:enumeration value=\"some\"/>\n" +
            "      <xs:enumeration value=\"few\"/>\n" +
            "      <xs:enumeration value=\"none\"/>\n" +
            "    </xs:restriction>\n" +
            "  </xs:simpleType>" +
            "</xs:schema>";

        XmlObject[] schemas = {XmlObject.Factory.parse(str)};
        XmlOptions xOpt = new XmlOptions().setValidateTreatLaxAsSkip();

        SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null, schemas,
            null, XmlBeans.getBuiltinTypeSystem(), null, xOpt);

        //ensure SchemaGlobalElement has getSourceName Method
        SchemaGlobalElement[] els = sts.globalElements();
        assertEquals(1, els.length);
        assertDoesNotThrow(els[0]::getSourceName);

        //ensure SchemaGlobalAttribute has getSourceName Method
        SchemaGlobalAttribute[] ats = sts.globalAttributes();
        assertEquals(1, ats.length);
        assertDoesNotThrow(ats[0]::getSourceName);
    }
    
    /**
     * https://issues.apache.org/jira/browse/XMLBEANS-648
     */
    @Test
    void test648() throws Exception {
        final XmlOptions options = new XmlOptions();
        options.setCompileNoUpaRule();
        options.setCompileNoValidation();
        options.setCompileDownloadUrls();
    
        /* Load the schema */
        final SchemaTypeLoader contextTypeLoader =
            SchemaTypeLoaderImpl.build(new SchemaTypeLoader[] {BuiltinSchemaTypeSystem.get()}, null,
                                       Object.class.getClassLoader());
    
        String schemaString = "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://validationnamespace.raml.org\" attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://validationnamespace.raml.org\">\n"
            + "    <element name=\"__DataType_Fragment__\">\n"
            + "        <complexType>\n"
            + "            <sequence>\n"
            + "                <element name=\"firstname\">\n"
            + "                    <simpleType>\n"
            + "                        <restriction base=\"string\"/>\n"
            + "                    </simpleType>\n"
            + "                </element>\n"
            + "                <element name=\"lastname\">\n"
            + "                    <simpleType>\n"
            + "                        <restriction base=\"string\"/>\n"
            + "                    </simpleType>\n"
            + "                </element>\n"
            + "                <element name=\"age\">\n"
            + "                    <simpleType>\n"
            + "                        <restriction base=\"double\"/>\n"
            + "                    </simpleType>\n"
            + "                </element>\n"
            + "                <any maxOccurs=\"unbounded\" minOccurs=\"0\" processContents=\"skip\"/>\n"
            + "            </sequence>\n"
            + "        </complexType>\n"
            + "    </element>\n"
            + "</schema>\n"
            + "";
        final XmlObject[] schemaRepresentation = new XmlObject[] {contextTypeLoader.parse(schemaString, null, null)};
    
        XmlBeans.compileXmlBeans(null, null, schemaRepresentation, null, contextTypeLoader, null, options);
    }
}
