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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import tools.xml.XmlComparator;
import junit.framework.Assert;

/**
 *
 * @author jacobd
 */
public class XPathCommon {

    public static void display(XmlObject[] rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();

        for (int i = 0; i < rObj.length; i++) {
            System.out.println("[" + i + "] -- " + rObj[i].xmlText(xm));
        }
    }


    public static String getPrint(XmlObject[] rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();

        StringBuffer st = new StringBuffer();
        for (int i = 0; i < rObj.length; i++) {
            st.append("[" + i + "] -- " + rObj[i].xmlText(xm));
        }
        return st.toString();
    }

    public static String getPrint(XmlCursor rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();

        StringBuffer st = new StringBuffer();
        int i = 0;
        while (rObj.toNextSelection()) {
            st.append("[cursor-" + i + "] -- " + rObj.xmlText(xm));
            i++;
        }

        return st.toString();
    }

    public static void display(XmlCursor rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();

        int i = 0;
        while (rObj.toNextSelection()) {
            System.err.println("[cursor-" + i + "] -- " + rObj.xmlText(xm));
            i++;
        }
    }

    private static void check(XmlCursor actual, XmlCursor expected) {
        try {
            XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
            boolean match = XmlComparator.lenientlyCompareTwoXmlStrings(actual.toString(), expected.toString(), diag);

            Assert.assertTrue("***********************\nFound difference: \nactual=\n'" + actual +
                              "'\nexpected=\n'" + expected + "'\ndiagnostic=" + diag, match);
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
    }

    public static void compare(XmlObject rObj, XmlObject rSet) throws Exception{
        check(rObj.newCursor(),rSet.newCursor());
    }
    public static void compare(XmlObject[] rObj, XmlObject[] rSet) throws Exception {

        if (rObj.length != rSet.length)
            throw new Exception("Comparison Failed\n " +
                    "Actual Count: "+rObj.length +" Expected Count: "+rSet.length+"\n" +
                    "Actual:"+getPrint(rObj)+"\nExpected:"+getPrint(rSet));

        for (int i = 0; i < rObj.length; i++){
            check(rObj[i].newCursor(), rSet[i].newCursor());
        }
        System.out.println("Test Passed");
    }

    public static void compare(XmlCursor rObj, XmlObject[] rSet) throws Exception {

        if (rObj.getSelectionCount() != rSet.length) {
            System.out.println("EXPECTED ==");
            display(rSet);
            System.out.println("ACTUAL ==");
            display(rObj);

            throw new Exception("Compare failure == Result Count was not equal to actual count\n" +
                    "Actual Count: "+rObj.getSelectionCount() +" Expected Count: "+rSet.length+"\n" +
                    "Actual:" + getPrint(rObj) + "\nExpected:" + getPrint(rSet));
        }
        int i = 0;
        while (rObj.toNextSelection()) {
            //System.out.println("[cursor-" + i + "] -- " + rObj.xmlText(xm));
            //System.out.println("[Expected-" + i + "] -- " + rSet[i].xmlText(xm));

            check(rObj, rSet[i].newCursor());
            i++;
        }

        System.out.println("Test Passed");
    }

    public static void checkLength(XmlCursor rObj, int count) throws Exception{
        if(rObj.getSelectionCount() != count){
            throw new Exception("Length == Return Count was not equal\n"+
                    "Cursor-Count: "+ rObj.getSelectionCount()+" Expected: "+count+"\n"+
                    getPrint(rObj));
        }
    }

}
