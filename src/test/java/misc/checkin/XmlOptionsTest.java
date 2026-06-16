/*   Copyright 2022, 2023 The Apache Software Foundation
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
package misc.checkin;

import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlOptionsTest {
    @Test
    void testUnsynchronizedFlag() {
        XmlOptions xmlOptions = new XmlOptions();
        assertFalse(xmlOptions.isUnsynchronized());
        xmlOptions.setUnsynchronized();
        assertTrue(xmlOptions.isUnsynchronized());
        xmlOptions.setUnsynchronized(false);
        assertFalse(xmlOptions.isUnsynchronized());
    }

    @Test
    void testLoadStrictFloatingPointFlag() {
        XmlOptions xmlOptions = new XmlOptions();
        assertFalse(xmlOptions.isLoadStrictFloatingPoint());
        xmlOptions.setLoadStrictFloatingPoint();
        assertTrue(xmlOptions.isLoadStrictFloatingPoint());
        xmlOptions.setLoadStrictFloatingPoint(false);
        assertFalse(xmlOptions.isLoadStrictFloatingPoint());
    }

    @Test
    void testSaveNoAttributeWhitespaceEscapeFlag() {
        XmlOptions xmlOptions = new XmlOptions();
        assertFalse(xmlOptions.isSaveNoAttributeWhitespaceEscape());
        xmlOptions.setSaveNoAttributeWhitespaceEscape();
        assertTrue(xmlOptions.isSaveNoAttributeWhitespaceEscape());
        xmlOptions.setSaveNoAttributeWhitespaceEscape(false);
        assertFalse(xmlOptions.isSaveNoAttributeWhitespaceEscape());
    }
}
