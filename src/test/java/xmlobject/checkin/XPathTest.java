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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XPathTest {
    @Test
    public void testPath() throws XmlException {
        final XmlObject obj = XmlObject.Factory.parse(
            "<a>" +
                "<b>" +
                "<c>val1</c>" +
                "<d><c>val2</c></d>" +
                "</b>" +
                "<c>val3</c>" +
                "</a>");
        try (XmlCursor c = obj.newCursor()) {
            c.selectPath(".//b/c");

            int selCount = c.getSelectionCount();
            assertEquals("SelectionCount", 1, selCount);

            while (c.hasNextSelection()) {
                c.toNextSelection();

                assertTrue("OnStartElement", c.isStart());
                assertEquals("TextValue", "val1", c.getTextValue());
                System.out.println(" -> " + c.getObject());
            }
        }
    }

    @Test
    public void testPath2() throws XmlException {
        final XmlObject obj = XmlObject.Factory.parse(
            "<a>" +
                "<b>" +
                "<c>val1</c>" +
                "<d>" +
                "<c>val2</c>" +
                "<b><c>val3</c></b>" +
                "</d>" +
                "</b>" +
                "<c>val4</c>" +
                "</a>");
        try (XmlCursor c = obj.newCursor()) {
            c.selectPath(".//b/c");

            int selCount = c.getSelectionCount();
            assertEquals("SelectionCount", 2, selCount);

            assertTrue("hasNextSelection", c.hasNextSelection());
            c.toNextSelection();

            System.out.println(" -> " + c.getObject());
            assertTrue("OnStartElement", c.isStart());
            assertEquals("TextValue", "val1", c.getTextValue());


            assertTrue("hasNextSelection2", c.hasNextSelection());
            c.toNextSelection();

            System.out.println(" -> " + c.getObject());
            assertTrue("OnStartElement2", c.isStart());
            assertEquals("TextValue2", "val3", c.getTextValue());
        }
    }

    @Test
    public void testPath3() throws XmlException {
        final XmlObject obj = XmlObject.Factory.parse(
            "<a>" +
                "<b>" +
                "<c>val1</c>" +
                "<d>" +
                "<c>val2</c>" +
                "<b>" +
                "<c>val3" +
                "<c>val5</c>" +
                "</c>" +
                "</b>" +
                "</d>" +
                "</b>" +
                "<c>val4</c>" +
                "</a>");

        try (XmlCursor c = obj.newCursor()) {
            c.selectPath(".//b/c//c");

            int selCount = c.getSelectionCount();
            assertEquals("SelectionCount", 1, selCount);

            while (c.hasNextSelection()) {
                c.toNextSelection();

                System.out.println(" -> " + c.getObject());
                assertTrue("OnStartElement", c.isStart());
                assertEquals("TextValue", "val5", c.getTextValue());
            }
        }
    }
}
