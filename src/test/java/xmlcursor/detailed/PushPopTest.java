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


package xmlcursor.detailed;


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xmlcursor.common.BasicCursorTestCase.cur;

public class PushPopTest {

    private static  final String XML =
        "<foo xmlns:edi='http://ecommerce.org/schema'>" +
        "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?>" +
        "<!-- the 'price' element's namespace is http://ecommerce.org/schema -->  " +
        "<edi:price units='Euro' date='12-12-03'>32.18</edi:price> </foo>";

    @Test
    void testPopEmpty() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            assertFalse(m_xc.pop());
        }
    }

    @Test
    void testPushNTimes() throws XmlException {
        final int nCount = 100;
        try (XmlCursor m_xc = cur(XML)) {
            for (int i = 0; i < nCount; i++) {
                m_xc.push();
            }
            for (int i = 0; i < nCount; i++) {
                assertTrue(m_xc.pop());
            }
            assertFalse(m_xc.pop());
        }
    }
}
