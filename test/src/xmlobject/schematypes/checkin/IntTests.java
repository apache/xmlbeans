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


package xmlobject.schematypes.checkin;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import xint.test.PositionDocument;

import static org.junit.Assert.assertEquals;

public class IntTests {
    @Test
    public void testLatLong() throws XmlException {
        PositionDocument doc = PositionDocument.Factory.parse(
            "<p:position xmlns:p='java:int.test'><p:lat>43</p:lat><p:lon>020</p:lon></p:position>");
        assertEquals(43, doc.getPosition().getLat());
        assertEquals(20, doc.getPosition().getLon());
        doc.getPosition().xgetLat().setStringValue("07");
        doc.getPosition().xgetLon().setStringValue("040");
        assertEquals(7, doc.getPosition().getLat());
        assertEquals(40, doc.getPosition().getLon());
        doc.getPosition().setLat((short) 22);
    }
}
