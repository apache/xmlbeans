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
package scomp.derivation.restriction.detailed;

import xbean.scomp.derivation.mixedContentRestriction.*;
import scomp.common.BaseCase;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;

/**
 * @owner: ykadiysk
 * Date: Jul 22, 2004
 * Time: 5:21:53 PM
 */
public class MixedContentRestriction extends BaseCase{

    public void testRestrictedMixed() throws Throwable{
        MixedEltDocument doc=MixedEltDocument.Factory.newInstance();
        RestrictedMixedT elt=doc.addNewMixedElt();
        assertTrue( !elt.isSetChild1());
        elt.setChild1(BigInteger.TEN);
        elt.setChild2(BigInteger.ZERO);
        //insert text b/n the 2 elements
        XmlCursor cur=elt.newCursor();
        cur.toFirstContentToken();
        assertTrue(cur.toNextSibling());
        cur.insertChars("My chars");
          try {
            assertTrue( doc.validate());
        }
        catch (Throwable t) {
            doc.validate(validateOptions);
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment>" +
                "<child1>10</child1>My chars<child2>0</child2>" +
                "</xml-fragment>", elt.xmlText());

    }
    public void testRestrictedEltOnly() throws Throwable{
       ElementOnlyEltDocument doc=ElementOnlyEltDocument.Factory.newInstance();
        RestrictedEltT elt=doc.addNewElementOnlyElt();
        assertTrue( !elt.isSetChild1());
        elt.setChild1(BigInteger.TEN);
        elt.setChild2(BigInteger.ZERO);
        //insert text b/n the 2 elements
        XmlCursor cur=elt.newCursor();
       cur.toFirstContentToken();
        assertTrue(cur.toNextSibling());
        cur.insertChars("My chars");
        assertTrue( !doc.validate(validateOptions));
        showErrors();
        //should be valid w/o the Text there
        cur.toPrevToken();
         assertEquals("<xml-fragment>" +
                "<child1>10</child1>My chars<child2>0</child2>" +
                "</xml-fragment>", elt.xmlText());
       assertTrue(cur.removeXml());
        try {
            assertTrue( doc.validate());
        }
        catch (Throwable t) {
            doc.validate(validateOptions);
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment>" +
                "<child1>10</child1><child2>0</child2>" +
                "</xml-fragment>", elt.xmlText());


    }
    //seems that this is not a valid example p.329 top
    //public void testRestrictedMixedToSimple() throws Throwable{}
    public void testRestrictedMixedToEmpty() throws Throwable{
         Mixed2EmptyEltDocument doc=Mixed2EmptyEltDocument.Factory.newInstance();
         Mixed2EmptyT elt=doc.addNewMixed2EmptyElt();
        assertEquals(null,elt.xgetChild1());
        elt.setChild1(BigInteger.TEN);
         assertTrue( !doc.validate(validateOptions));
        showErrors();
        elt.unsetChild1();
         try {
            assertTrue( doc.validate());
        }
        catch (Throwable t) {
            doc.validate(validateOptions);
            showErrors();
            throw t;
        }

    }
}
