package drtcases.prePostFeature.readOnlyBean;

import prePostFeature.xbean.readOnlyBean.purchaseOrder.PurchaseOrderDocument;
import prePostFeature.xbean.readOnlyBean.purchaseOrder.PurchaseOrderType;
import prePostFeature.xbean.readOnlyBean.purchaseOrder.Items;


import java.math.BigDecimal;

import junit.framework.*;
import drtcases.FixedAttrTest;

public class ReadOnlyTest extends TestCase
{
    public ReadOnlyTest(String s)
    {
        super(s);
    }

    public static Test suite()
    {
        return new TestSuite(FixedAttrTest.class);
    }

    public void test()
    {
        PurchaseOrderDocument poDoc;

        poDoc = PurchaseOrderDocument.Factory.newInstance();
        PurchaseOrderType po = poDoc.addNewPurchaseOrder();


        int LEN = 20;

        Items.Item[] it = new Items.Item[LEN];
        for (int i = 0; i < LEN; i++)
        {
            it[i] = Items.Item.Factory.newInstance();
            it[i].setUSPrice(new BigDecimal("" + 4));
        }
        Items items = Items.Factory.newInstance();
        items.setItemArray(it);
        po.setItems(items);


        String sExpected = "<pur:purchaseOrder xmlns:pur=\"http://xbean.prePost_feature/readOnlyBean/PurchaseOrder\"/>";


        it[0].setPrice(10);

        assertEquals(sExpected, poDoc.xmlText());

        assertTrue(!poDoc.validate());
    }
}
