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
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import org.openuri.nameworld.Loc;
import org.openuri.nameworld.NameworldDocument;
import org.openuri.nameworld.NameworldDocument.Nameworld;
import org.openuri.sgs.RootDocument;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The indexed getXxxArray(i) accessors resolve an index by position within the
 * children of the parent element. These check that they land on the right
 * element whatever order the indexes are asked for in, and that they notice
 * when the document changes underneath them.
 */
class IndexedElementAccessTest {
    private static final String SGS = "http://openuri.org/sgs";
    private static final int COUNT = 64;

    private static PurchaseOrder order(int items) throws XmlException {
        StringBuilder xml = new StringBuilder("<purchase-order xmlns='http://openuri.org/easypo'>");
        for (int i = 0; i < items; i++) {
            xml.append("<line-item><description>item").append(i).append("</description></line-item>");
        }
        xml.append("</purchase-order>");

        return XmlPurchaseOrderDocumentBean.Factory.parse(xml.toString()).getPurchaseOrder();
    }

    /** A root whose children cycle through the A/B/C substitution group, so the accessor matches on a QNameSet. */
    private static RootDocument.Root substitutionGroupRoot(int children) throws XmlException {
        String[] names = {"A", "B", "C"};
        StringBuilder xml = new StringBuilder();
        xml.append("<root xmlns='").append(SGS).append("'>");
        for (int i = 0; i < children; i++) {
            String name = names[i % names.length];
            xml.append('<').append(name).append('>').append("v").append(i).append("</").append(name).append('>');
        }
        xml.append("</root>");

        return RootDocument.Factory.parse(xml.toString()).getRoot();
    }

    private static int[] shuffled(int n) {
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }

