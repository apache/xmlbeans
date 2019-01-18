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

package ValidatingXSRTests.detailed;

import ValidatingXSRTests.common.TestPrefixResolver;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.validator.ValidatorUtil;
import org.junit.Test;
import tools.xml.Utils;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.fail;

public class ValidatorUtilTests {
    @Test
    public void testValidQName()
        throws Exception
    {
        String xml = "foo:foo";

        TestPrefixResolver pRes = new TestPrefixResolver("foo", "http://openuri.org/test/My");
        SchemaType type = org.openuri.test.simType.QNameType.type;

        Collection errors = new ArrayList();
        boolean success = ValidatorUtil.validateSimpleType(type, xml, errors, pRes);
        if (!success)
        {
            Utils.printXMLErrors(errors);
            fail("testValidQName failed");
        }
    }


    @Test
    public void testInvalidQName() {
        String xml = "foo:bz";
        TestPrefixResolver pRes = new TestPrefixResolver("foo", "http://openuri.org/test/My");
        SchemaType type = org.openuri.test.simType.QNameType.type;

        Collection errors = new ArrayList();
        boolean success = ValidatorUtil.validateSimpleType(type, xml, errors, pRes);
        System.out.println("Expected Errors:");
        tools.xml.Utils.printXMLErrors(errors);
        if (success)
            fail("testInvalidQName failed: Invalid QName was validated");
    }

    // TODO: add more simpleType tests... maybe create a generic test method

}
