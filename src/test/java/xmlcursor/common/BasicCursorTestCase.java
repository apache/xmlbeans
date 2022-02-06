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



package xmlcursor.common;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import tools.util.JarUtil;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class BasicCursorTestCase {

    public static void toNextTokenOfType(XmlCursor xc, TokenType tt) throws IllegalArgumentException {
        if (xc == null) {
            throw new IllegalArgumentException("Invalid argument: null XmlCursor");
        } else if (tt == null) {
            throw new IllegalArgumentException("Invalid argument: null TokenType");
        }

        while (xc.toNextToken() != tt) {
            if (xc.currentTokenType() == TokenType.ENDDOC)
                fail("Expected Token not found! " + tt.toString());
        }
        assertEquals(tt, xc.currentTokenType());
    }

    public static XmlCursor toNextTokenOfTypeCursor(XmlCursor xc, TokenType tt) throws IllegalArgumentException {
        toNextTokenOfType(xc, tt);
        return xc.newCursor();
    }

    public static void toPrevTokenOfType(XmlCursor xc, TokenType tt)
            throws IllegalArgumentException {
        if (xc == null) {
            throw new IllegalArgumentException("Invalid argument: null XmlCursor");
        } else if (tt == null) {
            throw new IllegalArgumentException("Invalid argument: null TokenType");
        }

        while (xc.toPrevToken() != tt) {
            if (xc.currentTokenType() == TokenType.STARTDOC)
                fail("Expected Token not found! " + tt.toString());
        }
        assertEquals(tt, xc.currentTokenType());
    }

    /**
     * Method compareDocTokens
     *
     * TODO: should really compare values also.
     *
     * @param    a                   a  XmlCursor
     * @param    b                   a  XmlCursor
     *
     *
     */
    public static void compareDocTokens(XmlCursor a, XmlCursor b) {
        while (a.hasNextToken() && b.hasNextToken()) {
            TokenType ttOrig = a.currentTokenType();
            TokenType ttRoundTrip = b.currentTokenType();
            if (ttOrig != ttRoundTrip) {
                fail("Round trip failed.  Original Cursor TokenType = "
                     + ttOrig + " Roundtrip Cursor TokenType = "
                     + ttRoundTrip);

            }
            a.toNextToken();
            b.toNextToken();
        }
    }

    public static XmlCursor jcur(String xml) throws XmlException, IOException {
        return jobj(xml).newCursor();
    }

    public static XmlObject jobj(String xml) throws XmlException, IOException {
        return obj(JarUtil.getResourceFromJar(xml));
    }

    public static XmlObject jobj(String xml, XmlOptions map) throws XmlException, IOException {
        return XmlObject.Factory.parse(JarUtil.getResourceFromJar(xml), map);
    }

    public static XmlCursor cur(String xml) throws XmlException {
        return obj(xml).newCursor();
    }

    public static XmlObject obj(String xml) throws XmlException {
        return XmlObject.Factory.parse(xml);
    }
}

