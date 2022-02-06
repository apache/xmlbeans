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
package xmlcursor.xpath.common;

public interface XPathTestBase {

    static String getQuery(String testName, int testCase) {
        switch (testName) {
            case "testAddition":
                return "//price[position()=0+1]";
            case "testSubtraction":
                return "//price[position()=4-2]";
            case "testMultiplication":
                return "//price[position()=2*1]";
            case "testDiv":
                return new String[]{
                    "//price[ position()= last() div 2 ]",
                    "//price[ position()= 4 div 2 ]",
                    "//price[ position()=10 div 0 ]",
                    "//price[position()=round(22 div 7)-1]"
                }[testCase];
            case "testMod":
                return new String[]{
                    "//price[ position() mod 2 = 0 ]",
                    "//price[position() = 5 mod 3]"
                }[testCase];
            case "testEqual":
                return "//bar[price=5.00]";
            case "testEqualityNodeset":
                return "//bar[price=3]";
            case "testNotEqual":
                return "//bar[price!=3]";
            case "testLessThan":
                return "//bar[price < 2 ]";
            case "testLessOrEqual":
                return "//bar[price <=2 ]";
            case "testGreaterThan":
                return "//bar[price > 2 ]";
            case "testGreaterOrEqual":
                return "//bar[price >= 2 ]";
            case "testOr":
                return "//price[text()>3 or @at=\"val1\"]";
            case "testAnd":
                return "//price[text()>2 and @at=\"val1\"]";
            case "testFunctionId":
                return new String[]{
                    "id(\"bobdylan\")",
                    "id(\"foobar\")",
                    "//child::cd[position()=3]"}[testCase];
            case "testFunctionLast":
                return "/catalog/cd[last()]";
            case "testFunctionNamespaceURI":
                return "//*[namespace-uri(.)=\"uri.org\"]";
            case "testFunctionNumber":
                return "/foo/bar[number(price)+1=4]";
            case "testFunctionRound":
                return "//bar//*[round(text())=3]";
            case "testFunctionSum":
                return "//bar[position()=sum(price)-4]";
            case "testFunctionBoolean":
                return "/foo[boolean(.//@at)]";
            case "testFunctionFalse":
                return "//foo[boolean(price)=false()]";
            case "testFunctionLang":
                return new String[]{
                "//price[xf:lang(\"en\")=true()]",
                "//foo[xf:lang(\"en\")=true()]"}[testCase];
            case "testFunctionTrue":
                return "//*[xf:boolean(@at)=true()]";
            default:
                return null;
        }
    }
}
