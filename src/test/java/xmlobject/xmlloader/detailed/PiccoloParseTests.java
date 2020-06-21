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
import org.junit.Ignore;
import org.junit.Test;
import tools.util.JarUtil;

import java.io.InputStream;

import static org.junit.Assert.assertTrue;

@Ignore("Piccolo is not anymore used")
public class PiccoloParseTests {
    String filename="xbean/xmlobject/japanese/core_generated_wsdl_src.xml";
    String temp="xbean/xmlobject/japanese/UCS2Encoding.xml";

    @Test
    public void testParseInputStream() throws Exception{
        InputStream is=JarUtil.getResourceFromJarasStream(filename);
        assertTrue (is != null );
        XmlObject obj=XmlObject.Factory.parse(is);
    }

    @Test
    public void testParseString() throws Exception{
        String str=JarUtil.getResourceFromJar(filename);
        assertTrue (str != null );
        XmlObject obj=XmlObject.Factory.parse(str);
    }

    @Test
    public void testParseInputStreamUCS2() throws Exception {
        InputStream is = JarUtil.getResourceFromJarasStream(temp);
        assertTrue(is != null);
        XmlObject obj = XmlObject.Factory.parse(is);
    }
}
