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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XsbReaderAnnotationCountTest {

    // Builds a reader whose next reads come from the given on-wire ints, exactly
    // as a crafted/corrupt .xsb annotation section would supply them. When
    // withVersion is set the version fields are bumped so readAnnotation gets
    // past its atLeast(2, 19, 0) guard and reaches the count reads.
    private static XsbReader reader(boolean withVersion, int... ints) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (int i : ints) {
            dos.writeInt(i); // same on-wire form as XsbReader.writeInt
        }
        dos.flush();

        XsbReader reader = new XsbReader(new SchemaTypeSystemImpl("test"), "h");
        set(reader, "_input", new LongUTFDataInputStream(new ByteArrayInputStream(bos.toByteArray())));
        if (withVersion) {
            set(reader, "_majorver", SchemaTypeSystemImpl.MAJOR_VERSION);
            set(reader, "_minorver", SchemaTypeSystemImpl.MINOR_VERSION);
            set(reader, "_releaseno", SchemaTypeSystemImpl.RELEASE_NUMBER);
        }
        return reader;
    }

    private static void set(XsbReader reader, String name, Object value) throws Exception {
        Field f = XsbReader.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(reader, value);
    }

    @Test
    void readAnnotationsRejectsNegativeCount() throws Exception {
        XsbReader reader = reader(false, -2);
        assertThrows(SchemaTypeLoaderException.class, reader::readAnnotations);
    }

    @Test
    void readAnnotationRejectsNegativeAttributeCount() throws Exception {
        XsbReader reader = reader(true, -2);
        assertThrows(SchemaTypeLoaderException.class, () -> reader.readAnnotation(null));
    }

    @Test
    void readAnnotationRejectsNegativeDocumentationCount() throws Exception {
        XsbReader reader = reader(true, 0, -2);
        assertThrows(SchemaTypeLoaderException.class, () -> reader.readAnnotation(null));
    }

    @Test
    void readAnnotationRejectsNegativeAppinfoCount() throws Exception {
        XsbReader reader = reader(true, 0, 0, -2);
        assertThrows(SchemaTypeLoaderException.class, () -> reader.readAnnotation(null));
    }

    // -1 is the null-annotation sentinel and must still be accepted unchanged.
    @Test
    void readAnnotationKeepsNullSentinel() throws Exception {
        XsbReader reader = reader(true, -1);
        assertNull(reader.readAnnotation(null));
    }
}
