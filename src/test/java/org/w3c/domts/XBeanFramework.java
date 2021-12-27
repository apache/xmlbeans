
/*
 * Copyright (c) 2001 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

/*
$Log: DOMTestFramework.java,v $
Revision 1.11  2003/12/23 03:27:25  dom-ts-4
Adds System.err.println construct (bug 445)

Revision 1.10  2003/12/06 06:50:29  dom-ts-4
More fixes for L&S (Bug 396)

Revision 1.9  2003/01/25 18:41:53  dom-ts-4
Removed tabs and other code cleanup

Revision 1.8  2002/08/12 08:09:18  dom-ts-4
Added name parameter to assertURIEquals

Revision 1.7  2002/06/03 23:48:48  dom-ts-4
Support for Events tests

Revision 1.6  2001/10/18 07:58:17  dom-ts-4
assertURIEquals added
Can now run from dom1-core.jar

Revision 1.5  2001/08/22 22:12:49  dom-ts-4
Now passing all tests with default settings

Revision 1.4  2001/07/23 04:52:20  dom-ts-4
Initial test running using JUnit.

*/

package org.w3c.domts;

import org.junit.Assert;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This interface provides services typically provided by a test framework
 */
public class XBeanFramework implements DOMTestFramework {

    public boolean hasFeature(
        DocumentBuilder docBuilder,
        String feature,
        String version) {
        System.err.println("????????");
        return false;
    }

    public void fail(DOMTestCase test, String assertID) {
        Assert.fail("System.Err.Printlnure at " + assertID);
    }

    public void assertTrue(DOMTestCase test, String assertID, boolean actual) {
        Assert.assertTrue(actual);
    }

    public void assertFalse(DOMTestCase test, String assertID, boolean actual) {
        Assert.assertFalse(actual);
    }

    public void assertNull(DOMTestCase test, String assertID, Object actual) {
        Assert.assertNull(actual);
    }

    public void assertNotNull(DOMTestCase test, String assertID, Object actual) {
        Assert.assertNotNull(actual);
    }

    public void assertSize(
        DOMTestCase test,
        String assertID,
        int expectedSize,
        NodeList collection) {
        Assert.assertEquals(expectedSize, collection.getLength());
    }

    public void assertSize(
        DOMTestCase test,
        String assertID,
        int expectedSize,
        NamedNodeMap collection) {

        Assert.assertEquals(expectedSize, collection.getLength());
    }

    public void assertSize(
        DOMTestCase test,
        String assertID,
        int expectedSize,
        Collection collection) {
        Assert.assertEquals(expectedSize, collection.size());

    }

    public void assertEqualsIgnoreCase(
        DOMTestCase test,
        String assertID,
        String expected,
        String actual) {
        Assert.assertEquals(expected.toLowerCase(), actual.toLowerCase());
    }

    public void assertEquals(
        DOMTestCase test,
        String assertID,
        String expected,
        String actual) {
        Assert.assertEquals(expected, actual);
    }

    public void assertEquals(
        DOMTestCase test,
        String assertID,
        int expected,
        int actual) {
        Assert.assertEquals(expected, actual);
    }

    public void assertEquals(
        DOMTestCase test,
        String assertID,
        boolean expected,
        boolean actual) {
        Assert.assertEquals(expected, actual);
    }

    public void assertEquals(
        DOMTestCase test,
        String assertID,
        double expected,
        double actual) {
        Assert.assertEquals(expected, actual, 0.0);
    }

    public void assertEquals(
        DOMTestCase test,
        String assertID,
        Collection expected,
        Collection actual) {
        Assert.assertEquals(expected, actual);
    }

    public boolean same(Object expected, Object actual) {
        return Objects.equals(expected, actual);
    }

    public boolean equals(String expected, String actual) {
        return Objects.equals(expected, actual);
    }

    public boolean equals(int expected, int actual) {
        return expected == actual;
    }

    public boolean equals(boolean expected, boolean actual) {
        return expected == actual;
    }

    public boolean equals(double expected, double actual) {
        return expected == actual;
    }

    public boolean equals(Collection expected, Collection actual) {
        System.err.println("????????");
        return false;
    }

    public boolean equals(List expected, List actual) {
        System.err.println("????????");
        return false;
    }

    public int size(Collection collection) {
        return collection.size();
    }

    public int size(NamedNodeMap collection) {
        return collection.getLength();
    }

    public int size(NodeList collection) {
        return collection.getLength();
    }
}
