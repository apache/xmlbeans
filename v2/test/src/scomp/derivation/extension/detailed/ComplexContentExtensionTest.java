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
package scomp.derivation.extension.detailed;

import scomp.common.BaseCase;
import xbean.scomp.derivation.complexExtension.ChoiceExtensionEltDocument;
import xbean.scomp.derivation.complexExtension.ChoiceExtensionT;
import xbean.scomp.derivation.complexExtension.SequenceExtensionEltDocument;
import xbean.scomp.derivation.complexExtension.SequenceExtensionT;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 * @owner: ykadiysk
 * Date: Jul 21, 2004
 * Time: 10:17:48 AM
 */
public class ComplexContentExtensionTest extends BaseCase {


    public void testSequenceExtension() throws Throwable {
        SequenceExtensionEltDocument doc = SequenceExtensionEltDocument.Factory
                .newInstance();
        SequenceExtensionT elt = doc.addNewSequenceExtensionElt();
        elt.setChild1(BigInteger.ONE);
        elt.addChild2("foobar");
        elt.setChild3Array(new BigInteger[]{BigInteger.ONE, BigInteger.ZERO});
        elt.addExtraEltInt(3);
        elt.setExtraEltStrArray(new String[]{"extra1", "extra2"});

        assertEquals(BigInteger.ONE, elt.getChild1());
        assertEquals("foobar", elt.getChild2Array()[0]);
        elt.insertChild2(0, "newfoobar");
        assertEquals("newfoobar", elt.getChild2Array()[0]);

        assertTrue(!doc.validate(validateOptions));
              showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
        };
                     assertTrue(compareErrorCodes(errExpected));


        elt.removeExtraEltInt(0);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        elt.removeExtraEltStr(1);
        assertTrue(elt.validate());

    }

    /**
     *  result type is a sequence of 2 choices
     * Valid sets: { (child1 xor child2 xor child3 )(extraEltStr xor extraEltStr) }
     * @throws Throwable
     */
    public void testChoiceExtension() throws Throwable {
        ChoiceExtensionEltDocument doc = ChoiceExtensionEltDocument.Factory
                .newInstance();
        ChoiceExtensionT elt = doc.addNewChoiceExtensionElt();
        elt.setChild1(new BigInteger("10"));
        elt.addChild2("foobar");
        elt.setChild3Array(new BigInteger[]{BigInteger.ONE, BigInteger.ZERO});
        elt.addExtraEltInt(3);
        elt.setExtraEltStrArray(new String[]{"extra1", "extra2"});
        assertTrue(!doc.validate(validateOptions));
                    showErrors();
             //TODO: child 2 and child3 not allowed
             //extraEltStr not allowed
              String[] errExpected = new String[]{
                  XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
                   XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
                  XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
              };
                           assertTrue(compareErrorCodes(errExpected));

        assertEquals(new BigInteger("10"), elt.getChild1());
        assertEquals("foobar", elt.getChild2Array()[0]);
        elt.unsetChild1();
        elt.removeExtraEltInt(0);
        elt.removeChild2(0);
        clearErrors();
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            System.out.println(" test failed :");
            showErrors();
            throw t;
        }


        XmlInteger expected = XmlInteger.Factory.newInstance();
        expected.setBigIntegerValue(BigInteger.ONE);
        assertEquals(expected, elt.xgetChild3Array()[0]);
        expected.setBigIntegerValue(BigInteger.ZERO);
        assertEquals(expected, elt.xgetChild3Array()[1]);
        assertEquals(3, elt.getExtraEltIntArray()[0]);
        assertEquals(3, elt.getExtraEltIntArray(0));
        clearErrors();
        assertTrue(!doc.validate(validateOptions));
                           showErrors();
                    errExpected = new String[]{
                         XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
                          XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
                         XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
                     };
                                  assertTrue(compareErrorCodes(errExpected));
        elt.removeExtraEltInt(0);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlString expected1 = XmlString.Factory.newInstance();
        expected1.setStringValue("extra1");
        assertEquals(expected1, elt.xgetExtraEltStrArray(0));
        expected1.setStringValue("extra2");
        assertEquals(expected1, elt.xgetExtraEltStrArray()[1]);

    }

    public void testAllExtension() {
        fail("Compile Time test");
    }

   
}
