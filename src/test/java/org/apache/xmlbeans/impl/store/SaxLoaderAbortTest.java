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

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.SAXHelper;
import org.junit.jupiter.api.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A caller-supplied XMLReader outlives the parse and holds the SaxLoader as its
 * content handler, so anything the loader still points at stays reachable. A load that
 * completes clears that in postLoad(); one that fails has to do the same.
 */
public class SaxLoaderAbortTest {

    private static Object fieldValue(Object target, String name) throws Exception {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ignored) {
                // keep walking up
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void assertReaderReleasedItsDocument(String xml, boolean expectFailure)
        throws Exception {
        XMLReader xr = SAXHelper.newXMLReader(new XmlOptions());
        XmlOptions options = new XmlOptions().setLoadUseXMLReader(xr);

        if (expectFailure) {
            assertThrows(XmlException.class, () -> XmlObject.Factory.parse(xml, options));
        } else {
            assertNotNull(XmlObject.Factory.parse(xml, options));
        }

        ContentHandler handler = xr.getContentHandler();
        assertNotNull(handler, "the reader should still hold the loader");

        assertNull(fieldValue(handler, "_locale"),
            "the loader still points at the Locale, keeping the document and its type loader alive");
        assertNull(fieldValue(handler, "_context"),
            "the loader still points at the load context");
    }

    @Test
    void releasesTheDocumentAfterAFailedParse() throws Exception {
        assertReaderReleasedItsDocument("<root>", true);
    }

    @Test
    void releasesTheDocumentAfterAParseErrorPartWayIn() throws Exception {
        assertReaderReleasedItsDocument("<root><a/><b></root>", true);
    }

    @Test
    void releasesTheDocumentAfterASuccessfulParse() throws Exception {
        assertReaderReleasedItsDocument("<root><a/></root>", false);
    }
}
