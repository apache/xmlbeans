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

package xmlcursor.xpath.complex.detailed;

import xmlcursor.xpath.common.XPathFunctionTest;


import org.apache.xmlbeans.XmlObject;

/**
 * Queries here overwrite whatever is loaded in the query map if
* the syntax is different
 */

public class XPathFunctionTestImpl extends XPathFunctionTest {
    public XPathFunctionTestImpl (String name) {
        super(name);
        testMap.put("testFunctionCount",new String[]{
            "count(//cd)",
             "//cd[position()=2]"
        }) ;
         testMap.put("testFunctionLocalName",new String[]{
        "//*[local-name(.)='bar']"
         });
         testMap.put("testFunctionConcat",new String[]{
        "//bar/*[name(.)=concat(\"pr\",\"ice\")]"
       });

        testMap.put("testFunctionString",new String[]{
        "/foo/*[name(.)=" +
                "concat(\"bar\",string(./foo/bar/price[last()]))]"
        });

            testMap.put("testFunctionStringLength",new String[]{
        "//bar/*[string-length(name(.))=5]"
            });
        testMap.put("testFunctionSubString",new String[]{
        "//bar/*[substring(name(.),3,3)=\"ice\"]"
        });

         testMap.put("testFunctionSubStringAfter",new String[]{
        "//bar/*[substring-after(" +
                 "name(.),'pr'" +
                 ")=\"ice\"]"
         });

         testMap.put("testFunctionSubStringBefore",new String[]{
        "//bar/*[substring-before(" +
                 "name(.),'ice'" +
                 ")=\"pr\"]"});

         testMap.put("testFunctionTranslate",new String[]{
        "//bar/*[translate(name(.)," +
                "'ice','pr')=\"prpr\"]"});

         testMap.put("testFunctionLang",new String[]{
        "//price[lang(\"en\")=true()]",
        "//foo[lang(\"en\")=true()]"}
        );

        testMap.put("testFunctionTrue",new String[]{
        "//*[boolean(@at)=true()]"  });
    }


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


    public void testErrorMessages() throws Exception {
      //do nothing for Jaxen
    }

    //ensure Jaxen is not in the classpath
    public void testAntiJaxenTest(){
        try{
        m_xc.selectPath("//*");
            fail("XQRL shouldn't handle absolute paths");
        }catch(Throwable t){}

    }

}