        Random random = new Random(42);
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int t = order[i];
            order[i] = order[j];
            order[j] = t;
        }

        return order;
    }

    private static final String NW = "http://openuri.org/nameworld";

    /** Three levels of repeated elements, so that a pass over it walks nested parents. */
    private static Nameworld nameworld(int islands, int locations, int references) throws XmlException {
        StringBuilder xml = new StringBuilder();
        xml.append("<nameworld xmlns='").append(NW).append("' xmlns:nw='").append(NW).append("'>");
        for (int i = 0; i < islands; i++) {
            xml.append("<island targetNamespace='is").append(i).append("'>");
            for (int l = 0; l < locations; l++) {
                xml.append("<location name='is").append(i).append("-loc").append(l).append("'>");
                for (int r = 0; r < references; r++) {
                    xml.append("<reference to='nw:r").append(r).append("'/>");
                }
                xml.append("</location>");
            }
            xml.append("</island>");
        }
        xml.append("</nameworld>");

        return NameworldDocument.Factory.parse(xml.toString()).getNameworld();
    }

    @Test
    void indexedAccessAgreesWithTheBulkArrayInAnyOrder() throws Exception {
        PurchaseOrder po = order(COUNT);
        assertEquals(COUNT, po.sizeOfLineItemArray());

        for (int i = 0; i < COUNT; i++) {
            assertEquals("item" + i, po.getLineItemArray(i).getDescription());
        }

        for (int i = COUNT - 1; i >= 0; i--) {
            assertEquals("item" + i, po.getLineItemArray(i).getDescription());
        }

        for (int i : shuffled(COUNT)) {
            assertEquals("item" + i, po.getLineItemArray(i).getDescription());
        }

        // the same index twice in a row, and the two ends alternating
        assertEquals("item7", po.getLineItemArray(7).getDescription());
        assertEquals("item7", po.getLineItemArray(7).getDescription());
        for (int i = 0; i < 8; i++) {
            assertEquals("item" + i, po.getLineItemArray(i).getDescription());
            assertEquals("item" + (COUNT - 1 - i), po.getLineItemArray(COUNT - 1 - i).getDescription());
        }
    }

    @Test
    void indexedAccessFollowsTheDocumentAsItChanges() throws Exception {
        PurchaseOrder po = order(8);
        assertEquals("item3", po.getLineItemArray(3).getDescription());

        po.removeLineItem(0);
        assertEquals("item4", po.getLineItemArray(3).getDescription());

        po.insertNewLineItem(0).setDescription("head");
        assertEquals("head", po.getLineItemArray(0).getDescription());
        assertEquals("item3", po.getLineItemArray(3).getDescription());

        po.getLineItemArray(2).setDescription("changed");
        assertEquals("changed", po.getLineItemArray(2).getDescription());
    }

    @Test
    void aNegativeIndexStillResolvesToTheFirstElement() throws Exception {
        // not obviously the right answer, but it is what this accessor has always done
        PurchaseOrder po = order(4);
        assertEquals("item0", po.getLineItemArray(-1).getDescription());
    }

    @Test
    void substitutionGroupAccessorsAreIndexedCorrectly() throws Exception {
        RootDocument.Root root = substitutionGroupRoot(COUNT);
        assertEquals(COUNT, root.sizeOfAArray());

        for (int i = 0; i < COUNT; i++) {
            assertEquals("v" + i, root.getAArray(i));
        }

        for (int i = COUNT - 1; i >= 0; i--) {
            assertEquals("v" + i, root.getAArray(i));
        }

        for (int i : shuffled(COUNT)) {
            assertEquals("v" + i, root.getAArray(i));
        }
    }

    @Test
    void aLookupByNameDoesNotDisturbALookupBySubstitutionGroup() throws Exception {
        // the accessor matches any of A/B/C by QNameSet while the cursor matches B by name,
        // both against the same parent element
        RootDocument.Root root = substitutionGroupRoot(COUNT);
        QName b = new QName(SGS, "B");

        // every third child is a B, and they are asked for out of step with the A indexes
        int bs = (COUNT + 1) / 3;

        try (XmlCursor cursor = root.newCursor()) {
            for (int i = 0; i < COUNT; i++) {
                assertEquals("v" + i, root.getAArray(i));

                int nth = i % bs;
                cursor.push();
                assertTrue(cursor.toChild(b, nth));
                assertEquals("v" + (nth * 3 + 1), cursor.getTextValue());
                cursor.pop();
            }
        }
    }

    @Test
    void listIterationAgreesWithTheArray() throws Exception {
        PurchaseOrder po = order(COUNT);

        int i = 0;
        for (XmlLineItemBean item : po.getLineItemList()) {
            assertEquals("item" + i++, item.getDescription());
        }

        assertEquals(COUNT, i);
        assertEquals(COUNT, po.getLineItemList().size());
    }

    @Test
    void listIterationSeesElementsAddedWhileItIsRunning() throws Exception {
        PurchaseOrder po = order(2);

        List<String> seen = new ArrayList<>();
        for (XmlLineItemBean item : po.getLineItemList()) {
            seen.add(item.getDescription());
            if (seen.size() == 1) {
                po.addNewLineItem().setDescription("added");
            }
        }

        assertEquals(Arrays.asList("item0", "item1", "added"), seen);
    }

    @Test
    void removingThroughTheIteratorUpdatesTheDocument() throws Exception {
        PurchaseOrder po = order(4);

        Iterator<XmlLineItemBean> it = po.getLineItemList().iterator();
        assertEquals("item0", it.next().getDescription());
        assertEquals("item1", it.next().getDescription());
        it.remove();
        assertEquals("item2", it.next().getDescription());
        assertEquals("item3", it.next().getDescription());

        assertEquals(3, po.sizeOfLineItemArray());
        assertEquals("item2", po.getLineItemArray(1).getDescription());
    }

    @Test
    void nestedIndexedAccessKeepsEachLevelOnItsOwnParent() throws Exception {
        // The store caches the last child it returned per parent. An inner level must not be able
        // to take the entry the level above it is resting on, or the outer walk restarts every time.
        Nameworld world = nameworld(12, 6, 4);
        assertEquals(12, world.sizeOfIslandArray());

        for (int i = 0; i < 12; i++) {
            Nameworld.Island island = world.getIslandArray(i);
            assertEquals("is" + i, island.getTargetNamespace());
            assertEquals(6, island.sizeOfLocationArray());

            for (int l = 0; l < 6; l++) {
                Loc location = island.getLocationArray(l);
                assertEquals("is" + i + "-loc" + l, location.getName());
                assertEquals(4, location.sizeOfReferenceArray());

                for (int r = 0; r < 4; r++) {
                    assertEquals("r" + r, location.getReferenceArray(r).getTo().getLocalPart());
                }
            }
        }
    }

    @Test
    void nestedListIterationKeepsEachLevelOnItsOwnParent() throws Exception {
        Nameworld world = nameworld(12, 6, 4);

        int islands = 0;
        for (Nameworld.Island island : world.getIslandList()) {
            assertEquals("is" + islands, island.getTargetNamespace());

            int locations = 0;
            for (Loc location : island.getLocationList()) {
                assertEquals("is" + islands + "-loc" + locations, location.getName());

                int references = 0;
                for (Loc.Reference reference : location.getReferenceList()) {
                    assertEquals("r" + references++, reference.getTo().getLocalPart());
                }
                assertEquals(4, references);

                locations++;
            }
            assertEquals(6, locations);

            islands++;
        }
        assertEquals(12, islands);
    }

    @Test
    void aNestedWalkLeavesTheOuterLevelResumable() throws Exception {
        // walk the outer level forwards, dipping into the inner level between steps, then read the
        // outer level again out of order - a cache the inner walk corrupted would answer wrongly
        Nameworld world = nameworld(16, 4, 2);

        for (int i = 0; i < 16; i++) {
            assertEquals("is" + i, world.getIslandArray(i).getTargetNamespace());
            assertEquals("is" + i + "-loc3", world.getIslandArray(i).getLocationArray(3).getName());
        }

        for (int i : shuffled(16)) {
            assertEquals("is" + i, world.getIslandArray(i).getTargetNamespace());
        }
    }
}
