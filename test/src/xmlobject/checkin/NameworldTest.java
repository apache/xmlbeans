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

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.openuri.nameworld.Loc;
import org.openuri.nameworld.NameworldDocument;
import org.openuri.nameworld.NameworldDocument.Nameworld;
import tools.util.JarUtil;

import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;


public class NameworldTest {
    @Test
    public void testWorld1() throws Exception {
        NameworldDocument doc = (NameworldDocument)
            XmlObject.Factory.parse(
                JarUtil.getResourceFromJarasFile(
                    "xbean/xmlobject/nameworld.xml"));

        assertEquals(new QName("http://openuri.org/nameworld", "nameworld"), doc.schemaType().getDocumentElementName());

        QName[] contents = {
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
        int t = 0;

        Nameworld world = doc.getNameworld();
        Nameworld.Island[] islands = world.getIslandArray();
        for (int i = 0; i < islands.length; i++) {
            Loc[] locs = islands[i].getLocationArray();
            for (int j = 0; j < locs.length; j++) {
                Loc.Reference[] refs = locs[j].getReferenceArray();
                for (int k = 0; k < refs.length; k++) {
                    assertEquals(contents[t++], refs[k].getTo());
                }
            }
        }
    }
}
