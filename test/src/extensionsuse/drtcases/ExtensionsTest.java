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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import drtcases.prePostFeature.readOnlyBean.ReadOnlyTest;
import drtcases.prePostFeature.ValueRestriction.ValueRestrictionTest;

/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Mar 28, 2004
 */
public class ExtensionsTest extends TestCase
{
    public ExtensionsTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(SimpleTest.suite());
        suite.addTest(AverageCaseTest.suite());
        suite.addTest(FixedAttrTest.suite());
        suite.addTest(MultInterfacesTest.suite());
        suite.addTest(ReadOnlyTest.suite());
        suite.addTest(ValueRestrictionTest.suite());

        return suite;
    }
}
