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
import junit.framework.Assert;
import xsd.company.CompanyDocument;
import xsd.company.CompanyType;
import myPackage.Foo;
import myPackage.Bar;
import myPackage.FooHandler;
import myPackage.FooHandler.PreBookmark;
import myPackage.FooHandler.PostBookmark;
import org.apache.xmlbeans.XmlCursor;

/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Mar 28, 2004
 */
public class SimpleTest extends TestCase
{
    public SimpleTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SimpleTest.class);
    }

    public void testInterfaces()
    {
        String expected;
        String actual;

        CompanyDocument cDoc = CompanyDocument.Factory.newInstance();

        CompanyType co = cDoc.addNewCompany();
        co.setName2("xbean name");

        expected = "xbean name";
        actual = co.getName2();
        Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));

        expected = "{name in FooHandler}";
        actual = co.getName();
        Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));

        Foo foo = cDoc;

        expected = "{in FooHandler.handleFoo(s: param)}";
        actual = foo.foo("param");
        Assert.assertTrue("Expected: '" + expected + "'\n  actual: '" + actual + "'", expected.equals(actual));


        Bar bar = null;

        try
        {
            bar = cDoc;
            byte[] ba = bar.bar("param for bar");

            expected = "{in BarHandler.handleBar(s: param for bar)}";
            actual = new String(bar.bar("param for bar"));
            Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));
        }
        catch (Bar.MyException e)
        {
            Assert.assertTrue(false);
        }

        try
        {
            bar.bar(null);
            Assert.assertTrue(false);
        }
        catch (Bar.MyException e)
        {
            Assert.assertTrue(true);
        }
    }

    public void testPrePost()
    {
        String expected;
        String actual;
        XmlCursor xc;

        CompanyDocument cDoc = CompanyDocument.Factory.newInstance();

        // add new
        CompanyType co = cDoc.addNewCompany();

        xc = cDoc.newCursor();
        PreBookmark prebk = (PreBookmark) xc.getBookmark(PreBookmark.class);

        expected = "{preSet in FooHandler: 2, <xml-fragment></xml-fragment>, {company.xsd}company, false, -1}";
        actual = prebk.getMsg();
        Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));


        PostBookmark postbk = (PostBookmark) xc.getBookmark(PostBookmark.class);

        expected = "{postSet in FooHandler: 2, " + cDoc + ", {company.xsd}company, false, -1}";
        actual = postbk.getMsg();
        Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));

        xc.dispose();


        // set
        co.setName2("xbean name");

        xc = co.newCursor();
        prebk = (PreBookmark) xc.getBookmark(PreBookmark.class);

        expected = "{preSet in FooHandler: 1, <xml-fragment/>, name, true, -1}";
        actual = prebk.getMsg();
        Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));


        postbk = (PostBookmark) xc.getBookmark(PostBookmark.class);

        expected = "{postSet in FooHandler: 1, " + co + ", name, true, -1}";
        actual = postbk.getMsg();
        Assert.assertTrue("Expected: " + expected + "\n  actual: " + actual, expected.equals(actual));

        xc.dispose();
    }
}
