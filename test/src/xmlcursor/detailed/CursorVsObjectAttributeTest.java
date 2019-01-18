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


package  xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CarLocationMessageDocument.CarLocationMessage;
import tools.util.JarUtil;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;


public class CursorVsObjectAttributeTest {
    @Test
    public void testAttributeSet() throws Exception {
        CarLocationMessageDocument clmDoc = CarLocationMessageDocument.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlCursor xc = clmDoc.newCursor();
        xc.toFirstChild();
        CarLocationMessage clm = (CarLocationMessage) xc.getObject();

        clm.setVersion("XyZ");
        QName name = new QName("Version");
        assertEquals("XyZ", xc.getAttributeText(name));
        xc.setAttributeText(name, "012");
        assertEquals("012", clm.getVersion());
    }

    @Test
    public void testAttributeUnsetRemove() throws Exception {
        CarLocationMessageDocument clmDoc =
                (CarLocationMessageDocument) XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlCursor xc = clmDoc.newCursor();
        xc.toFirstChild();
        CarLocationMessage clm = (CarLocationMessage) xc.getObject();
        QName name = new QName("Version");
        assertEquals("CLM", xc.getAttributeText(name));
        clm.unsetVersion();
        assertNull(xc.getAttributeText(name));
        xc.setAttributeText(name, "012");
        assertEquals("012", clm.getVersion());
        xc.removeAttribute(name);
        assertNull(clm.getVersion());
    }

    @Test
    public void testAttributeInsert() throws Exception {
        XmlOptions map = new XmlOptions();
        map.put(XmlOptions.LOAD_STRIP_WHITESPACE, "");

        CarLocationMessageDocument clmDoc =
                (CarLocationMessageDocument) XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM), map);
        XmlCursor xc = clmDoc.newCursor();
        xc.toFirstChild();
        CarLocationMessage clm = (CarLocationMessage) xc.getObject();
        QName name = new QName("Version");
        assertEquals("CLM", xc.getAttributeText(name));
        clm.unsetVersion();
        assertNull(xc.getAttributeText(name));
        xc.toFirstChild();
        assertEquals(TokenType.START, xc.currentTokenType());
        xc.insertAttributeWithValue(name, "012");
        assertEquals("012", clm.getVersion());
    }
}
