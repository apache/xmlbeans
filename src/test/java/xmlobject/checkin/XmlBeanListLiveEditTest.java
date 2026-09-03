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

package xmlobject.checkin;

import com.easypo.XmlLineItemBean;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.openuri.sgs.RootDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lists handed out by getXxxList() are views over the document rather than
 * copies of it. Everything written through the list or through one of its iterators
 * has to land in the store, and anything written to the bean has to show up in a
 * list that was handed out earlier. Each check reads the change back twice: once
 * through the typed accessors, and once from the document serialized and parsed
 * again, which only passes if the edit really reached the store.
 */
class XmlBeanListLiveEditTest {
    private static final String SGS = "http://openuri.org/sgs";

    // ---- line items, a list of XmlObjects (JavaListXmlObject) ----

    private static PurchaseOrder order(String... descriptions) throws XmlException {
        StringBuilder xml = new StringBuilder("<purchase-order xmlns='http://openuri.org/easypo'>");
        for (String description : descriptions) {
            xml.append("<line-item><description>").append(description).append("</description></line-item>");
        }
        xml.append("</purchase-order>");

        return XmlPurchaseOrderDocumentBean.Factory.parse(xml.toString()).getPurchaseOrder();
    }

    /** The element itself rather than just its contents, so that it can be parsed again. */
    private static String xml(XmlObject o) {
        return o.xmlText(new XmlOptions().setSaveOuter());
    }

    private static XmlLineItemBean lineItem(String description) {
        XmlLineItemBean item = XmlLineItemBean.Factory.newInstance();
        item.setDescription(description);
        return item;
    }

    private static List<String> descriptionsOf(PurchaseOrder po) {
        List<String> descriptions = new ArrayList<>();
        for (XmlLineItemBean item : po.getLineItemArray()) {
            descriptions.add(item.getDescription());
        }
        return descriptions;
    }

    /** Reads the descriptions back from the typed accessors and from the serialized document. */
    private static void assertDocument(PurchaseOrder po, String... expected) throws XmlException {
        List<String> wanted = Arrays.asList(expected);

        assertEquals(wanted, descriptionsOf(po));
        assertEquals(expected.length, po.sizeOfLineItemArray());
        assertEquals(expected.length, po.getLineItemList().size());

        PurchaseOrder reparsed = XmlPurchaseOrderDocumentBean.Factory.parse(xml(po)).getPurchaseOrder();
        assertEquals(wanted, descriptionsOf(reparsed), "the edit did not reach the document");
    }

    @Test
    void setThroughTheListIsWrittenToTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");

        po.getLineItemList().set(1, lineItem("replaced"));

