/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package drtcases;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.store.Root;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XMLStreamValidationException;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlToken;
import org.apache.xmlbeans.XmlNormalizedString;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Types from substgroup.xsd
import org.openuri.sgs.ADocument;
import org.openuri.sgs.BDocument;
import org.openuri.sgs.CDocument;
import org.openuri.sgs.RootDocument;

import org.w3.x2001.xmlSchema.SchemaDocument;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SubstGroupTests extends TestCase
{
    public SubstGroupTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(SubstGroupTests.class); }

    public void test1() throws Exception {
        String xml1 = "<root xmlns='http://openuri.org/sgs'>" +
            "<A>\ta\ta\t</A>" +
            "<B>\tb\tb\t</B>" +
            "<C>\tc\tc\t</C>" +
            "</root>";


        RootDocument doc1 = RootDocument.Factory.parse(xml1);
        RootDocument.Root root = doc1.getRoot();
        assertTrue(doc1.validate());

        XmlString a = root.xgetAArray(0);
        assertTrue(a.schemaType().equals(XmlString.type));
        assertEquals("\ta\ta\t", a.stringValue());

        XmlString b = root.xgetAArray(1);
        assertTrue(b.schemaType().equals(XmlNormalizedString.type));
        assertEquals(" b b ", b.stringValue());

        XmlString c = root.xgetAArray(2);
        assertTrue(c.schemaType().equals(XmlToken.type));
        assertEquals("c c", c.stringValue());

        root.insertA(2, "d d");
        assertEquals("d d", root.getAArray(2));
        assertEquals(4, root.sizeOfAArray());
        root.removeA(2);

        root.removeA(1);
        assertEquals("c c", root.getAArray(1));
        assertEquals(2, root.sizeOfAArray());

        root.addA("f f");
        assertEquals(3, root.sizeOfAArray());
        assertEquals("f f", root.getAArray(2));

        // Test array setters

        // test m < n case
        String[] smaller = new String[]{ "x", "y" };
        root.setAArray(smaller);
        assertEquals(2, root.sizeOfAArray());
        assertEquals("y", root.getAArray(1));

        // test m > n case
        String[] larger = new String[] { "p", "q", "r", "s" };
        root.setAArray(larger);
        assertEquals(4, root.sizeOfAArray());
        assertEquals("r", root.getAArray(2));
    }

    public void test2() throws Exception {
        String xml1 = "<A xmlns='http://openuri.org/sgs'>\ta\ta\t</A>";
        String xml2 = "<B xmlns='http://openuri.org/sgs'>\tb\tb\t</B>";
        String xml3 = "<C xmlns='http://openuri.org/sgs'>\tc\tc\t</C>";

        ADocument d1 = ADocument.Factory.parse(xml1);
        XmlString a = d1.xgetA();
        assertTrue(a.schemaType().equals(XmlString.type));
        assertEquals("\ta\ta\t", a.stringValue());

        ADocument d2 = ADocument.Factory.parse(xml2);
        XmlString b = d2.xgetA();
        assertTrue(d2.schemaType().equals(BDocument.type));
        assertTrue(b.schemaType().equals(XmlNormalizedString.type));
        assertEquals(" b b ", b.stringValue());

        ADocument d3 = ADocument.Factory.parse(xml3);
        XmlString c = d3.xgetA();
        assertTrue(d3.schemaType().equals(CDocument.type));
        assertTrue(c.schemaType().equals(XmlToken.type));
        assertEquals("c c", c.stringValue());
    }

    public static final String[] invalidSchemas = 
    {
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string'/> " +
        "  <xsd:element name='B' type='xsd:int' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:complexType name='foo'> " +
        "    <xsd:sequence> " +
        "      <xsd:element name='bar' substitutionGroup='A'/>" +
        "    </xsd:sequence> " +
        "  </xsd:complexType>" +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string' final='#all'/> " +
        "  <xsd:element name='B' type='xsd:string' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string' final='restriction'/> " +
        "  <xsd:element name='B' type='xsd:token' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string' substitutionGroup='B'/> " +
        "  <xsd:element name='B' type='xsd:string' substitutionGroup='C'/> " +
        "  <xsd:element name='C' type='xsd:string' substitutionGroup='D'/> " +
        "  <xsd:element name='D' type='xsd:string' substitutionGroup='E'/> " +
        "  <xsd:element name='E' type='xsd:string' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:token' substitutionGroup='B'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string'/> " +
        "  <xsd:element name='B' type='xsd:string' substitutionGroup='A'/> " +
        "  <xsd:element name='Complex'> " +
        "    <xsd:complexType> " +
        "      <xsd:choice> " +
        "        <xsd:element ref='A'/>" +
        "        <xsd:element ref='B'/>" +
        "      </xsd:choice> " +
        "    </xsd:complexType> " +
        "  </xsd:element> " +
        "</xsd:schema>",
    };

    public static final String[] validSchemas = 
    {
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
        "  <xsd:complexType name='base'>" +
        "    <xsd:all>" +
        "      <xsd:element ref='head'/>" +
        "    </xsd:all>" +
        "  </xsd:complexType>" +
        "  <xsd:complexType name='restr'>" +
        "    <xsd:complexContent>" +
        "       <xsd:restriction base='base'>" +
        "         <xsd:all>" +
        "           <xsd:element ref='tail'/>" +
        "         </xsd:all>" +
        "       </xsd:restriction>" +
        "    </xsd:complexContent>" +
        "  </xsd:complexType>" +
        "  <xsd:element name='head' type='xsd:string'/>" +
        "  <xsd:element name='tail' substitutionGroup='head'/>" +
        "</xsd:schema>",
    };
        
    public void test3() throws Exception {
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
            catch (XmlException success) { /* System.out.println(success); */ }
        }


        // Parse the valid schema files
        schemas = new SchemaDocument[validSchemas.length];
        for (int i = 0 ; i < validSchemas.length ; i++)
            schemas[i] = SchemaDocument.Factory.parse(validSchemas[i]);

        // Now compile the valid schemas, test that they succeed
        for (int i = 0 ; i < schemas.length ; i++)
        {
            try {
                XmlBeans.loadXsd(new XmlObject[] {schemas[i]});
            }
            catch (XmlException fail)
            {
               fail("Failed to compile schema: " + schemas[i] + " with error: " + fail);
            }
        }
    }

    public static String[] invalidDocs = 
    {
        "<abstractTest xmlns='http://openuri.org/sgs'>" +
        "    <abstract>content</abstract> " +
        "</abstractTest> ",
    };

    public static String[] validDocs = 
    {
        "<abstractTest xmlns='http://openuri.org/sgs'>" +
        "    <concrete>content</concrete> " +
        "</abstractTest> ",
    };

    public void test4() throws Exception 
    {

        for (int i = 0 ; i < invalidDocs.length ; i++)
        {
            XmlObject xo = XmlObject.Factory.parse(invalidDocs[i]);
            assertTrue("Doc was valid. Should be invalid: " + invalidDocs[i], 
                ! xo.validate());
        }

        for (int i = 0 ; i < validDocs.length ; i++)
        {
            XmlObject xo = XmlObject.Factory.parse(validDocs[i]);
            assertTrue("Doc was invalid. Should be valid: " + validDocs[i],
                xo.validate());
        }
    }

}
