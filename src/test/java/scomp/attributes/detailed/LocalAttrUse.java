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

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.attribute.localAttrUse.LocalAttrUseDocDocument;
import xbean.scomp.attribute.localAttrUse.LocalAttrUseT;
import xbean.scomp.derivation.attributeUseProhibited.AttrProhibitedEltDocument;
import xbean.scomp.derivation.attributeUseProhibited.AttrUseProhibited;

import java.math.BigInteger;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class LocalAttrUse {
    /**
     * Default use of an attribute should be optional
     * Optional attributes can be missing
     */
    @Test
    void testDefaultOptional() throws Throwable {
        String localAttDoc =
            "<pre:LocalAttrUseDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
            "attRequired=\"1\" " +
            "pre:attRequiredDefault=\"XBeanDef\" " +
            "pre:attRequiredFixed=\"XBeanFix\"/>";

        //figure out the deal w/ namespaces here...
        LocalAttrUseT testDoc = LocalAttrUseDocDocument.Factory.parse(localAttDoc).getLocalAttrUseDoc();
        assertTrue(testDoc.validate(createOptions()));
    }


    /**
     * test that an optional attr is not set before it is set
     */
    @Test
    void testOptional() throws Throwable {
        LocalAttrUseDocDocument testDoc = LocalAttrUseDocDocument.Factory.newInstance();
        LocalAttrUseT att = testDoc.addNewLocalAttrUseDoc();
        assertFalse(att.isSetLastPasswordUpdate());
        att.setLastPasswordUpdate(Calendar.getInstance());
        assertTrue(att.isSetLastPasswordUpdate());
    }

    /**
     * test that an optional attr is not set before it is set
     */
    @Test
    void testOptionalParse() throws Throwable {
        String localAttDoc =
            "<pre:LocalAttrUseDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
            "attRequired=\"1\" " +
            "pre:attRequiredDefault=\"XBeanDef\" " +
            "pre:attRequiredFixed=\"XBeanFix\"/>";
        LocalAttrUseT testDoc = LocalAttrUseDocDocument.Factory.parse(localAttDoc).getLocalAttrUseDoc();
        assertFalse(testDoc.isSetLastPasswordUpdate());

        testDoc.setLastPasswordUpdate(Calendar.getInstance());
        assertTrue(testDoc.isSetLastPasswordUpdate());
    }

    @Test
    void testRequired() throws XmlException {
        String localAttDoc =
            "<pre:LocalAttrUseDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
            "pre:attRequiredFixed=\"XBeanAttrStr\"" +
            " attRequired=\"34\"" +
            " />";

        //required attRequired is missing
        LocalAttrUseT testDoc = LocalAttrUseDocDocument.Factory.parse(localAttDoc).getLocalAttrUseDoc();
        //catch XML error and assert message here
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));


        //default required should not be explicitly needed?
        //assertEquals(1, errorList.size());

        String[] errExpected = {
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * can not overwrite an existing value
     */
    @Test
    void testRequiredFixed() throws XmlException {
        String localAttDoc =
            "<foo:LocalAttrUseDoc" +
            " xmlns:foo=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
            "foo:attRequiredFixed=\"foobar\" " +
            " />";
        LocalAttrUseT testDoc = LocalAttrUseDocDocument.Factory.parse(localAttDoc).getLocalAttrUseDoc();
        //catch XML error and assert message here
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));
        assertEquals("foobar", testDoc.getAttRequiredFixed());
        //attr locally valid for fixed val
        String[] errExpected = {
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testRequiredDefault() throws XmlException {
        String localAttDoc =
            "<pre:LocalAttrUseDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
            "pre:attRequiredDefault=\"newval\" " +
            " />";
        LocalAttrUseT testDoc = LocalAttrUseDocDocument.Factory.parse(localAttDoc).getLocalAttrUseDoc();
        //catch XML error and assert message here
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));
        assertEquals("newval", testDoc.getAttRequiredDefault());
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testUseProhibited() throws Throwable {
        AttrProhibitedEltDocument doc = AttrProhibitedEltDocument.Factory.newInstance();
        AttrUseProhibited elt = doc.addNewAttrProhibitedElt();
        elt.setAttRequiredFixed("XBeanFix");
        elt.setAttRequired(new BigInteger("10"));
        elt.setAttRequiredDefault("boo");
        XmlOptions validateOptions = createOptions();
        assertTrue(elt.validate(validateOptions));
        //use here is prohibited
        elt.setAttOpt("bla");
        assertFalse(elt.validate(validateOptions));
        //does Kevin have the right code here? doesn't seem so to me?
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        elt.unsetAttOpt();
        assertTrue(elt.validate(validateOptions));
    }

}
