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
package xmlobject.checkin;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class SaverTest {

    @Disabled("https://issues.apache.org/jira/browse/XMLBEANS-604")
    @Test
    void testLengthAssertion() throws IOException, XmlException {
        final String lineSeparator = System.getProperty("line.separator");
        System.setProperty("line.separator", "\n");
        try {
            String xml = "<test>" + new String(new char[16339]).replace('\0', 'x') + "</test>";
            XmlObject object = XmlObject.Factory.parse(xml);
            try (InputStream is = object.newInputStream()) {
                byte[] readBytes = IOUtils.toByteArray(is);
            }
        } finally {
            System.setProperty("line.separator", lineSeparator);
        }
    }
}
