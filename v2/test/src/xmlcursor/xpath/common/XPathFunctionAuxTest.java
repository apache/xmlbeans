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

import xmlcursor.common.BasicCursorTestCase;

import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;
import tools.util.JarUtil;

import java.io.IOException;

/**
 * Verifies XPath using functions
 * http://www.w3schools.com/xpath/xpath_functions.asp
 *
 * @status inactive
 */

public class XPathFunctionAuxTest extends BasicCursorTestCase {
    public XPathFunctionAuxTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(XPathFunctionAuxTest.class);
    }

    static String fixPath(String path){
        return "$this"+path;
    }
    public void testFunctionCount_caseB() throws Exception {
	XmlObject xDoc = XmlObject.Factory.parse(JarUtil.getResourceFromJar(Common.XMLCASES_JAR,
									    "xbean/xmlcursor/xpath/cdcatalog.xml"));

	String ex1Simple = "count(//cd)";
        String ex1R1 = Common.XMLFRAG_BEGINTAG+"26"+Common.XMLFRAG_ENDTAG;
        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1)};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(ex1Simple);
        XPathCommon.display(x1);
        XPathCommon.compare(x1, exXml1);
        x1.dispose();
    }

     public void testFunctionConcat_caseB() throws Exception {
	String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String	sXPath="concat(name(//bar/*),//price/text())";
	String sExpected="price3.00";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
     }

     public void testFunctionStringLength_caseB() throws Exception {

	String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String	sXPath="string-length(name(//bar/*[last()]))";
	String sExpected="price".length()+"";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
  }

public void testFunctionSubString_caseB() throws Exception {
	String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String sXPath="substring(name(//bar/*),3,3)";
	String sExpected="ice";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());

    }

 public void testFunctionSubStringAfter_caseB() throws Exception {

	String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String sXPath="substring-after(name(//bar/*),'pr')";
	String sExpected="ice";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());

    }

 public void testFunctionSubStringBefore_caseB() throws Exception {

	String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String	sXPath="substring-before(name(//bar/*),'ice')";
	String sExpected="pr";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
    }

 public void testFunctionTranslate_caseB() throws Exception {
	String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String sXPath="translate(//bar/price/text(),'200','654')";//0 is now 5 &&4?
	String sExpected="355";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
    }

 public void testFunctionNumber_caseB() throws Exception {
	String sXml="<foo><bar><price at=\"val0\">3.00</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();
	String	sXPath="number(//price/text())+10";
	String sExpected="13.00";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());

   }

  public void testFunctionRound_caseB() throws Exception {
	String sXml="<foo><bar><price at=\"val0\">3.15</price><price at=\"val1\">2.87</price></bar><bar1>3.00</bar1></foo>";
	m_xc=XmlObject.Factory.parse(sXml).newCursor();

	String	sXPath="round(//bar/price[position()=1]/text())";
	String sExpected="3";
	m_xc.selectPath(fixPath(sXPath));
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
  }

public void testFunctionSum_caseB() throws Exception {

	 String sXml="<foo><bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar><bar1>3.00</bar1></foo>";
	 m_xc=XmlObject.Factory.parse(sXml).newCursor();

	 String	sXPath="sum(//bar/price)";
	String sExpected="5";
	m_xc.selectPath(sXPath);
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());
  }
//

  public void testFunctionBoolean_caseB() throws Exception {
	m_xc.selectPath("boolean(//foo/text())");
	m_xc.toNextSelection();
	assertEquals("false",m_xc.xmlText());
	m_xc.clearSelections();

       //number
       m_xc.selectPath("boolean(//price/text())");
       m_xc.toNextSelection();
       assertEquals("true",m_xc.xmlText());
       m_xc.clearSelections();


       //number
       m_xc.selectPath("boolean(//price/text() div 0)");
       m_xc.toNextSelection();
       assertEquals("false",m_xc.xmlText());
       m_xc.clearSelections();

       //node-set
       m_xc.selectPath("boolean(//price)");
       m_xc.toNextSelection();
       assertEquals("true",m_xc.xmlText());
       m_xc.clearSelections();

       m_xc.selectPath("boolean(//bar1)");
       m_xc.toNextSelection();
       assertEquals("false",m_xc.xmlText());
       m_xc.clearSelections();

   }

 public void testFunctionFalse_caseB() throws Exception {
        m_xc=XmlObject.Factory.parse("<foo><price at=\"val0\">3.00</price></foo>").newCursor();
	m_xc.selectPath("name(//*[boolean(text())=false()])");
	String sExpected="foo";
	m_xc.toNextSelection();
	assertEquals(sExpected,m_xc.xmlText());

   }
}
