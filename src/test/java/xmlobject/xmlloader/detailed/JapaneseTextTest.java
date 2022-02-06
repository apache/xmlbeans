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
package xmlobject.xmlloader.detailed;

import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.util.JarUtil;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JapaneseTextTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "pr-xml-euc-jp.xml",
        "pr-xml-iso-2022-jp.xml",
        "pr-xml-little-endian.xml",
        "pr-xml-shift_jis.xml",
        "pr-xml-utf-8.xml",
        "pr-xml-utf-16.xml",
        "weekly-euc-jp.xml",
        "weekly-iso-2022-jp.xml",
        "weekly-little-endian.xml",
        "weekly-shift_jis.xml",
        "weekly-utf-8.xml",
        "weekly-utf-16.xml",
        "prefix_test.xml"
    })
    void testEncoding(String encodedFile) throws Exception {
        try (InputStream is = JarUtil.getResourceFromJarasStream("xbean/xmlobject/japanese/" + encodedFile)) {
            XmlObject obj = XmlObject.Factory.parse(is);
            assertNotNull(obj);
        }
    }
}
