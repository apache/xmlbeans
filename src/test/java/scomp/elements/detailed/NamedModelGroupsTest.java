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
package scomp.elements.detailed;

import org.junit.jupiter.api.Test;
import xbean.scomp.element.namedModelGroup.EmployeePerf;
import xbean.scomp.element.namedModelGroup.EmployeePerformanceDocument;
import xbean.scomp.element.namedModelGroup.ManagerDocument;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;

public class NamedModelGroupsTest {
    @Test
    void testValid() {
        EmployeePerformanceDocument doc = EmployeePerformanceDocument.Factory.newInstance();
        EmployeePerf elt = doc.addNewEmployeePerformance();
        ManagerDocument.Manager m = elt.addNewManager();
        m.setDepartment("Marketing");
        m.setLastName("Smith");

        elt.setComment("Horrible performance by employee Potatohead");
        elt.setDate(new GregorianCalendar(2004, Calendar.SEPTEMBER, 12));
        elt.setGrade(new BigDecimal(new BigInteger("10")));
        elt.setManager(m);
        assertTrue(doc.validate(createOptions()));
    }
}
