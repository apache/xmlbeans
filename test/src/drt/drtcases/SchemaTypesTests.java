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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCalendar;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.InputStream;

import org.openuri.xstypes.test.*;
import org.openuri.def.DefaultsDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;

import javax.xml.namespace.QName;

public class SchemaTypesTests extends TestCase
{
    public SchemaTypesTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(SchemaTypesTests.class); }

    private CustomerDocument doc;

    private void ensureDoc()
        throws Exception
    {
        if( doc==null )
        {
            doc = (CustomerDocument)
                    XmlObject.Factory.parse(TestEnv.xbeanCase("../src/xstypes/xstypes.xml"));
        }
    }

    public void testDefaults() throws Throwable
    {
        DefaultsDocument doc = DefaultsDocument.Factory.newInstance();
        DefaultsDocument.Defaults defs = doc.addNewDefaults();
        Assert.assertEquals(783, defs.getCool()); // this is the default value
    }

    public void testSourceName() throws Throwable
    {
        String name = DefaultsDocument.type.getSourceName();
        Assert.assertEquals("defaults.xsd", name);
        InputStream str = XmlBeans.getContextTypeLoader().getSourceAsStream("defaults.xsd");
        SchemaDocument doc = SchemaDocument.Factory.parse(str);
        Assert.assertTrue(doc.validate());
    }

    public void testRead() throws Throwable
    {
        ensureDoc();

        // Move from the root to the root customer element
        Person person = doc.getCustomer();
        Assert.assertEquals("Howdy", person.getFirstname());
        Assert.assertEquals(4,   person.sizeOfNumberArray());
        Assert.assertEquals(436, person.getNumberArray(0));
        Assert.assertEquals(123, person.getNumberArray(1));
        Assert.assertEquals(44,  person.getNumberArray(2));
        Assert.assertEquals(933, person.getNumberArray(3));
        Assert.assertEquals(2,   person.sizeOfBirthdayArray());
        Assert.assertEquals(new XmlCalendar("1998-08-26Z"),
             person.getBirthdayArray(0));
        Assert.assertEquals(new XmlCalendar("2000-08-06-08:00"),
             person.getBirthdayArray(1));

        Person.Gender.Enum g = person.getGender();
        Assert.assertEquals(Person.Gender.MALE, g);

        Assert.assertEquals("EGIQTWYZJ", new String(person.getHex()));
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64()));

        Assert.assertEquals("GGIQTWYGG", new String(person.getHexAtt()));
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64Att()));

        Assert.assertEquals("{some_uri}localname", person.getQnameAtt().toString());
        Assert.assertEquals("{http://openuri.org/xstypes/test}openuri_org_localname", person.getQname().toString());

        Assert.assertEquals("http://dmoz.org/World/Fran\u00e7ais/", person.getAnyuriAtt().toString());
        Assert.assertEquals("http://3space.org/space%20space/", person.getAnyuri().toString());

        //RuntimeException: src/xmlstore/com/bea/xml/store/Splay.java(1537): ns != null && ns.length() > 0 failed
        //Assert.assertEquals("JPEG", person.getNotationAtt().toString());
        //Assert.assertEquals("GIF", person.getNotation().toString());
    }

    public void testWriteRead() throws Throwable
    {
        ensureDoc();
        // Move from the root to the root customer element
        Person person = doc.getCustomer();

        person.setFirstname("George");
        Assert.assertEquals("George", person.getFirstname());

        person.setHex("hex encoding".getBytes());
        Assert.assertEquals("hex encoding", new String(person.getHex()));

        person.setBase64("base64 encoded".getBytes());
        Assert.assertEquals("base64 encoded",
                            new String(person.getBase64()));

        person.setHexAtt("hex encoding in attributes".getBytes());
        Assert.assertEquals("hex encoding in attributes",
                            new String(person.getHexAtt()));

        person.setBase64Att("This string is base64Binary encoded!".getBytes());
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64Att()));

        person.setAnyuri("a.c:7001");
        Assert.assertEquals("a.c:7001", person.getAnyuri());

        person.setAnyuriAtt("b.d:7002");
        Assert.assertEquals("b.d:7002", person.getAnyuriAtt());

        person.setQnameAtt(new QName("aaa","bbb"));
        Assert.assertEquals("{aaa}bbb", person.getQnameAtt().toString());

        person.setQname(new QName("ddd","eee"));
        Assert.assertEquals("{ddd}eee", person.getQname().toString());

        //Exception: src/xmlstore/com/bea/xml/store/Type.java(189): user == _user failed
//        person.setAnyuriAtt(URI.create("b.d:7002"));
//        Assert.assertEquals("b.d:7002", person.getAnyuriAtt().toString());

        //XmlNOTATION notation = (XmlNOTATION)Person.Notation.type.createNode();
        //notation.setValue("JPEG");
        //person.setNotation( notation );
        //Assert.assertEquals("JPEG", person.getNotation().toString());

        //XmlNOTATION notationAtt = (XmlNOTATION)Person.NotationAtt.type.createNode();
        //notationAtt.setValue("GIF");
        //person.setNotationAtt( notationAtt );
        //person.setNotationAtt(notation);
        //Assert.assertEquals("GIF", person.getNotationAtt().toString());
    }

    public void testStoreWrite() throws Throwable
    {
        ensureDoc();
        // Move from the root to the root customer element
        Person person = doc.getCustomer();

        XmlObject xmlobj;
        XmlCursor xmlcurs;

        person.setFirstname("George");
        xmlobj = person.xgetFirstname();
        xmlcurs = xmlobj.newCursor();
        Assert.assertEquals("George", xmlcurs.getTextValue() );

        person.setQnameAtt( new QName("http://ggg.com","hhh") );
        xmlobj = person.xgetQnameAtt();
        xmlcurs = xmlobj.newCursor();
        Assert.assertEquals("ggg:hhh", xmlcurs.getTextValue() );

        person.setQname( new QName("http://ggg.com/gggAgain","kkk") );
        xmlobj = person.xgetQname();
        xmlcurs = xmlobj.newCursor();
        Assert.assertEquals("ggg1:kkk", xmlcurs.getTextValue() );

        person.setAnyuri( "crossgain.com" );
        xmlobj = person.xgetAnyuri();
        xmlcurs = xmlobj.newCursor();
        Assert.assertEquals("crossgain.com", xmlcurs.getTextValue() );

        person.setAnyuriAtt( "www.crossgain.com" );
        xmlobj = person.xgetAnyuriAtt();
        xmlcurs = xmlobj.newCursor();
        Assert.assertEquals("www.crossgain.com", xmlcurs.getTextValue() );

        //person.setNotation("GIF");
        //xmlobj = person.getNotation();
        //xmlcurs = xmlobj.newXmlCursor();
        //Assert.assertEquals("GIF", xmlcurs.getText() );

        //person.setNotationAtt("JPEGu");
        //xmlobj = person.xgetNotationAtt();
        //xmlcurs = xmlobj.newXmlCursor();
        //Assert.assertEquals("JPEG", xmlcurs.getText() );
    }
}
