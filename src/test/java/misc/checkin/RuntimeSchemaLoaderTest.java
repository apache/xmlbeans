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

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.junit.jupiter.api.Test;
import tools.util.JarUtil;

import javax.xml.namespace.QName;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RuntimeSchemaLoaderTest {

    @Test
    void testDynamicLoad() throws Throwable {
        File inputfile1 = JarUtil.getResourceFromJarasFile("xbean/misc/dyntest.xsd");
        SchemaTypeLoader loader = XmlBeans.loadXsd(XmlObject.Factory.parse(inputfile1));
        XmlObject result = loader.parse(
                JarUtil.getResourceFromJarasFile("xbean/misc/dyntest.xml"),
                null, null);
        assertEquals("D=wrappedinstance@http://openuri.org/test/dyntest", result.schemaType().toString());
        assertEquals(loader.findDocumentType(
                new QName("http://openuri.org/test/dyntest",
                        "wrappedinstance")), result.schemaType());
    }

    @Test
    void testDynamicLoad2() throws Throwable {
        File inputfile1 = JarUtil.getResourceFromJarasFile("xbean/misc/dyntest2.xsd");
        SchemaTypeLoader loader = XmlBeans.loadXsd(XmlObject.Factory.parse(inputfile1));
        XmlObject result = loader.parse(
                JarUtil.getResourceFromJarasFile("xbean/misc/dyntest2.xml"),
                null, null);
        assertEquals("D=wrappedwildcard@http://openuri.org/test/dyntest", result.schemaType().toString());
        assertEquals(loader.findDocumentType(
                new QName("http://openuri.org/test/dyntest",
                        "wrappedwildcard")), result.schemaType());
        try (XmlCursor cur = result.newCursor()) {
            assertTrue(cur.toFirstChild(), "Should have a root element");
            result = cur.getObject();
            assertEquals("E=wrappedwildcard|D=wrappedwildcard@http://openuri.org/test/dyntest", result.schemaType().toString());
            assertEquals(loader.findElement(
                    new QName("http://openuri.org/test/dyntest",
                            "wrappedwildcard"))
            .getType(), result.schemaType());
            assertTrue(cur.toFirstChild(), "Should have a first child");
            assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "schema"), cur.getName());
            XmlObject obj = cur.getObject();
            assertEquals(Schema.type, obj.schemaType());
        }
    }
}
