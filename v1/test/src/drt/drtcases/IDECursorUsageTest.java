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

package drtcases;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;

import java.io.File;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests a simple usage of the cursor I'm having trouble with in IDE code.
 */
public class IDECursorUsageTest extends TestCase
{
    public IDECursorUsageTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(IDECursorUsageTest.class); }

    public static File getCaseFile(String theCase)
    {
        return TestEnv.xbeanCase("store/" + theCase);
    }

    static XmlCursor loadCase(String theCase) throws Exception
    {
        return XmlObject.Factory.parse(getCaseFile(theCase)).newCursor();
    }

    public void testScanElement() throws Exception
    {
        XmlCursor cursor = loadCase("ConsolidateTest.xml");
        cursor.toFirstChild();

        XmlCursor cur = cursor.newCursor();

        // back up to previous start or end tag
        while (!isStartOrEnd(cur.toPrevToken())) { System.out.println("Backing up: at " + cur.currentTokenType()); }
        // then forward to first start or comment (may end up where we started)
        while (!isStartOrComment(cur.toNextToken())) { System.out.println("Advancing: at " + cur.currentTokenType()); }
        // then grab the "start" javelin annotation, and grab the start token and the first char
        // _firstToken = ((JavelinAnnotation)cur.getAnnotation(JavelinAnnotation.class)).getStartToken();

        // now peek at the end tag (it may be unplaced for the <a/> case, so use the start tag if needed
        cur.toCursor(cursor);
        // _lastToken = ((JavelinAnnotation)cur.getAnnotation(JavelinAnnotation.class)).getEndToken();
        cur.toEndToken();
        // JavelinAnnotation endAnn = (JavelinAnnotation)cur.getAnnotation(JavelinAnnotation.class);
        // if (endAnn != null)
        //    _lastToken = endAnn.getEndToken();
    }

    boolean isStartOrEnd(XmlCursor.TokenType tokType)
    {
        switch (tokType.intValue())
        {
            case XmlCursor.TokenType.INT_END:
            case XmlCursor.TokenType.INT_ENDDOC:
            case XmlCursor.TokenType.INT_START:
            case XmlCursor.TokenType.INT_STARTDOC:
                return true;
            default:
                return false;
        }
    }

    boolean isStartOrComment(XmlCursor.TokenType tokType)
    {
        switch (tokType.intValue())
        {
            case XmlCursor.TokenType.INT_START:
            case XmlCursor.TokenType.INT_STARTDOC:
            case XmlCursor.TokenType.INT_COMMENT:
                return true;
            default:
                return false;
        }
    }

}
