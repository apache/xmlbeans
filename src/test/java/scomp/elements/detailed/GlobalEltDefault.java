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

package scomp.elements.detailed;

import org.junit.jupiter.api.Test;
import xbean.scomp.element.globalEltDefault.GlobalEltDefaultIntDocument;
import xbean.scomp.element.globalEltDefault.GlobalEltDefaultStrDocument;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;

public class GlobalEltDefault {
    //empty string is OK considered , so default value is ignored
    @Test
    void testStringType() throws Throwable {
        GlobalEltDefaultStrDocument testDoc = GlobalEltDefaultStrDocument.Factory.newInstance();
        assertNull(testDoc.getGlobalEltDefaultStr());
        testDoc.setGlobalEltDefaultStr("foo");
        assertTrue(testDoc.validate(createOptions()));
    }

    //default value is used
    @Test
    void testIntType() throws Throwable {
        GlobalEltDefaultIntDocument testDoc = GlobalEltDefaultIntDocument.Factory.newInstance();
        assertEquals(0, testDoc.getGlobalEltDefaultInt());
        testDoc.setGlobalEltDefaultInt(5);
        assertTrue(testDoc.validate(createOptions()));
    }
}
