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

package xmlobject.xmlloader.detailed;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class XmlLoaderBvtTest extends BasicCursorTestCase {
    @Test
    public void testCastDocument() throws Exception {

        CarLocationMessageDocument clm =
            (CarLocationMessageDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));

        assertNotNull(clm);
    }
}
