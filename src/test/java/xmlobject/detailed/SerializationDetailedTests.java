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


package xmlobject.detailed;

import org.apache.xmlbeans.XmlInteger;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SerializationDetailedTests {

    @Test
    void testDocFragmentSerialization() throws Exception {
        XmlInteger xmlInt = XmlInteger.Factory.newInstance();
        xmlInt.setBigIntegerValue(new BigInteger("10"));

        Node node = xmlInt.getDomNode();
        assertTrue(node instanceof DocumentFragment, "DOM node from XmlObject not instance of DocumentFragment (it is a " + node + ")");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(xmlInt);


        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(in);
        XmlInteger deserialized = (XmlInteger) ois.readObject();

        node = deserialized.getDomNode();
        assertTrue(node instanceof DocumentFragment, "DOM node from deserialized XmlObject not instance of DocumentFragment (it is a " + node + ")");
    }

}
