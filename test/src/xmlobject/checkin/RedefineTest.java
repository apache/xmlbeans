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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.example.prod.NewSizeDocument;
import org.junit.Test;
import org.openuri.versionstest.ElementDocument;
import org.openuri.versionstest.Type;
import org.openuri.versionstest.TypeX;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RedefineTest {
    @Test
    public void testRedefine() throws XmlException {
        String xml = "<newSize xmlns='http://example.org/prod'>7</newSize>";
        NewSizeDocument nsDoc = NewSizeDocument.Factory.parse(xml);

        boolean valid = nsDoc.validate();

        assertTrue(valid);

        assertEquals(7, nsDoc.getNewSize());

        nsDoc.setNewSize(20);

        List errors = new ArrayList();
        XmlOptions options = new XmlOptions();
        options.setErrorListener(errors);

        valid = nsDoc.validate(options);

        assertFalse(valid);

        assertEquals(1, errors.size());
    }

    @Test
    public void testMultipleRedefine() throws XmlException {
        String xml = "<v:element xmlns:v='http://openuri.org/versionstest'>" +
            "<aa>AA</aa><a>A</a><b>B</b><c>C</c>" + "</v:element>";
        ElementDocument doc = ElementDocument.Factory.parse(xml);
        TypeX tx = doc.getElement();

        assertTrue(tx.validate());
        assertEquals("A", tx.getA());
        assertEquals("B", tx.getB());
        assertEquals("C", tx.getC());
        assertEquals("AA", ((Type) tx).getAa());
    }
}
