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

package scomp.contentType.complex.detailed;

import xbean.scomp.contentType.anonymous.AnonymousEltDocument.*;
import xbean.scomp.contentType.anonymous.AnonymousMixedEltDocument.*;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlCursor;
import scomp.common.BaseCase;

/**
 * @owner: ykadiysk
 * Date: Jul 16, 2004
 * Time: 3:28:03 PM
 */

/**
 * testing anonymous complex types
 */
public class AnonymousTest extends BaseCase {


    public void testSimpleAnonymous() throws Throwable {
        AnonymousElt testElt = AnonymousElt.Factory.newInstance();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        XmlInteger ival=XmlInteger.Factory.newInstance();
        testElt.xsetChild1(ival);
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        assertEquals("<xml-fragment><child1/><child2>5</child2>" +
                "<child3>1</child3></xml-fragment>",
                testElt.xmlText());

        testElt.xsetChild2(
                XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        ival.setBigIntegerValue(new BigInteger("10"));
        testElt.xsetChild1(ival);
        assertEquals("<xml-fragment><child1>10</child1><child2>3</child2>" +
                "<child3>1</child3></xml-fragment>",
                testElt.xmlText());
           try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testMixedAnonymous() throws Exception {
        AnonymousMixedElt testElt = AnonymousMixedElt.Factory.newInstance();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        assertEquals(5, testElt.getChild2().intValue());
        assertEquals(
                XmlInteger.Factory.parse("<xml-fragment>5</xml-fragment>"),
                testElt.xgetChild2());
        XmlCursor cur = testElt.newCursor();
        cur.insertChars("Random mixed content");
        testElt.setChild3(new BigInteger("1"));
        assertEquals("", testElt.xmlText());

        testElt.xsetChild2(XmlInteger.Factory.parse("3"));
        assertEquals("", testElt.xmlText());
        assertTrue(testElt.validate());


    }
}
