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
package xmlobject.extensions.prePostFeature.ValueRestriction.checkin;

import org.apache.xmlbeans.XmlString;
import org.junit.jupiter.api.Test;
import prePostFeature.xbean.valueRestriction.company.CompanyDocument;
import prePostFeature.xbean.valueRestriction.company.CompanyType;
import prePostFeature.xbean.valueRestriction.company.ConsultantType;
import prePostFeature.xbean.valueRestriction.company.DepartmentType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ValueRestrictionTest {

    @Test
    void test() throws Exception {

        CompanyDocument poDoc;

        poDoc = CompanyDocument.Factory.newInstance();
        CompanyType po = poDoc.addNewCompany();


        int LEN = 20;

        StringBuilder sExpected = new StringBuilder();
        sExpected.append("<com:company xmlns:com=\"http://xbean.prePostFeature/ValueRestriction/company\"><departments>");
        DepartmentType dept = po.addNewDepartments();
        ConsultantType[] it = new ConsultantType[LEN];
        for (int i = 0; i < LEN; i++) {
            it[i] = dept.addNewConsultant();
            XmlString s = XmlString.Factory.newInstance();
            it[i].setAge(50);
            sExpected.append("<consultant age=\"50\"/>");
        }

        sExpected.append("</departments></com:company>");

        assertEquals(sExpected.toString(), poDoc.xmlText());
        assertTrue(poDoc.validate());


        xmlobject.extensions.prePostFeature.ValueRestriction.existing.SetterHandler.bReady = true;


        for (int i = 0; i < LEN; i++) {
            it[i].setAge(150);
        }

        assertEquals(sExpected.toString(), poDoc.xmlText());
        assertTrue(poDoc.validate());

        for (int i = 0; i < LEN; i++) {
            it[i].setEmployeeAge(150);
        }
        assertEquals(sExpected.toString(), poDoc.xmlText());
        assertTrue(poDoc.validate());
    }

}

