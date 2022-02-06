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

import org.junit.jupiter.api.Test;
import xbean.scomp.attribute.globalAttrId.GlobalAttrIdDocument;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;

public class GlobalAttrIdTest {
    @Test
    void testId() throws Throwable {
        GlobalAttrIdDocument testDoc = GlobalAttrIdDocument.Factory.newInstance();
        GlobalAttrIdDocument.GlobalAttrId elt = testDoc.addNewGlobalAttrId();
        elt.addNewIDElement().setId1("foobar");

        elt.addNewIDRefElement().setIdref1("foobar");

        assertTrue(testDoc.validate(createOptions()));
    }
}
