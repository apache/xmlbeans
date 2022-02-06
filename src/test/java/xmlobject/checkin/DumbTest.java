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

package xmlobject.checkin;

import dumbNS.RootDocument;
import dumbNS.RootDocument.Root;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.jobj;


public class DumbTest {
    @Test
    void testGetB2() throws Exception {
        RootDocument rootDoc = (RootDocument) jobj("xbean/simple/dumb/dumb.xml");
        Root root = rootDoc.getRoot();
        assertEquals(4, root.getB2().intValue(), "bar:b attribute != 4");
    }
}
