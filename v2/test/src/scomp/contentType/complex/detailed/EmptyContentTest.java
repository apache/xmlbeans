package scomp.contentType.complex.detailed;

import xbean.scomp.contentType.complexTypeTest.EmptyTypeDocument;
import xbean.scomp.contentType.complexTypeTest.EmptyT;
import xbean.scomp.contentType.complexTypeTest.EmptyMixedTypeDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlException;
import scomp.common.BaseCase;

/**
 * @owner: ykadiysk
 * Date: Jul 16, 2004
 * Time: 3:31:03 PM
 */
public class EmptyContentTest extends BaseCase {
    public void testIllegalContent() {
        EmptyTypeDocument doc = EmptyTypeDocument.Factory.newInstance();
        EmptyT elt = doc.addNewEmptyType();
        assertTrue(!elt.isSetEmptyAttr());
        elt.setEmptyAttr("foobar");
        assertTrue(elt.validate());
        XmlCursor cur = elt.newCursor();
        cur.toFirstContentToken();
        cur.beginElement("foobarElt");
        assertTrue(!elt.validate(validateOptions));
        showErrors();
        //  fail("Assert correct error here");

    }

    public void testLegalContent() throws XmlException {
        EmptyTypeDocument doc = EmptyTypeDocument.Factory.newInstance();
        EmptyT elt = doc.addNewEmptyType();
        assertTrue(!elt.isSetEmptyAttr());
        elt.setEmptyAttr("foobar");
        assertTrue(elt.isSetEmptyAttr());
        assertEquals("foobar", elt.getEmptyAttr());
        XmlString expected=XmlString.Factory.newInstance();
        expected.setStringValue("foobar");
        assertEquals( expected, elt.xgetEmptyAttr());
        elt.unsetEmptyAttr();
        assertTrue(!elt.isSetEmptyAttr());
        assertTrue(elt.validate());     
    }
}
