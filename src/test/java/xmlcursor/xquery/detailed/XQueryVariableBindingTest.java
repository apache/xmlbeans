/*   Copyright 2006 The Apache Software Foundation
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
 *   limitations under the License.
 */
package xmlcursor.xquery.detailed;

import common.Common;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/** This class tests the use of XmlOptions in binding XQuery variables */
public class XQueryVariableBindingTest extends Common
{
    public static final String XQUERY_CASE_DIR =
        XBEAN_CASE_ROOT + P + "xmlcursor" + P + "xquery";
    public static File dir = new File(XQUERY_CASE_DIR);

    private XmlCursor _testDocCursor1() throws Exception {
        String xml =
            "<elem1>" +
            "<elem11 id=\"123\">text11</elem11>" +
            "<elem21 id=\"456\">text11</elem21>" +
            "<elem12 idRef=\"123\"/>" +
            "<elem13 idRef=\"456\"/>" +
            "<elem14 idRef=\"123\"/>" +
            "<elem15 idRef=\"456\"/>" +
            "<elem16 idRef=\"123\"/>" +
            "<elem17 idRef=\"789\"/>" +
            "</elem1>";
        XmlObject doc = XmlObject.Factory.parse(xml);
        XmlCursor xc = doc.newCursor();
        return xc;
    }

    private void _verifySelection(XmlCursor xc) {
        assertEquals(3, xc.getSelectionCount());
        assertTrue(xc.toNextSelection());
        assertEquals("<elem12 idRef=\"123\"/>", xc.xmlText());
        assertTrue(xc.toNextSelection());
        assertEquals("<elem14 idRef=\"123\"/>", xc.xmlText());
        assertTrue(xc.toNextSelection());
        assertEquals("<elem16 idRef=\"123\"/>", xc.xmlText());
    }

    /** test the automatic binding of $this to the current node: selectPath() */
    @Test
    public void testThisVariable1() throws Exception {
        try (XmlCursor xc = _testDocCursor1()) {
            xc.toFirstChild(); //<elem1>
            xc.toFirstChild(); //<elem11>
            xc.selectPath("//*[@idRef=$this/@id]");
            _verifySelection(xc);
            xc.clearSelections();
        }
    }

    // this fails: see JIRA issue XMLBEANS-276
    /** test the binding of a variable to the current node: selectPath() */
    @Test
    public void testCurrentNodeVariable1() throws Exception {
        try (XmlCursor xc = _testDocCursor1()) {
            xc.toFirstChild();
            xc.toFirstChild();
            XmlOptions opts = new XmlOptions();
            opts.setXqueryCurrentNodeVar("cur");
            //String varDecl = "declare variable $cur external; ";
            //xc.selectPath(varDecl + "//*[@idRef=$cur/@id]", opts);
            xc.selectPath("//*[@idRef=$cur/@id]", opts);
            _verifySelection(xc);
            xc.clearSelections();
        }
    }

    private XmlCursor _testDocCursor2() throws Exception {
        File f = new File(dir, "employees.xml");
        XmlObject doc = XmlObject.Factory.parse(f);
        return doc.newCursor();
    }

    public void _verifyQueryResult(XmlCursor qc) {
        System.out.println(qc.xmlText());
        assertTrue(qc.toFirstChild());
        assertEquals("<phone location=\"work\">(425)555-5665</phone>", qc.xmlText());
        assertTrue(qc.toNextSibling());
        assertEquals("<phone location=\"work\">(425)555-6897</phone>", qc.xmlText());
        assertFalse(qc.toNextSibling());
    }

    /** test the automatic binding of $this to the current node: execQuery() */
    @Test
    public void testThisVariable2() throws Exception
    {
        String q =
            "for $e in $this/employees/employee " +
            "let $s := $e/address/state " +
            "where $s = 'WA' " +
            "return $e//phone[@location='work']";
        try (XmlCursor xc = _testDocCursor2();
            XmlCursor qc = xc.execQuery(q)) {
            _verifyQueryResult(qc);
        }
    }

    /** test the binding of a variable to the current node: execQuery() */
    @Test
    public void testCurrentNodeVariable2() throws Exception {
        String q =
            "for $e in $cur/employees/employee " +
            "let $s := $e/address/state " +
            "where $s = 'WA' " +
            "return $e//phone[@location='work']";
        XmlOptions opts = new XmlOptions();
        opts.setXqueryCurrentNodeVar("cur");
        try (XmlCursor xc = _testDocCursor2();
            XmlCursor qc = xc.execQuery(q, opts)) {
            _verifyQueryResult(qc);
        }
    }

    private XmlObject[] _execute(XmlObject xo, Map m, String q) {
        XmlOptions opts = new XmlOptions();
        opts.setXqueryVariables(m);
        return xo.execQuery(q, opts);
    }

    /** test the binding of a variable to an XmlTokenSource using a map */
    @Test
    public void testOneVariable() throws Exception {
        File f = new File(dir, "bookstore.xml");
        XmlObject doc = XmlObject.Factory.parse(f);
        String q =
            "declare variable $rt external; " +
            "for $x in $rt/book " +
            "where $x/price > 30 " +
            "return $x/title";
        Map<String,Object> m = new HashMap<>();
        m.put("rt", doc.selectChildren("", "bookstore")[0]);
        XmlObject[] results = _execute(doc, m, q);
        assertNotNull(results);
        assertEquals(2, results.length);
        assertEquals("<title lang=\"en\">XQuery Kick Start</title>", results[0].xmlText());
        assertEquals("<title lang=\"en\">Learning XML</title>", results[1].xmlText());
    }

    /** test the binding of multiple variables using a map;
        at the same time, test the binding of a variable to a String
     */
    @Test
    public void testMultipleVariables() throws Exception {
        File f = new File(dir, "bookstore.xml");
        XmlObject doc = XmlObject.Factory.parse(f);
        String q =
            "declare variable $rt external; " +
            "declare variable $c external; " +
            "for $x in $rt/book " +
            "where $x[@category=$c] " +
            "return $x/title";
        Map<String,Object> m = new HashMap<>();
        m.put("rt", doc.selectChildren("", "bookstore")[0]);
        m.put("c", "CHILDREN");
        XmlObject[] results = _execute(doc, m, q);
        assertNotNull(results);
        assertEquals(1, results.length);
        assertEquals("<title lang=\"en\">Harry Potter</title>", results[0].xmlText());
    }

}
