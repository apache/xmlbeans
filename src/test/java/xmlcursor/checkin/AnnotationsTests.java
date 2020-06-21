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

package xmlcursor.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.Common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AnnotationsTests {
    private static class TestBookmark extends XmlCursor.XmlBookmark {
    }

    //
    // Basic load up a file and iterate through it
    //
    @Test
    public void testBasicXml() throws Exception {
        XmlCursor c = XmlObject.Factory.parse(Common.XML_ATTR_TEXT, null).newCursor();

        TestBookmark a1 = new TestBookmark();

        c.setBookmark(a1);

        TestBookmark a2 = new TestBookmark();

        c.toNextToken();
        c.toNextToken();

        c.setBookmark(a2);

        c.toPrevToken();
        c.toPrevToken();

        assertEquals(c.getBookmark(TestBookmark.class), a1);

        c.toNextToken();
        c.toNextToken();

        assertEquals(c.getBookmark(TestBookmark.class), a2);

        c.toNextToken();

        assertNull(c.getBookmark(TestBookmark.class));
    }
}
