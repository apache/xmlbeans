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
package scomp.derivation.detailed;

import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.xabstract.AbstractT;
import xbean.scomp.derivation.xabstract.EltAbstractDocument;
import xbean.scomp.derivation.xabstract.EltConcreteDocument;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static scomp.common.BaseCase.createOptions;

public class AbstractTest {

    /**
     * This is an abstract element...no instance should ever be valid
     */
    @Test
    void testElementAbstract() {
        EltAbstractDocument doc = EltAbstractDocument.Factory.newInstance();
        AbstractT elt = doc.addNewEltAbstract();
        elt.setAge(new BigInteger("15"));
        elt.setName("Ben");
        assertNotNull(elt);
        assertFalse(elt.validate());
    }

    @Test
    void testElementAbstractParse() throws XmlException {
        String input =
            "<foo:EltAbstract " +
            "xmlns:foo=\"http://xbean/scomp/derivation/Abstract\">" +
            " <name>Bob</name><age>25</age><gender>G</gender>" +
            "</foo:EltAbstract>";
        EltAbstractDocument doc = EltAbstractDocument.Factory.parse(input);
        assertFalse(doc.validate(createOptions()));
    }

    @Test
    void testElementConcrete() throws Throwable {
        String input =
            "<foo:EltConcrete " +
            "xmlns:foo=\"http://xbean/scomp/derivation/Abstract\">" +
            " <name>Bob</name><age>25</age><gender>G</gender>" +
            "</foo:EltConcrete>";
        EltConcreteDocument doc = EltConcreteDocument.Factory.parse(input);
        assertFalse(doc.validate(createOptions()));
    }
}
