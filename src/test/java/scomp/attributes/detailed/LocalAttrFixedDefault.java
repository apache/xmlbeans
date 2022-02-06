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

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.attribute.localAttrFixedDefault.LocalAttrFixedDefaultDocument;
import xbean.scomp.attribute.localAttrFixedDefault.LocalAttrFixedDefaultT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class LocalAttrFixedDefault {
    /**
     * Verify that a local attribute can add a fixed value but can not overwrite
     * an existing fixed val
     * inspired by Walmsley 13.6.2
     */
    //ensure default val is shadowed locally
    //fixed can not be...
    @Test
    void testDefault() throws Throwable {
        LocalAttrFixedDefaultT testDoc = LocalAttrFixedDefaultDocument.Factory.newInstance().addNewLocalAttrFixedDefault();
        assertTrue(testDoc.validate());
        assertEquals(2, testDoc.getAttDefault().intValue());
        //second fixed value is ignored
        testDoc.setAttFixed("NEWXBeanAttrStr");
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));


        testDoc.setAttFixed("XBeanAttrStr");
        assertTrue(testDoc.validate(validateOptions));
    }
}
