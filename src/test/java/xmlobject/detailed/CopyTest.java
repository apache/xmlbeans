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

import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CopyTest {
    // Test for a Document object being copied as DocFrag if the type of the
    // source of the copy is not a document type (as is the case with NO_TYPE).
    @Test
    void testXobjTypeOnDomNodeCopy() throws Exception {
        XmlObject o = XmlObject.Factory.parse("<foo><a/></foo>");
        String xobjOrgClassName = "org.apache.xmlbeans.impl.store.DocumentXobj";
        assertEquals(o.getDomNode().getClass().getName(), xobjOrgClassName, "Invalid Type!");

        XmlObject o2 = o.copy();
        String xobjCopyClassName = o2.getDomNode().getClass().getName();
        System.out.println("DocXobj:" + xobjCopyClassName);

        // check for the expected type
        assertEquals("org.apache.xmlbeans.impl.store.DocumentXobj", xobjOrgClassName, "Invalid Type!");
        assertEquals("org.apache.xmlbeans.impl.store.DocumentXobj", xobjCopyClassName, "Invalid Type!");
    }

    // Test the same for a simple untyped XmlObject copy
    @Test
    void testXobjTypeOnCopy() throws Exception {
        String untypedXobjClass = "org.apache.xmlbeans.impl.values.XmlAnyTypeImpl";

        XmlObject o = XmlObject.Factory.parse("<foo><a/></foo>");
        assertEquals(untypedXobjClass, o.getClass().getName(), "Invalid Type!");

        XmlObject o2 = o.copy();
        // type should be unchanged after the copy
        assertEquals(untypedXobjClass, o2.getClass().getName(), "Invalid Type!");
    }
}
