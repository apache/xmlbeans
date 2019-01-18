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


package xmlobject.schematypes.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.Test;
import org.openuri.def.DefaultsDocument;
import org.openuri.xstypes.test.CustomerDocument;
import org.openuri.xstypes.test.Person;
import tools.util.JarUtil;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchemaTypesTests {

    private CustomerDocument doc;

    private void ensureDoc()
        throws Exception {
        if (doc == null) {
            doc = (CustomerDocument)
                XmlObject.Factory.parse(
                    JarUtil.getResourceFromJarasFile("xbean/xmlobject/person.xml"));
        }
    }

    @Test
    public void testDefaults() {
        DefaultsDocument doc = DefaultsDocument.Factory.newInstance();
        DefaultsDocument.Defaults defs = doc.addNewDefaults();
        assertEquals(783, defs.getCool()); // this is the default value
    }

    @Test
    public void testSourceName() throws IOException, XmlException {
        String name = DefaultsDocument.type.getSourceName();
        assertEquals("defaults.xsd", name);
        InputStream str = XmlBeans.getContextTypeLoader().getSourceAsStream("defaults.xsd");
        SchemaDocument doc = SchemaDocument.Factory.parse(str);
        assertTrue(doc.validate());
    }

    @Test
    public void testRead() throws Exception {
        ensureDoc();

        // Move from the root to the root customer element
        Person person = doc.getCustomer();
        assertEquals("Howdy", person.getFirstname());
        assertEquals(4,   person.sizeOfNumberArray());
        assertEquals(436, person.getNumberArray(0));
        assertEquals(123, person.getNumberArray(1));
        assertEquals(44,  person.getNumberArray(2));
        assertEquals(933, person.getNumberArray(3));
        assertEquals(2,   person.sizeOfBirthdayArray());
        assertEquals(new XmlCalendar("1998-08-26Z"),
             person.getBirthdayArray(0));
        assertEquals(new XmlCalendar("2000-08-06-08:00"),
             person.getBirthdayArray(1));

        Person.Gender.Enum g = person.getGender();
        assertEquals(Person.Gender.MALE, g);

        assertEquals("EGIQTWYZJ", new String(person.getHex()));
        assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64()));

        assertEquals("GGIQTWYGG", new String(person.getHexAtt()));
        assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64Att()));

        assertEquals("{some_uri}localname", person.getQnameAtt().toString());
        assertEquals("{http://openuri.org/xstypes/test}openuri_org_localname", person.getQname().toString());

        //assertEquals("http://dmoz.org/World/Fran\u00e7ais/", person.getAnyuriAtt().toString());
        assertEquals("http://3space.org/space%20space/", person.getAnyuri().toString());

        //RuntimeException: src/xmlstore/org/apache/xmlbeans/impl/store/Splay.java(1537): ns != null && ns.length() > 0 failed
        //assertEquals("JPEG", person.getNotationAtt().toString());
        //assertEquals("GIF", person.getNotation().toString());
    }

    @Test
    public void testWriteRead() throws Exception {
        ensureDoc();
        // Move from the root to the root customer element
        Person person = doc.getCustomer();

        person.setFirstname("George");
        assertEquals("George", person.getFirstname());

        person.setHex("hex encoding".getBytes());
        assertEquals("hex encoding", new String(person.getHex()));

        person.setBase64("base64 encoded".getBytes());
        assertEquals("base64 encoded",
                            new String(person.getBase64()));

        person.setHexAtt("hex encoding in attributes".getBytes());
        assertEquals("hex encoding in attributes",
                            new String(person.getHexAtt()));

        person.setBase64Att("This string is base64Binary encoded!".getBytes());
        assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64Att()));

        person.setAnyuri("a.c:7001");
        assertEquals("a.c:7001", person.getAnyuri());

        person.setAnyuriAtt("b.d:7002");
        assertEquals("b.d:7002", person.getAnyuriAtt());

        person.setQnameAtt(new QName("aaa","bbb"));
        assertEquals("{aaa}bbb", person.getQnameAtt().toString());

        person.setQname(new QName("ddd","eee"));
        assertEquals("{ddd}eee", person.getQname().toString());

        //Exception: src/xmlstore/org/apache/xmlbeans/impl/store/Type.java(189): user == _user failed
//        person.setAnyuriAtt(URI.create("b.d:7002"));
//        assertEquals("b.d:7002", person.getAnyuriAtt().toString());

        //XmlNOTATION notation = (XmlNOTATION)Person.Notation.type.createNode();
        //notation.setValue("JPEG");
        //person.setNotation( notation );
        //assertEquals("JPEG", person.getNotation().toString());

        //XmlNOTATION notationAtt = (XmlNOTATION)Person.NotationAtt.type.createNode();
        //notationAtt.setValue("GIF");
        //person.setNotationAtt( notationAtt );
        //person.setNotationAtt(notation);
        //assertEquals("GIF", person.getNotationAtt().toString());
    }

    @Test
    public void testStoreWrite() throws Exception {
        ensureDoc();
        // Move from the root to the root customer element
        Person person = doc.getCustomer();

        XmlObject xmlobj;
        XmlCursor xmlcurs;

        person.setFirstname("George");
        xmlobj = person.xgetFirstname();
        xmlcurs = xmlobj.newCursor();
        assertEquals("George", xmlcurs.getTextValue() );

        person.setQnameAtt( new QName("http://ggg.com","hhh") );
        xmlobj = person.xgetQnameAtt();
        xmlcurs = xmlobj.newCursor();
        assertEquals("ggg:hhh", xmlcurs.getTextValue() );

        person.setQname( new QName("http://ggg.com/gggAgain","kkk") );
        xmlobj = person.xgetQname();
        xmlcurs = xmlobj.newCursor();
        assertEquals("ggg1:kkk", xmlcurs.getTextValue() );

        person.setAnyuri( "crossgain.com" );
        xmlobj = person.xgetAnyuri();
        xmlcurs = xmlobj.newCursor();
        assertEquals("crossgain.com", xmlcurs.getTextValue() );

        person.setAnyuriAtt( "www.crossgain.com" );
        xmlobj = person.xgetAnyuriAtt();
        xmlcurs = xmlobj.newCursor();
        assertEquals("www.crossgain.com", xmlcurs.getTextValue() );

        //person.setNotation("GIF");
        //xmlobj = person.getNotation();
        //xmlcurs = xmlobj.newXmlCursor();
        //assertEquals("GIF", xmlcurs.getText() );

        //person.setNotationAtt("JPEGu");
        //xmlobj = person.xgetNotationAtt();
        //xmlcurs = xmlobj.newXmlCursor();
        //assertEquals("JPEG", xmlcurs.getText() );
    }
}
