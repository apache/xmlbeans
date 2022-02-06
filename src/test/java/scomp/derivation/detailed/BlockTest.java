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
package scomp.derivation.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.block.*;
import xbean.scomp.derivation.finalBlockDefault.EltDefaultBlockDocument;
import xbean.scomp.derivation.finalBlockDefault.EltNoBlockDocument;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class BlockTest {
    String restrContentValid = "<name>Bobby</name><age>20</age>";
    String restrContentInvalid = "<name>Bobby</name><age>40</age>";
    String extContent = "<name>Bobby</name><age>40</age><gender>f</gender>";

    public String getInstance(String elt, String type, boolean ext, boolean valid) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ns:" + elt + "  xmlns:ns=\"http://xbean/scomp/derivation/Block\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:type=\"ns:" + type + "\">");
        if (ext) {
            sb.append(extContent);
        } else if (valid) {
            sb.append(restrContentValid);
        } else {
            sb.append(restrContentInvalid);
        }

        sb.append("</ns:" + elt + ">");
        return sb.toString();
    }

    public String getInstanceDefault(String elt, String type, boolean ext, boolean valid) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ns:" + elt + "  xmlns:ns=\"http://xbean/scomp/derivation/FinalBlockDefault\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:type=\"ns:" + type + "\">");
        if (ext) {
            sb.append(extContent);
        } else if (valid) {
            sb.append(restrContentValid);
        } else {
            sb.append(restrContentInvalid);
        }

        sb.append("</ns:" + elt + ">");
        return sb.toString();
    }

    @Test
    void testBlockAll() throws Throwable {
        //subst ext type: should not be possible
        EltAllBaseDocument doc = EltAllBaseDocument.Factory.parse(getInstance("EltAllBase", "extAllT", true, true));
        assertFalse(doc.validate());

        //subst rest type:  should not be possible
        EltAllBaseDocument doc1 = EltAllBaseDocument.Factory.parse(getInstance("EltAllBase", "restAllT", false, false));

        assertFalse(doc.validate());

        doc1 = EltAllBaseDocument.Factory.parse(getInstance("EltAllBase", "restAllT", false, true));
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testBlockExtension() throws Throwable {
        //subst ext type: should not be possible
        assertThrows(XmlException.class, () -> EltEBaseDocument.Factory.parse(getInstance("EltExtE", "extET", true, true)),
            "Not a valid Substitution");

        // subst rest type: should work
        // base type: blocked="extension" so rest. type should be a vald subst
        EltEBaseDocument.Factory.parse(getInstance("EltEBase", "restET", false, false));

        String instance = getInstance("EltEBase", "restET", false, true);
        EltEBaseDocument doc1 = EltEBaseDocument.Factory.parse(instance);
        assertTrue(doc1.validate(createOptions()));
    }

    @Test
    void testBlockRestriction() throws Throwable {
        XmlOptions validateOptions = createOptions();
        //subst ext type: should work
        EltRBaseDocument doc = EltRBaseDocument.Factory.parse(getInstance("EltRBase", "extRT", true, true));
        assertTrue(doc.validate(validateOptions));
        //subst rest type:  should not be possible
        EltRBaseDocument doc1 = EltRBaseDocument.Factory.parse(getInstance("EltRBase", "restRT", false, false));
        assertFalse(doc1.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc1 = EltRBaseDocument.Factory.parse(getInstance("EltRBase", "restRT", false, true));
        validateOptions.getErrorListener().clear();
        assertFalse(doc1.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //should be equivalent to final="#all"
    @Test
    void testBlockRE_ER() throws Throwable {
        //subst ext type: should not be possible
        //ER
        EltERBaseDocument doc = EltERBaseDocument.Factory.parse(getInstance("EltERBase", "extERT", true, true));
        //RE
        EltREBaseDocument doc1 = EltREBaseDocument.Factory.parse(getInstance("EltREBase", "extRET", true, true));
        XmlOptions validateOptions = createOptions();
        assertFalse(doc1.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        //subst rest type:  should not be possible
        //ER
        EltERBaseDocument doc2 = EltERBaseDocument.Factory.parse(getInstance("EltERBase", "restERT", false, false));
        validateOptions.getErrorListener().clear();
        assertFalse(doc2.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc2 = EltERBaseDocument.Factory.parse(getInstance("EltERBase", "restERT", false, true));
        validateOptions.getErrorListener().clear();
        assertFalse(doc2.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        // RE
        EltREBaseDocument doc3 = EltREBaseDocument.Factory.parse(getInstance("EltREBase", "restRET", false, false));
        validateOptions.getErrorListener().clear();
        assertFalse(doc3.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc3 = EltREBaseDocument.Factory.parse(getInstance("EltREBase", "restRET", false, true));
        validateOptions.getErrorListener().clear();
        assertFalse(doc3.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * blockDefault="#all"
     */
    @Test
    void testBlockDefault() throws Throwable {
        EltDefaultBlockDocument doc = EltDefaultBlockDocument.Factory.parse(getInstanceDefault("EltDefaultBlock", "extNoneT", true, true));
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc = EltDefaultBlockDocument.Factory.parse(getInstanceDefault("EltDefaultBlock", "restNoneT", false, false));
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc = EltDefaultBlockDocument.Factory.parse(getInstanceDefault("EltDefaultBlock", "restNoneT", false, true));
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * blockDefault="#all"
     * local block=""
     */
    @Test
    void testBlockNone() throws Throwable {
        XmlOptions validateOptions = createOptions();
        String instance = getInstanceDefault("EltNoBlock", "extAllT", true, true);
        EltNoBlockDocument doc = EltNoBlockDocument.Factory.parse(instance);
        assertTrue(doc.validate(validateOptions));
        doc = EltNoBlockDocument.Factory.parse(getInstanceDefault("EltNoBlock", "restAllT", false, false));
        assertFalse(doc.validate());
        doc = EltNoBlockDocument.Factory.parse(getInstanceDefault("EltNoBlock", "restAllT", false, true));
        assertTrue(doc.validate(validateOptions));
    }
}
