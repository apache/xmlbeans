package scomp.elements.detailed;

import scomp.common.BaseCase;
import xbean.scomp.element.any.AnyEltDocument;
import xbean.scomp.element.any.AnySimpleDocument;
import xbean.scomp.substGroup.wide.BusinessShirtType;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlDate;

import java.math.BigInteger;
import java.util.GregorianCalendar;

/**
 * @owner: ykadiysk
 * Date: Aug 11, 2004
 * Time: 4:17:57 PM
 */
public class AnyTest extends BaseCase {

    public void testAny() throws Throwable {
        AnyEltDocument doc = AnyEltDocument.Factory.newInstance();
        BusinessShirtType bst = BusinessShirtType.Factory.newInstance();
        bst.setName("shirt");
        bst.setNumber("SkU034");
        bst.setColor("white");
        bst.setSize(BigInteger.TEN);
        doc.setAnyElt(bst);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


        XmlString val = XmlString.Factory.newInstance();
        val.setStringValue("foobar");
        doc.setAnyElt(val);

        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

    public void testAnySimple() throws Throwable {
        AnySimpleDocument doc =
                AnySimpleDocument.Factory.newInstance();
        XmlString str = XmlString.Factory.newInstance();
        str.setStringValue("foobar");
        doc.setAnySimple(str);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlDate date = XmlDate.Factory.newInstance();
        date.setCalendarValue(new GregorianCalendar(2004, 8, 12));
        doc.setAnySimple(date);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

}
