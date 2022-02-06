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
package scomp.attributes.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.namespace.attributeFormDefault.AttributeUnqualifiedDocument;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class LocalAttrForm {

    /**
     * attrFormDefault is "qualified", overwrite locally to "unqualified"
     */
    @Test
    void testRun() throws Throwable{

        String input="<ns:AttributeUnqualified " +
                "xmlns:ns=\"http://xbean/scomp/namespace/AttributeFormDefault\"" +
                " LocalAttribute=\"foobar\"/>";

        AttributeUnqualifiedDocument doc= AttributeUnqualifiedDocument.Factory.parse(input);

        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        try (XmlCursor c = doc.getAttributeUnqualified().getLocalAttribute().newCursor()) {
            c.setName(new QName(
                "http://xbean/scomp/namespace/AttributeFormDefault",
                "LocalAttribute"));
        }
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
