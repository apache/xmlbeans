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

import org.junit.jupiter.api.Test;
import org.openuri.nameworld.Loc;
import org.openuri.nameworld.NameworldDocument;

import javax.xml.namespace.QName;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.jobj;


public class NameworldTest {
    @Test
    void testWorld1() throws Exception {
        NameworldDocument doc = (NameworldDocument) jobj("xbean/xmlobject/nameworld.xml");
        QName expected = new QName("http://openuri.org/nameworld", "nameworld");
        assertEquals(expected, doc.schemaType().getDocumentElementName());

        QName[] exp = {
            new QName("http://bar.com/", "barcity"),
            new QName("http://foo.com/", "footown"),
            new QName("http://bar.com/", "barvillage"),
            new QName("http://bar.com/", "bartown"),
            new QName("http://foo.com/", "foovillage"),
            new QName("http://bar.com/", "barvillage"),
            new QName("http://foo.com/", "foocity"),
            new QName("http://bar.com/", "bartown"),
            new QName("http://foo.com/", "foovillage"),
            new QName("http://foo.com/", "footown"),
            new QName("http://bar.com/", "barvillage"),
            new QName("http://foo.com/", "foovillage"),
        };

        QName[] act = Stream.of(doc.getNameworld().getIslandArray())
            .flatMap(island -> Stream.of(island.getLocationArray()))
            .flatMap(loc -> Stream.of(loc.getReferenceArray()))
            .map(Loc.Reference::getTo)
            .toArray(QName[]::new);

        assertArrayEquals(exp, act);
    }
}
