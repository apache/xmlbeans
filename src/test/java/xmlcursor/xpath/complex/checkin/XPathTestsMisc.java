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

package xmlcursor.xpath.complex.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

public class XPathTestsMisc {

    @Test
    public void testDelete() throws Exception {
        String query = "*";

        try (XmlCursor xc = XmlObject.Factory.parse(XPathTests.XML).newCursor()) {
            xc.selectPath(query);
            while (xc.toNextSelection()) {
                System.out.println(xc.xmlText());
            }
        }
    }

}
