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
package scomp.derivation.restriction.facets.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.facets.list.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 *
 */
public class ListRestriction {
    @Test
    void testLengthFacet() {
        LengthEltDocument doc = LengthEltDocument.Factory.newInstance();
        List<String> vals = new ArrayList<>();
        vals.add("lstsmall");

        doc.setLengthElt(vals);
        //this should be too short
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_LENGTH_VALID$LIST_LENGTH};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        vals.add("lstsmall");
        doc.setLengthElt(vals);
        assertTrue(doc.validate(validateOptions));
        //this should be too long
        vals.add("lstsmall");
        doc.setLengthElt(vals);
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.DATATYPE_LENGTH_VALID$LIST_LENGTH};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testMinLengthFacet() throws Throwable {
        String input =
            "<MinLengthElt xmlns=\"http://xbean/scomp/derivation/facets/List\">" +
            "lstsmall lstlarge lstsmall" +
            "</MinLengthElt>";
        MinLengthEltDocument doc = MinLengthEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        List vals = doc.getMinLengthElt();
        assertEquals(3, vals.size());
        List newvals = new ArrayList(vals);
        newvals.remove(0);
        newvals.remove(1);
        assertEquals(1, newvals.size());

        doc.setMinLengthElt(newvals);
        assertEquals(doc.getMinLengthElt().size(), doc.xgetMinLengthElt().getListValue().size());
        assertEquals(1, doc.xgetMinLengthElt().getListValue().size());

        assertEquals("lstlarge", (String) doc.xgetMinLengthElt().getListValue().get(0));
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_LENGTH_VALID$LIST_LENGTH};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testMaxLengthFacet() throws XmlException {
        String input =
            "<MaxLengthElt xmlns=\"http://xbean/scomp/derivation/facets/List\">" +
            "lstsmall lstlarge lstsmall" +
            "</MaxLengthElt>";
        MaxLengthEltDocument doc = MaxLengthEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_LENGTH_VALID$LIST_LENGTH};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        MaxLengthFacet elt = MaxLengthFacet.Factory.newInstance();
        List<String> vals = new ArrayList<>();
        vals.add("lstsmall");
        vals.add("lstsmall");
        //why is there no xsetListValue method here?
        elt.setListValue(vals);
        doc.xsetMaxLengthElt(elt);
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * Walmsley, p. 215...
     */
    @Test
    void testEnum() {
        EnumEltDocument doc = EnumEltDocument.Factory.newInstance();
        List<Object> vals = new ArrayList<>(Arrays.asList("small", "medium", "large"));

        doc.setEnumElt(vals);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        vals.clear();
        vals.add(2);
        vals.add(3);
        vals.add(1);
        doc.setEnumElt(vals);
        assertTrue(doc.validate(validateOptions));

        vals.clear();
        vals.add("small");
        vals.add(10);
        doc.setEnumElt(vals);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_VALID$UNION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testPattern() {
        PatternEltDocument doc = PatternEltDocument.Factory.newInstance();
        List<Integer> vals = Arrays.asList(152, 154, 156, 918, 342);

        doc.setPatternElt(vals);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
    }
}
