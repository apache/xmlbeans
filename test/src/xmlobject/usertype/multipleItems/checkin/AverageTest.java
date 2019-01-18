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

package xmlobject.usertype.multipleItems.checkin;


import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.Test;
import usertype.xbean.multipleItems.company.CompanyDocument;
import usertype.xbean.multipleItems.company.CompanyType;
import usertype.xbean.multipleItems.company.ConsultantType;
import usertype.xbean.multipleItems.company.DepartmentType;
import xmlobject.usertype.multipleItems.existing.Room;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;


public class AverageTest {

    @Test
    public void test() {
        CompanyDocument doc = CompanyDocument.Factory.newInstance();
        CompanyType company = doc.addNewCompany();
        DepartmentType dept = company.addNewDepartments();

        ConsultantType cons = dept.addNewConsultant();

        cons.setName("Joe Smith");
        cons.setAge(BigInteger.valueOf(100));

        int LEN=20;

        for (int i=0; i < LEN; i++) {
            cons.addRoom(new Room(i, "AB"));
        }

        System.out.println(doc.xmlText());

        Room[] rooms = cons.getRoomArray();

        for (int i=0; i < LEN; i++) {
            assertEquals(i, rooms[i].getDigits());
            assertEquals("AB", rooms[i].getLetters());
        }
    }


    @Test
    public void testArrayGetSet() {

        CompanyDocument doc = CompanyDocument.Factory.newInstance();
        CompanyType company = doc.addNewCompany();
        DepartmentType dept = company.addNewDepartments();

        ConsultantType cons = dept.addNewConsultant();

        cons.setName("Joe Smith");
        cons.setAge(BigInteger.valueOf(100));

        int LEN=20;

        Room[] rooms = new Room[LEN];

        for (int i=0; i < LEN; i++) {
            rooms[i] = new Room(i, "AB");
        }

        cons.setRoomArray(rooms);

        rooms = cons.getRoomArray();
        for (int i=0; i < LEN; i++) {
            assertEquals(i, rooms[i].getDigits());
            assertEquals("AB", rooms[i].getLetters());
        }


    }

    @Test
    public void testIthGetSet() {

        CompanyDocument doc = CompanyDocument.Factory.newInstance();
        CompanyType company = doc.addNewCompany();
        DepartmentType dept = company.addNewDepartments();

        ConsultantType cons = dept.addNewConsultant();

        cons.setName("Joe Smith");
        cons.setAge(BigInteger.valueOf(100));


        int LEN=20;

        for (int i=0; i < LEN; i++) {
            cons.addNewRoom();
        }


        for (int i=0; i < LEN; i++) {
            cons.setRoomArray(i, new Room(i, "AB"));
        }

        for (int i=0; i < LEN; i++) {
            assertEquals(i, cons.getRoomArray(i).getDigits());
            assertEquals("AB", cons.getRoomArray(i).getLetters());
        }


    }

    @Test(expected = XmlValueOutOfRangeException.class)
    public void testBadInput() throws XmlException{

        StringBuffer sb = new StringBuffer();
        sb.append("<com:company xmlns:com=\"http://xbean.usertype/multipleItems/company\">");
        sb.append("<departments><consultant name=\"Joe Smith\" age=\"100\">");
        sb.append("<room>000-AB</room><room>0001-AB</room><room>002-AB</room>");
        sb.append("</consultant></departments></com:company>");

        CompanyDocument doc = CompanyDocument.Factory.parse(sb.toString());

        CompanyType company = doc.getCompany();

        ConsultantType cons = company.getDepartmentsArray(0).getConsultantArray(0);
        assertEquals(3, cons.xgetRoomArray().length);

        cons.getRoomArray();
    }

    @Test(expected = XmlValueOutOfRangeException.class)
    public void testBadInputGetIthBad() throws XmlException{

        String sb =
            "<com:company xmlns:com=\"http://xbean.usertype/multipleItems/company\">" +
            "<departments><consultant name=\"Joe Smith\" age=\"100\">" +
            "<room>000-AB</room><room>0001-AB</room><room>002-AB</room>" +
            "</consultant></departments></com:company>";
        CompanyDocument doc = CompanyDocument.Factory.parse(sb);

        CompanyType company = doc.getCompany();

        ConsultantType cons = company.getDepartmentsArray(0).getConsultantArray(0);
        assertEquals(3, cons.xgetRoomArray().length);

        cons.getRoomArray(1);
    }

    @Test
    public void testBadInputGetIthGood() throws XmlException{
        String sb =
            "<com:company xmlns:com=\"http://xbean.usertype/multipleItems/company\">" +
            "<departments><consultant name=\"Joe Smith\" age=\"100\">" +
            "<room>000-AB</room><room>0001-AB</room><room>002-AB</room>" +
            "</consultant></departments></com:company>";
        CompanyDocument doc = CompanyDocument.Factory.parse(sb);

        CompanyType company = doc.getCompany();

        ConsultantType cons = company.getDepartmentsArray(0).getConsultantArray(0);
        assertEquals(3, cons.xgetRoomArray().length);

        assertEquals(0, cons.getRoomArray(0).getDigits());
        assertEquals("AB", cons.getRoomArray(0).getLetters());
        assertEquals(2, cons.getRoomArray(2).getDigits());
        assertEquals("AB", cons.getRoomArray(2).getLetters());
    }
}
