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
package xmlcursor.xpath.jaxen.detailed;

import xmlcursor.xpath.common.XPathFunctionTest;
import xmlcursor.common.Common;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import junit.framework.TestCase;

/**
 *
 */
public class XPathNodeTest  extends TestCase
{
   public void testNodeEquality() throws Exception{
       XmlCursor c =XmlObject.Factory.parse("<root>" +
           "<book isbn='012345' id='09876'/></root>")
           .newCursor();
       c.selectPath("//book[isbn='012345'] is //book[id='09876']");
       assertEquals(1, c.getSelectionCount() );
       c.toNextSelection();
       assertEquals(Common.wrapInXmlFrag("true"), c.xmlText());
   }

    public void testNodeOrder() throws Exception{
       XmlCursor c =XmlObject.Factory.parse("<root>" +
           "<book isbn='012345'/><book id='09876'/></root>")
           .newCursor();
       c.selectPath("//book[isbn='012345'] << //book[id='09876']");
       assertEquals(1, c.getSelectionCount() );
       c.toNextSelection();
       assertEquals(Common.wrapInXmlFrag("true"), c.xmlText());

       c.selectPath("//book[isbn='012345'] >> //book[id='09876']");
       assertEquals(1, c.getSelectionCount() );
       c.toNextSelection();
       assertEquals(Common.wrapInXmlFrag("false"), c.xmlText());
   }
}
