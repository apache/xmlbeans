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
import org.junit.jupiter.api.Test;
import org.openuri.mytest.CustomerDocument;
import org.openuri.mytest.Person;

import java.util.Calendar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePersonTest {
    @Test
    void test() throws Exception {
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

        assertEquals("EGIQTWYZJ", new String(person.getHex(), UTF_8));
        assertEquals("This string is base64Binary encoded!", new String(person.getBase64(), UTF_8));

        assertEquals("GGIQTWYGG", new String(person.getHexAtt(), UTF_8));
        assertEquals("This string is base64Binary encoded!", new String(person.getBase64Att(), UTF_8));

        person.setFirstname("George");
        assertEquals("George", person.getFirstname());

        person.setHex("hex encoding".getBytes());
        assertEquals("hex encoding", new String(person.getHex(), UTF_8));

        person.setBase64("base64 encoded".getBytes());
        assertEquals("base64 encoded", new String(person.getBase64(), UTF_8));
    }
}
