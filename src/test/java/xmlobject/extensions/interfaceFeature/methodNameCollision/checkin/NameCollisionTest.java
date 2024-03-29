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
package xmlobject.extensions.interfaceFeature.methodNameCollision.checkin;

import interfaceFeature.xbean.methodNameCollision.company.CompanyDocument;
import interfaceFeature.xbean.methodNameCollision.company.CompanyType;
import interfaceFeature.xbean.methodNameCollision.company.ConsultantType;
import interfaceFeature.xbean.methodNameCollision.company.DepartmentType;
import org.apache.xmlbeans.XmlString;
import org.junit.jupiter.api.Test;
import xmlobject.extensions.interfaceFeature.methodNameCollision.existing.IFoo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NameCollisionTest {

    @Test
    void test() {
        CompanyDocument poDoc;

        poDoc = CompanyDocument.Factory.newInstance();
        CompanyType po = poDoc.addNewCompany();


        int LEN = 20;

        StringBuilder sExpected = new StringBuilder();
        sExpected.append("<com:company xmlns:com=" +
                "\"interfaceFeature/xbean/methodNameCollision/company\">" +
                "<departments>");
        DepartmentType dept = po.addNewDepartments();
        ConsultantType[] it = new ConsultantType[LEN];
        for (int i = 0; i < LEN; i++) {
            it[i] = dept.addNewConsultant();
            XmlString s = XmlString.Factory.newInstance();
            it[i].setAge(50);
            it[i].setName4("BEAN Name" + i);
            sExpected.append("<consultant age=\"50\" name=\"BEAN Name" + i + "\"/>");
        }

        sExpected.append("</departments></com:company>");

        int[][] ints = new int[2][3];
        for (int i = 0; i < ints.length; i++)
            for (int j = 0; j < ints[i].length; j++)
                ints[i][j] = (i + 1) * (j + 1);

        IFoo.Inner inner = new IFoo.Inner() {
            public String getValue() {
                return "inner value";
            }
        };

        assertEquals(sExpected.toString(), poDoc.xmlText());
        assertTrue(poDoc.validate());
        assertEquals("Name0", it[0].getName());
        assertEquals("Name2: [1, 2, 3, ], [2, 4, 6, ], ", it[0].getName2(ints));
        assertEquals("Name3: inner value", it[0].getName3(inner));
        assertEquals("BEAN Name0", it[0].getName4());


    }

}
