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

package scomp.contentType.simple.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlSimpleList;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.list.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class ListType {
    @Test
    void testListTypeAnonymous() throws Throwable {
        ListEltTokenDocument doc = ListEltTokenDocument.Factory.newInstance();
        assertNull(doc.getListEltToken());
        List<Object> values = new LinkedList<>();
        values.add("lstsmall");
        values.add("lstmedium");
        doc.setListEltToken(values);
        assertTrue(doc.validate(createOptions()));
        values.set(0, 4);

        // since the list has enumerations, it contains a fixed number of Java constants in the xobj
        // which are checked for types and an exception is expected irrespective of validateOnSet XmlOption
        // if the value being set is not one of them
        assertThrows(XmlValueNotSupportedException.class, () -> doc.setListEltToken(values));
    }

    @Test
    void testListTypeGlobal() throws Throwable {
        String input =
            "<ListEltInt xmlns=\"http://xbean/scomp/contentType/List\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >" +
            "-1 -3" +
            "</ListEltInt>";
        ListEltIntDocument doc = ListEltIntDocument.Factory.parse(input);
        List result = doc.getListEltInt();
        assertEquals(-1, ((Integer) result.get(0)).intValue());
        assertEquals(-3, ((Integer) result.get(1)).intValue());
        GlobalSimpleT gst = GlobalSimpleT.Factory.newInstance();
        assertTrue(result instanceof XmlSimpleList);
        // immutable list
        assertThrows(UnsupportedOperationException.class, () -> result.set(0, "foobar"));

        List<String> arrayList = new ArrayList<>();
        arrayList.add("foobar");
        List<String> newList = new XmlSimpleList<>(arrayList);
        assertThrows(XmlValueOutOfRangeException.class, () -> gst.setListValue(newList));
        doc.xsetListEltInt(gst);
        assertTrue(doc.validate(createOptions()));
    }

    @Disabled
    public void testListofLists() {
        //also,a list of union that contains a list is not OK
    }

    /**
     * values should be in [small,medium,large,1-3,-3,-2,-1]
     */
    @Test
    void testListofUnions() throws Throwable {
        ListUnionDocument doc = ListUnionDocument.Factory.newInstance();
        List<Object> arrayList = Arrays.asList("small", "large", -1, 2);
        doc.setListUnion(arrayList);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testListofUnionsIllegal() throws Throwable {
        String input =
            "<ListUnion xmlns=\"http://xbean/scomp/contentType/List\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >" +
            "small -3 11" +
            "</ListUnion>";
        ListUnionDocument doc = ListUnionDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_VALID$UNION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }


    @Test
    void testListofUnions2() throws Throwable {
        ListUnion2Document doc = ListUnion2Document.Factory.newInstance();
        List<Object> arrayList = Arrays.asList("small", "large", -1, 2, "addVal1", "addVal2", "addVal3");

        doc.setListUnion2(arrayList);
        assertTrue(doc.validate(createOptions()));
    }
}
