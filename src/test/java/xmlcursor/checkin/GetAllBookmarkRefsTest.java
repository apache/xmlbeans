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
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xmlcursor.common.BasicCursorTestCase.cur;

public class GetAllBookmarkRefsTest {
    private static final Bookmark0 _theBookmark0 = new Bookmark0("value0");
    private static final Bookmark1 _theBookmark1 = new Bookmark1("value1");
    private static final Bookmark2 _theBookmark2 = new Bookmark2("value2");

    @Test
    void testGetAll() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            m_xc.setBookmark(_theBookmark0);
            m_xc.setBookmark(_theBookmark1);
            m_xc.setBookmark(_theBookmark2);

            List<Object> v = new ArrayList<>();
            m_xc.getAllBookmarkRefs(v);
            assertEquals(3, v.size());
            assertEquals("value0", ((Bookmark0) v.get(0)).text);
            assertEquals("value1", ((Bookmark1) v.get(1)).text);
            assertEquals("value2", ((Bookmark2) v.get(2)).text);
        }
    }

    @Test
    void testGetAllNullListToFill() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            m_xc.setBookmark(_theBookmark0);
            m_xc.setBookmark(_theBookmark1);
            m_xc.setBookmark(_theBookmark2);
            m_xc.getAllBookmarkRefs(null);
            assertTrue(true);
        }
    }

    private static class Bookmark0 extends XmlCursor.XmlBookmark {
        public String text;

        Bookmark0(String text) {
            this.text = text;
        }
    }

    private static class Bookmark1 extends XmlCursor.XmlBookmark {
        public String text;

        Bookmark1(String text) {
            this.text = text;
        }
    }

    private static class Bookmark2 extends XmlCursor.XmlBookmark {
        public String text;

        Bookmark2(String text) {
            this.text = text;
        }
    }
}

