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

import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlException;

import xbean.scomp.attribute.localAttrUse.LocalAttrUseDocDocument;
import xbean.scomp.attribute.localAttrUse.LocalAttrUseT;
import xbean.scomp.derivation.attributeUseProhibited.AttrProhibitedEltDocument;
import xbean.scomp.derivation.attributeUseProhibited.AttrUseProhibited;

import java.util.Calendar;
import java.math.BigInteger;

/**
 * @owner: ykadiysk
 * Date: Jul 14, 2004
 * Time: 2:09:08 PM
 */
public class LocalAttrUse extends BaseCase {
    /**
     * Default use of an attribute should be optional
     * Optional attributes can be missing
     *
     * @throws XmlException
     */
    public void testDefaultOptional() throws Throwable {
        //figure out the deal w/ namespaces here...
        LocalAttrUseT testDoc =
                LocalAttrUseDocDocument.Factory.parse("<pre:LocalAttrUseDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
                "attRequired=\"1\" " +
                "pre:attRequiredDefault=\"XBeanDef\" " +
                "pre:attRequiredFixed=\"XBeanFix\"/>")
                .getLocalAttrUseDoc();

        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }


    /**
     * test that an optional attr is not set before it is set
     *
     * @throws XmlException
     */
    public void testOptional() throws Throwable {
        LocalAttrUseDocDocument testDoc =
                LocalAttrUseDocDocument.Factory.newInstance();
        LocalAttrUseT att = testDoc.addNewLocalAttrUseDoc();
        assertTrue(!att.isSetLastPasswordUpdate());
        att.setLastPasswordUpdate(Calendar.getInstance());
        assertTrue(att.isSetLastPasswordUpdate());
    }

    /**
     * test that an optional attr is not set before it is set
     *
     * @throws XmlException
     */
    public void testOptionalParse() throws Throwable {
        LocalAttrUseT testDoc =
                LocalAttrUseDocDocument.Factory.parse("<pre:LocalAttrUseDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
                "attRequired=\"1\" " +
                "pre:attRequiredDefault=\"XBeanDef\" " +
                "pre:attRequiredFixed=\"XBeanFix\"/>")
                .getLocalAttrUseDoc();
        assertTrue(!testDoc.isSetLastPasswordUpdate());

        testDoc.setLastPasswordUpdate(Calendar.getInstance());
        assertTrue(testDoc.isSetLastPasswordUpdate());

    }

    public void testRequired() throws XmlException {
        //required attRequired is missing
        LocalAttrUseT testDoc =
                LocalAttrUseDocDocument.Factory.parse("<pre:LocalAttrUseDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
                "pre:attRequiredFixed=\"XBeanAttrStr\"" +
                " attRequired=\"34\"" +
                " />").getLocalAttrUseDoc();
        //catch XML error and assert message here
        assertFalse(testDoc.validate(validateOptions));


        //default required should not be explicitly needed?
        //assertEquals(1, errorList.size());

          showErrors();

    }

    /**
     * can not overwrite an existing value
     *
     * @throws XmlException
     */
    public void testRequiredFixed() throws XmlException {
        LocalAttrUseT testDoc =
                LocalAttrUseDocDocument.Factory.parse("<foo:LocalAttrUseDoc" +
                " xmlns:foo=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
                "foo:attRequiredFixed=\"foobar\" " +
                " />").getLocalAttrUseDoc();
        //catch XML error and assert message here
        assertTrue(!testDoc.validate());
        assertEquals("foobar", testDoc.getAttRequiredFixed());
        showErrors();
    }

    public void testRequiredDefault() throws XmlException {
        LocalAttrUseT testDoc =
                LocalAttrUseDocDocument.Factory.parse("<pre:LocalAttrUseDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/LocalAttrUse\" " +
                "pre:attRequiredDefault=\"newval\" " +
                " />").getLocalAttrUseDoc();
        //catch XML error and assert message here
        assertTrue(!testDoc.validate(validateOptions));
        assertEquals("newval", testDoc.getAttRequiredDefault());
        showErrors();
    }


    public void testUseProhibited() throws Throwable {
        AttrProhibitedEltDocument doc =
                AttrProhibitedEltDocument.Factory.newInstance();
        AttrUseProhibited elt = doc.addNewAttrProhibitedElt();
        elt.setAttRequiredFixed("XBeanFix");
        elt.setAttRequired(BigInteger.TEN);
        elt.setAttRequiredDefault("boo");
        try {
            assertTrue(elt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        //use here is prohibited
        elt.setAttOpt("bla");
        assertTrue(!elt.validate());

        elt.unsetAttOpt();
        try {
            assertTrue(elt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

}
