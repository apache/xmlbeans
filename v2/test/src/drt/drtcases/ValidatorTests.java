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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValidatorTests extends TestCase
{
    public ValidatorTests(String name)
    { super(name); }

    public static Test suite()
    { return new TestSuite(ValidatorTests.class); }

    static String[] _args;
    static String _test;

    public static File getCaseFile(String theCase)
    {
        return TestEnv.xbeanCase("store/" + theCase);
    }

    static XmlCursor loadCase(String theCase) throws Exception
    {
        return XmlObject.Factory.parse(getCaseFile(theCase)).newCursor();
    }

    public SchemaTypeLoader makeSchemaTypeLoader(String[] schemas) throws XmlException
    {
        XmlObject[] schemaDocs = new XmlObject[schemas.length];

        for (int i = 0; i < schemas.length; i++)
        {
            schemaDocs[i] = XmlObject.Factory.parse(schemas[i]);
        }

        return XmlBeans.loadXsd(schemaDocs);
    }

    public SchemaTypeLoader makeSchemaTypeLoader(File[] schemas) throws XmlException, IOException
    {
        XmlObject[] schemaDocs = new XmlObject[schemas.length];

        for (int i = 0; i < schemas.length; i++)
        {
            schemaDocs[i] = XmlObject.Factory.parse(schemas[i], new XmlOptions().setLoadLineNumbers().setLoadMessageDigest());
        }

        return XmlBeans.loadXsd(schemaDocs);
    }


    public List performValidation(String[] schemas, QName docType, String instances,
                                  boolean startOnDocument) throws XmlException
    {
        SchemaTypeLoader stl = makeSchemaTypeLoader(schemas);

        XmlOptions options = new XmlOptions();

        if (docType != null)
        {
            SchemaType docSchema = stl.findDocumentType(docType);

            Assert.assertTrue(docSchema != null);

            options.setDocumentType(docSchema);
        }

        XmlObject x = stl.parse( instances, null, options);

        if (!startOnDocument)
        {
            XmlCursor c = x.newCursor();
            c.toFirstChild();
            x = c.getObject();
            c.dispose();
        }

        List xel = new ArrayList();
        options.setErrorListener(xel);

        x.validate(options);

        return xel;
    }


    public void testValidationElementError() throws XmlException
    {
        String bobSchema = "<xs:schema\n" + "   xmlns:xs='http://www.w3.org/2001/XMLSchema'\n" + "   xmlns:bob='http://openuri.org/bobschema'\n" + "   targetNamespace='http://openuri.org/bobschema'\n" + "   elementFormDefault='qualified'>\n" + "\n" + "  <xs:complexType name='biff'>\n" + "   <xs:complexContent>\n" + "    <xs:extension base='bob:foo'>\n" + "     <xs:sequence>\n" + "       <xs:element name='a' minOccurs='0' maxOccurs='unbounded'/>\n" + "     </xs:sequence>\n" + "    </xs:extension>\n" + "   </xs:complexContent>\n" + "  </xs:complexType>\n" + "" + "  <xs:complexType name='foo'>\n" + "  </xs:complexType>\n" + "" + "  <xs:element name='foo' type='bob:foo'>\n" + "  </xs:element>\n" + "" + "</xs:schema>\n";

        String invalid = "<bob:foo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:bob='http://openuri.org/bobschema' " + "xsi:type='bob:biff'><bob:q/></bob:foo>";

        String[] schemas = {bobSchema};

        List errors = null;

        errors = performValidation(schemas, null, invalid, true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        for (Iterator it = errors.iterator(); it.hasNext();)
        {
            XmlValidationError xmlValError = (XmlValidationError) it.next();
            Assert.assertEquals(xmlValError.getErrorType(), XmlValidationError.INCORRECT_ELEMENT);
            Assert.assertEquals(xmlValError.getBadSchemaType().getName().getLocalPart(), "biff");
            Assert.assertEquals(xmlValError.getOffendingQName().getLocalPart(), "q");
            Assert.assertEquals(xmlValError.getMessage(), "Expected element a@http://openuri.org/bobschema instead of q@http://openuri.org/bobschema here in element foo@http://openuri.org/bobschema");
        }
    }


    public void testValidationAttributeError() throws XmlException
    {
        StringBuffer empSchema = new StringBuffer();

        empSchema.append("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'   elementFormDefault='qualified'>\n");
        empSchema.append("<xs:element name='age'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:integer'>\n");
        empSchema.append("<xs:minInclusive value='0'/>\n");
        empSchema.append("<xs:maxInclusive value='100'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='empRecords'>\n");
        empSchema.append("<xs:complexType>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element name='person' type='personType' maxOccurs='unbounded'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='name' type='xs:string'/>\n");
        empSchema.append("<xs:complexType name='personType'>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element ref='name'/>\n");
        empSchema.append("<xs:element ref='age'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("<xs:attribute name='employee' use='required'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:NMTOKEN'>\n");
        empSchema.append("<xs:enumeration value='current'/>\n");
        empSchema.append("<xs:enumeration value='past'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:attribute>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:schema>\n");

        StringBuffer xmlInstance = new StringBuffer();
        xmlInstance.append("<empRecords xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' >");
        xmlInstance.append("<person employee='past'>");
        xmlInstance.append("<name>joe blow</name>");
        xmlInstance.append("<age>31</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("<person>");
        xmlInstance.append("<name>test user</name>");
        xmlInstance.append("<age>29</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("</empRecords>");

        String[] schemas = {empSchema.toString()};

        List errors = null;

        errors = performValidation(schemas, null, xmlInstance.toString(), true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        for (Iterator it = errors.iterator(); it.hasNext();)
        {
            XmlValidationError xmlValError = (XmlValidationError) it.next();
            Assert.assertEquals(xmlValError.getErrorType(), XmlValidationError.INCORRECT_ATTRIBUTE);
            Assert.assertEquals(xmlValError.getBadSchemaType().getName().getLocalPart(), "personType");
            Assert.assertEquals(xmlValError.getOffendingQName().getLocalPart(), "employee");
            Assert.assertEquals(xmlValError.getMessage(), "Expected attribute: employee in element person");
        }
    }

    public void testValidationIncorrectElementError() throws XmlException
    {
        StringBuffer empSchema = new StringBuffer();

        empSchema.append("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'   elementFormDefault='qualified'>\n");
        empSchema.append("<xs:element name='age'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:integer'>\n");
        empSchema.append("<xs:minInclusive value='0'/>\n");
        empSchema.append("<xs:maxInclusive value='100'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='empRecords'>\n");
        empSchema.append("<xs:complexType>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element name='person' type='personType' maxOccurs='unbounded'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='name' type='xs:string'/>\n");
        empSchema.append("<xs:complexType name='personType'>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element ref='name'/>\n");
        empSchema.append("<xs:element ref='age'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("<xs:attribute name='employee' use='required'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:NMTOKEN'>\n");
        empSchema.append("<xs:enumeration value='current'/>\n");
        empSchema.append("<xs:enumeration value='past'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:attribute>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:schema>\n");

        StringBuffer xmlInstance = new StringBuffer();
        xmlInstance.append("<empRecords xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' >");
        xmlInstance.append("<person employee='past'>");
        //xmlInstance.append("<name>joe blow</name>");
        xmlInstance.append("<age>31</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("<person employee='current'>");
        xmlInstance.append("<name>test user</name>");
        xmlInstance.append("<age>29</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("</empRecords>");

        String[] schemas = {empSchema.toString()};

        List errors = null;

        errors = performValidation(schemas, null, xmlInstance.toString(), true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        Iterator it = errors.iterator();
        Assert.assertTrue(it.hasNext());

        XmlValidationError xmlValError = (XmlValidationError) it.next();
        Assert.assertEquals(XmlValidationError.INCORRECT_ELEMENT, xmlValError.getErrorType());
        Assert.assertEquals("personType", xmlValError.getBadSchemaType().getName().getLocalPart());
        Assert.assertEquals("age", xmlValError.getOffendingQName().getLocalPart());
        Assert.assertEquals("Expected element name instead of age here in element person", xmlValError.getMessage());

        Assert.assertTrue(it.hasNext());

        xmlValError = (XmlValidationError) it.next();
        Assert.assertEquals(XmlValidationError.INCORRECT_ELEMENT, xmlValError.getErrorType());
        Assert.assertEquals("personType", xmlValError.getBadSchemaType().getName().getLocalPart());
        Assert.assertEquals( null,xmlValError.getOffendingQName());
        Assert.assertEquals("Expected element name at the end of the content in element person", xmlValError.getMessage());
    }

    public void testValidationElementNotAllowedError() throws XmlException
    {
        StringBuffer empSchema = new StringBuffer();

        empSchema.append("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' elementFormDefault='qualified' attributeFormDefault='unqualified'> \n ");
        empSchema.append("<xs:element name='person'>\n");
        empSchema.append("<xs:complexType>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element name='firstname' type='xs:string'/>\n");
        empSchema.append("<xs:element name='lastname' type='xs:string'/>\n");
        empSchema.append("<xs:any minOccurs='0'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("</xs:schema>\n");


        StringBuffer xmlInstance = new StringBuffer();
        xmlInstance.append("<person xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' > \n");
        xmlInstance.append("<firstname>Joe</firstname>");
        xmlInstance.append("<lastname>blow</lastname>");
        xmlInstance.append("<blah></blah>");
        xmlInstance.append("</person>");


        String[] schemas = {empSchema.toString()};

        List errors = null;

        errors = performValidation(schemas, null, xmlInstance.toString(), true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        for (Iterator it = errors.iterator(); it.hasNext();)
        {
            XmlValidationError xmlValError = (XmlValidationError) it.next();
            Assert.assertEquals(xmlValError.getErrorType(), XmlValidationError.ELEMENT_NOT_ALLOWED);
            Assert.assertEquals(xmlValError.getMessage(), "Element not allowed (strict wildcard, and no definition found): blah in element person");
        }
    }


    public void testValidationAttributeTypeError() throws XmlException
    {
        StringBuffer empSchema = new StringBuffer();

        empSchema.append("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'   elementFormDefault='qualified'>\n");
        empSchema.append("<xs:element name='age'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:integer'>\n");
        empSchema.append("<xs:minInclusive value='0'/>\n");
        empSchema.append("<xs:maxInclusive value='100'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='empRecords'>\n");
        empSchema.append("<xs:complexType>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element name='person' type='personType' maxOccurs='unbounded'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='name' type='xs:string'/>\n");
        empSchema.append("<xs:complexType name='personType'>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element ref='name'/>\n");
        empSchema.append("<xs:element ref='age'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("<xs:attribute name='employee' use='required'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:NMTOKEN'>\n");
        empSchema.append("<xs:enumeration value='current'/>\n");
        empSchema.append("<xs:enumeration value='past'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:attribute>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:schema>\n");

        StringBuffer xmlInstance = new StringBuffer();
        xmlInstance.append("<empRecords xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' >");
        xmlInstance.append("<person employee='past'>");
        xmlInstance.append("<name>joe blow</name>");
        xmlInstance.append("<age>31</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("<person employee='current'>");
        xmlInstance.append("<name>test user</name>");
        xmlInstance.append("<age>junk</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("</empRecords>");

        String[] schemas = {empSchema.toString()};

        List errors = null;

        errors = performValidation(schemas, null, xmlInstance.toString(), true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        for (Iterator it = errors.iterator(); it.hasNext();)
        {
            XmlValidationError xmlValError = (XmlValidationError) it.next();
            Assert.assertEquals(xmlValError.getErrorType(), XmlValidationError.ATTRIBUTE_TYPE_INVALID);
            Assert.assertEquals(xmlValError.getMessage(), "Illegal decimal, unexpected char: 106");
        }
    }

    public void testElementError() throws XmlException
    {
        String bobSchema = "<xs:schema\n" + "   xmlns:xs='http://www.w3.org/2001/XMLSchema'\n" +
            "   xmlns:bob='http://openuri.org/bobschema'\n" +
            "   targetNamespace='http://openuri.org/bobschema'\n" +
            "   elementFormDefault='qualified'>\n" +
            "\n" +
            "  <xs:complexType name='biff'>\n" +
            "   <xs:complexContent>\n" +
            "    <xs:extension base='bob:foo'>\n" +
            "     <xs:sequence>\n" +
            "       <xs:element name='a' minOccurs='0' maxOccurs='unbounded'/>\n" +
            "     </xs:sequence>\n" +
            "    </xs:extension>\n" +
            "   </xs:complexContent>\n" +
            "  </xs:complexType>\n" + "" +
            "  <xs:complexType name='foo'>\n" +
            "  </xs:complexType>\n" + "" +
            "  <xs:element name='foo' type='bob:foo'>\n" +
            "  </xs:element>\n" + "" +
            "</xs:schema>\n";

        String invalid = "<bob:foo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
            "xmlns:bob='http://openuri.org/bobschema' " +
            "xsi:type='bob:biff'><bob:q/></bob:foo>";

        String[] schemas = {bobSchema};

        List errors = performValidation(schemas, null, invalid, true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        for (Iterator it = errors.iterator(); it.hasNext();)
        {
            XmlValidationError xmlError = (XmlValidationError) it.next();
            Assert.assertEquals(xmlError.getMessage(), "Expected element a@http://openuri.org/bobschema instead of q@http://openuri.org/bobschema here in element foo@http://openuri.org/bobschema");
        }
    }

    public void testAttributeError() throws XmlException
    {
        StringBuffer empSchema = new StringBuffer();

        empSchema.append("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'   elementFormDefault='qualified'>\n");
        empSchema.append("<xs:element name='age'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:integer'>\n");
        empSchema.append("<xs:minInclusive value='0'/>\n");
        empSchema.append("<xs:maxInclusive value='100'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='empRecords'>\n");
        empSchema.append("<xs:complexType>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element name='person' type='personType' maxOccurs='unbounded'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:element>\n");
        empSchema.append("<xs:element name='name' type='xs:string'/>\n");
        empSchema.append("<xs:complexType name='personType'>\n");
        empSchema.append("<xs:sequence>\n");
        empSchema.append("<xs:element ref='name'/>\n");
        empSchema.append("<xs:element ref='age'/>\n");
        empSchema.append("</xs:sequence>\n");
        empSchema.append("<xs:attribute name='employee' use='required'>\n");
        empSchema.append("<xs:simpleType>\n");
        empSchema.append("<xs:restriction base='xs:NMTOKEN'>\n");
        empSchema.append("<xs:enumeration value='current'/>\n");
        empSchema.append("<xs:enumeration value='past'/>\n");
        empSchema.append("</xs:restriction>\n");
        empSchema.append("</xs:simpleType>\n");
        empSchema.append("</xs:attribute>\n");
        empSchema.append("</xs:complexType>\n");
        empSchema.append("</xs:schema>\n");

        StringBuffer xmlInstance = new StringBuffer();
        xmlInstance.append("<empRecords xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' >");
        xmlInstance.append("<person employee='past'>");
        xmlInstance.append("<name>joe blow</name>");
        xmlInstance.append("<age>31</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("<person>");
        xmlInstance.append("<name>test user</name>");
        xmlInstance.append("<age>29</age>");
        xmlInstance.append("</person>");
        xmlInstance.append("</empRecords>");

        String[] schemas = {empSchema.toString()};

        List errors = null;

        errors = performValidation(schemas, null, xmlInstance.toString(), true);
        Assert.assertTrue(errors != null);
        Assert.assertTrue(errors.size()>0);

        for (Iterator it = errors.iterator(); it.hasNext();)
        {
            XmlValidationError xmlError = (XmlValidationError) it.next();
            Assert.assertEquals(xmlError.getMessage(), "Expected attribute: employee in element person");
        }
    }
}
