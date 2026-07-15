/*   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaTypeLoaderException;
import org.apache.xmlbeans.impl.util.LongUTFDataInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class XsbReaderGroupQNameTest {

    // Builds an XsbReader whose first readQName() returns null, exactly as a
    // crafted .xsb would supply it: writeQName encodes a null name as two null
    // string codes (code 0), and stringForCode(0) returns null so readQName
    // hands back null for a zero local-part code.
    private static XsbReader readerForNullQName() throws Exception {
        XsbReader reader = new XsbReader(new SchemaTypeSystemImpl("test"), "h");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(0); // namespace string code -> null
        dos.writeShort(0); // localname string code -> null -> readQName returns null
        dos.flush();

        Field inputF = XsbReader.class.getDeclaredField("_input");
        inputF.setAccessible(true);
        inputF.set(reader, new LongUTFDataInputStream(new ByteArrayInputStream(bos.toByteArray())));

        return reader;
    }

    @Test
    void modelGroupNullQNameStaysWithinLoaderException() throws Exception {
        // name.getNamespaceURI() used to run before the try, so a null name
        // escaped as a raw NullPointerException instead of SchemaTypeLoaderException
        XsbReader reader = readerForNullQName();
        assertThrows(SchemaTypeLoaderException.class, reader::finishLoadingModelGroup);
    }

    @Test
    void attributeGroupNullQNameStaysWithinLoaderException() throws Exception {
        XsbReader reader = readerForNullQName();
        assertThrows(SchemaTypeLoaderException.class, reader::finishLoadingAttributeGroup);
    }
}