        assertDocument(po, "i0", "replaced", "i2");
        assertFalse(xml(po).contains("i1"));
    }

    @Test
    void addThroughTheListAppendsToTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1");

        assertTrue(po.getLineItemList().add(lineItem("added")));

        assertDocument(po, "i0", "i1", "added");
    }

    @Test
    void addAtAnIndexInsertsIntoTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");

        po.getLineItemList().add(1, lineItem("inserted"));

        assertDocument(po, "i0", "inserted", "i1", "i2");
    }

    @Test
    void addAllThroughTheListIsWrittenToTheDocument() throws Exception {
        PurchaseOrder po = order("i0");

        po.getLineItemList().addAll(Arrays.asList(lineItem("x"), lineItem("y")));

        assertDocument(po, "i0", "x", "y");
    }

    @Test
    void removeThroughTheListDeletesFromTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");

        po.getLineItemList().remove(1);

        assertDocument(po, "i0", "i2");
        assertFalse(xml(po).contains("i1"));
    }

    @Test
    void clearThroughTheListEmptiesTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2", "i3");

        po.getLineItemList().clear();

        assertDocument(po);
        assertFalse(xml(po).contains("line-item"));
    }

    @Test
    void iteratorRemoveIsWrittenToTheDocument() throws Exception {
        PurchaseOrder po = order("keep0", "drop0", "keep1", "drop1", "keep2");

        for (Iterator<XmlLineItemBean> it = po.getLineItemList().iterator(); it.hasNext(); ) {
            if (it.next().getDescription().startsWith("drop")) {
                it.remove();
            }
        }

        assertDocument(po, "keep0", "keep1", "keep2");
        assertFalse(xml(po).contains("drop"));
    }

    @Test
    void listIteratorSetIsWrittenToTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");

        ListIterator<XmlLineItemBean> it = po.getLineItemList().listIterator();
        it.next();
        it.next();
        it.set(lineItem("second"));

        assertDocument(po, "i0", "second", "i2");
    }

    @Test
    void listIteratorAddIsWrittenToTheDocument() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");

        ListIterator<XmlLineItemBean> it = po.getLineItemList().listIterator();
        it.next();
        it.add(lineItem("between"));

        // the added element goes before the cursor, so the walk carries on with i1
        assertEquals("i1", it.next().getDescription());
        assertDocument(po, "i0", "between", "i1", "i2");
    }

    @Test
    void listIteratorSetAfterPreviousWritesToThatElement() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");

        ListIterator<XmlLineItemBean> it = po.getLineItemList().listIterator();
        it.next();
        it.next();
        assertEquals("i1", it.previous().getDescription());
        it.set(lineItem("backwards"));

        assertDocument(po, "i0", "backwards", "i2");
    }

    @Test
    void theListIsAViewOfTheDocumentNotACopyOfIt() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");
        List<XmlLineItemBean> list = po.getLineItemList();

        po.addNewLineItem().setDescription("late");
        assertEquals(4, list.size());
        assertEquals("late", list.get(3).getDescription());

        po.removeLineItem(0);
        assertEquals(3, list.size());
        assertEquals("i1", list.get(0).getDescription());

        po.insertNewLineItem(0).setDescription("head");
        assertEquals(4, list.size());
        assertEquals("head", list.get(0).getDescription());

        po.getLineItemArray(1).setDescription("edited");
        assertEquals("edited", list.get(1).getDescription());

        List<String> seen = new ArrayList<>();
        for (XmlLineItemBean item : list) {
            seen.add(item.getDescription());
        }
        assertEquals(Arrays.asList("head", "edited", "i2", "late"), seen);
    }

    @Test
    void twoListsOverTheSameBeanSeeEachOthersEdits() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2");
        List<XmlLineItemBean> first = po.getLineItemList();
        List<XmlLineItemBean> second = po.getLineItemList();

        first.remove(0);
        assertEquals(2, second.size());
        assertEquals("i1", second.get(0).getDescription());

        second.add(lineItem("fromSecond"));
        assertEquals(3, first.size());
        assertEquals("fromSecond", first.get(2).getDescription());

        assertDocument(po, "i1", "i2", "fromSecond");
    }

    @Test
    void anEditMadeDuringIterationIsSeenByThatIterator() throws Exception {
        PurchaseOrder po = order("i0", "i1", "i2", "i3");

        List<String> seen = new ArrayList<>();
        for (XmlLineItemBean item : po.getLineItemList()) {
            seen.add(item.getDescription());
            if (seen.size() == 1) {
                // an element the iterator has not reached yet
                po.getLineItemArray(2).setDescription("edited");
            }
        }

        assertEquals(Arrays.asList("i0", "i1", "edited", "i3"), seen);
    }

    // ---- substitution group values, a list of Strings (JavaListObject) ----

    private static RootDocument.Root root(String... values) throws XmlException {
        String[] names = {"A", "B", "C"};
        StringBuilder xml = new StringBuilder("<root xmlns='" + SGS + "'>");
        for (int i = 0; i < values.length; i++) {
            String name = names[i % names.length];
            xml.append('<').append(name).append('>').append(values[i]).append("</").append(name).append('>');
        }
        xml.append("</root>");

        return RootDocument.Factory.parse(xml.toString()).getRoot();
    }

    private static void assertDocument(RootDocument.Root root, String... expected) throws XmlException {
        List<String> wanted = Arrays.asList(expected);

        assertEquals(wanted, Arrays.asList(root.getAArray()));
        assertEquals(expected.length, root.sizeOfAArray());
        assertEquals(wanted, root.getAList());

        RootDocument.Root reparsed = RootDocument.Factory.parse(xml(root)).getRoot();
        assertEquals(wanted, Arrays.asList(reparsed.getAArray()), "the edit did not reach the document");
    }

    @Test
    void setThroughTheValueListIsWrittenToTheDocument() throws Exception {
        RootDocument.Root root = root("v0", "v1", "v2", "v3");

        assertEquals("v1", root.getAList().set(1, "changed"));

        assertDocument(root, "v0", "changed", "v2", "v3");
    }

    @Test
    void addThroughTheValueListAppendsToTheDocument() throws Exception {
        RootDocument.Root root = root("v0", "v1");

        assertTrue(root.getAList().add("v2"));

        assertDocument(root, "v0", "v1", "v2");
    }

    @Test
    void addAtAnIndexInsertsIntoTheValueListsDocument() throws Exception {
        RootDocument.Root root = root("v0", "v1", "v2");

        root.getAList().add(1, "inserted");

        assertDocument(root, "v0", "inserted", "v1", "v2");
    }

    @Test
    void removeThroughTheValueListDeletesFromTheDocument() throws Exception {
        RootDocument.Root root = root("v0", "v1", "v2");

        assertEquals("v1", root.getAList().remove(1));

        assertDocument(root, "v0", "v2");
        assertFalse(xml(root).contains("v1"));
    }

    @Test
    void iteratorRemoveOnTheValueListIsWrittenToTheDocument() throws Exception {
        RootDocument.Root root = root("keep0", "drop0", "keep1", "drop1");

        for (Iterator<String> it = root.getAList().iterator(); it.hasNext(); ) {
            if (it.next().startsWith("drop")) {
                it.remove();
            }
        }

        assertDocument(root, "keep0", "keep1");
        assertFalse(xml(root).contains("drop"));
    }

    @Test
    void listIteratorSetAndAddOnTheValueListAreWrittenToTheDocument() throws Exception {
        RootDocument.Root root = root("v0", "v1", "v2");

        ListIterator<String> it = root.getAList().listIterator();
        it.next();
        it.set("first");
        it.add("second");
        assertEquals("v1", it.next());

        assertDocument(root, "first", "second", "v1", "v2");
    }

    @Test
    void theValueListIsAViewOfTheDocumentNotACopyOfIt() throws Exception {
        RootDocument.Root root = root("v0", "v1", "v2");
        List<String> list = root.getAList();

        root.setAArray(0, "direct");
        assertEquals("direct", list.get(0));

        root.insertA(0, "head");
        assertEquals(4, list.size());
        assertEquals("head", list.get(0));
        assertEquals(Arrays.asList("head", "direct", "v1", "v2"), new ArrayList<>(list));

        root.removeA(3);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList("head", "direct", "v1"), new ArrayList<>(list));
    }
}
