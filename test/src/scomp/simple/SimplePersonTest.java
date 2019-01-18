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

package scomp.simple;

import common.Common;
import org.junit.Test;
import org.openuri.mytest.CustomerDocument;
import org.openuri.mytest.Person;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class SimplePersonTest {
    @Test
    public void test() throws Exception {
        CustomerDocument doc =
            CustomerDocument.Factory.parse(
                Common.xbeanCase("xbean/simple/person/person.xml"), null);

        // Move from the root to the root customer element
        Person person = doc.getCustomer();
        assertEquals("Howdy", person.getFirstname());
        assertEquals(4, person.sizeOfNumberArray());
        assertEquals(436, person.getNumberArray(0));
        assertEquals(123, person.getNumberArray(1));
        assertEquals(44, person.getNumberArray(2));
        assertEquals(933, person.getNumberArray(3));
        assertEquals(2, person.sizeOfBirthdayArray());
        Calendar cal = person.getBirthdayArray(0);
        cal.set(1998, 7, 25, 17, 0);
        assertEquals(1998, cal.get(Calendar.YEAR));
        assertEquals(7, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DATE));
        assertEquals(17, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));

        Person.Gender.Enum g = person.getGender();
        assertEquals(Person.Gender.MALE, g);

        assertEquals("EGIQTWYZJ", new String(person.getHex()));
        assertEquals("This string is base64Binary encoded!",
            new String(person.getBase64()));

        assertEquals("GGIQTWYGG", new String(person.getHexAtt()));
        assertEquals("This string is base64Binary encoded!",
            new String(person.getBase64Att()));

        person.setFirstname("George");
        assertEquals("George", person.getFirstname());

        person.setHex("hex encoding".getBytes());
        assertEquals("hex encoding", new String(person.getHex()));

        person.setBase64("base64 encoded".getBytes());
        assertEquals("base64 encoded",
            new String(person.getBase64()));

        //person.setHexAtt("hex encoding in attributes".getBytes());
        //Assert.assertEquals("hex encoding in attributes",
        //                    new String(person.getHexAtt()));

        //person.setBase64Att("base64 encoded in attributes".getBytes());
        //Assert.assertEquals("base64 encoded in attributes",
        //                    new String(person.getBase64Att()));
//
//        XmlCursor cp = person.newXmlCursor();
//        Root.dump( cp );

//        XmlCursor c = person.xgetBirthdayArray(0).newXmlCursor();

//        Root.dump( c );

//        person.setBirthday(0,new Date("Tue Aug 25 16:00:00 PDT 2001"));

//        Root.dump( c );

//        c.toNextToken();

//        System.out.println( "---" + c.getText() + "---" );

//        Root.dump( c );

//        Assert.assertEquals(person.getBirthdayArray(0), new Date("Tue Aug 25 16:00:00 PDT 2002"));
//
//        person.setFirstname("George");
//        Assert.assertEquals(person.getFirstname(), "George");
//
//        person.addNumber( (short) 69 );
//        Assert.assertEquals(person.countNumber(), 5);
//        Assert.assertEquals(person.getNumberArray(4), 69);
//
//
//        while ( c.hasNextToken() )
//            c.toNextToken();
    }
}
