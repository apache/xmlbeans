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


package xmlobject.detailed;

import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xmlcursor.common.BasicCursorTestCase.jobj;

public class ValueEqualsTest  {
    @Test
    void testValueEqualsTrue() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) jobj(Common.TRANXML_FILE_CLM);
        XmlObject m_xo = jobj(Common.TRANXML_FILE_CLM);
        assertTrue(clmDoc.valueEquals(m_xo));
    }

    @Disabled // https://issues.apache.org/jira/browse/XMLBEANS-658
    @Test
    void testIssue658() throws Exception {
        CarLocationMessageDocument clm1 = CarLocationMessageDocument.Factory.newInstance();
        CarLocationMessageDocument.CarLocationMessage msg1 = clm1.addNewCarLocationMessage();
        msg1.setFleetID("fleet1");

        CarLocationMessageDocument clm2 = CarLocationMessageDocument.Factory.newInstance();
        CarLocationMessageDocument.CarLocationMessage msg2 = clm2.addNewCarLocationMessage();
        msg2.setFleetID("fleet2");

        assertTrue(clm1.valueEquals(clm1));
        assertTrue(clm2.valueEquals(clm2));
        assertFalse(clm1.valueEquals(clm2));
        assertFalse(clm2.valueEquals(clm1));
    }

}

