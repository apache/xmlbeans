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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

public class StandaloneTests extends TestCase
{
    StandaloneTests(String name) { super(name); }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(StandaloneTests.class.getName());
        suite.addTest(AssortedTests.suite());
        suite.addTest(IntTests.suite());
        suite.addTest(RuntimeSchemaLoaderTest.suite());
        suite.addTest(StoreTests.suite());
        suite.addTest(QNameTests.suite());
        suite.addTest(ValidationTests.suite());
        suite.addTest(CompilationTests.suite());
        suite.addTest(AnnotationsTests.suite());
        suite.addTest(EasyPoTests.suite());
        suite.addTest(NameworldTest.suite());
        suite.addTest(SchemaTypesTests.suite());
        suite.addTest(EnumTests.suite());
        suite.addTest(CreationTests.suite());
        suite.addTest(ThreadingTest.suite());
        suite.addTest(SerializationTests.suite());
        suite.addTest(DomTests.suite());
        suite.addTest(GDateTests.suite());
        suite.addTest(SubstGroupTests.suite());
        return suite;
    }
}
