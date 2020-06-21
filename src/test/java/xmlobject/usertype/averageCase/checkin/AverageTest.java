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

package xmlobject.usertype.averageCase.checkin;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.Test;
import usertype.xbean.averageCase.purchaseOrder.Items;
import usertype.xbean.averageCase.purchaseOrder.PurchaseOrderDocument;
import usertype.xbean.averageCase.purchaseOrder.PurchaseOrderType;
import xmlobject.usertype.averageCase.existing.SKU;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class AverageTest {

    @Test
    public void test(){
        PurchaseOrderDocument poDoc = PurchaseOrderDocument.Factory.newInstance();
        PurchaseOrderType po=poDoc.addNewPurchaseOrder();
        int LEN=20;

        Items.Item[] it= new Items.Item[LEN];
        for (int i=0; i< LEN; i++){
            it[i]=Items.Item.Factory.newInstance();
            it[i].setUSPrice(new BigDecimal(""+ 2 ));
            it[i].setPartNum(new SKU(i, "AB"));
        }
        Items items= Items.Factory.newInstance();
        items.setItemArray(it);
        po.setItems(items);

        for (int i=0; i< LEN; i++){
            assertEquals(i, it[i].getPartNum().getDigits());
            assertEquals("AB", it[i].getPartNum().getLetters());
        }
    }

    @Test(expected = XmlValueOutOfRangeException.class)
    public void testBadInput() throws XmlException{
        String sb =
            "<purchaseOrder xmlns=\"http://xbean.usertype/averageCase/PurchaseOrder\">" +
            "<items><item partNum=\"000-AB\"><USPrice>2</USPrice></item>" +
            "<item partNum=\"0013-AB\"><USPrice>2</USPrice></item>" +
            "</items></purchaseOrder>";
        PurchaseOrderDocument poDocument = PurchaseOrderDocument.Factory.parse(sb);

        PurchaseOrderType po = poDocument.getPurchaseOrder();

        Items.Item[] it = po.getItems().getItemArray();
        assertEquals(2, it.length);


        SKU sku = it[0].getPartNum();

        assertEquals(0, sku.getDigits());
        assertEquals("AB", sku.getLetters());

        it[1].getPartNum();
        // Invalid SKU format should fail
    }
}
