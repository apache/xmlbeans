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

package xmlcursor.xpath.jaxen.detailed;

import xmlcursor.xpath.common.XPathExpressionTest;

import java.util.HashMap;

/**
 * Verifies XPath with Expressions
 * http://www.w3schools.com/xpath/xpath_expressions.asp
 *
 *
 *
 */

public class XPathExpressionTestImpl extends XPathExpressionTest {
    public XPathExpressionTestImpl(String name) {
        super(name);
    }


/**
* the only difference from Jaxen queries is that we need a context to start from:
* preped "$this" to queries
*/
    public String getQuery(String testName, int testCase)
            throws IllegalArgumentException {
        Object queries;

        if ((queries = testMap.get(testName)) == null)
            throw new IllegalArgumentException("No queries for test" +
                    testName);
        else if (((String[]) queries).length <= testCase)
            throw new IllegalArgumentException("No query " + testCase +
                    " for test" + testName);
        else
            return ((String[]) queries)[testCase];

    }


}
