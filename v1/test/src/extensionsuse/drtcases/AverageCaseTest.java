package drtcases;

import interfaceFeature.xbean.averageCase.purchaseOrder.PurchaseOrderDocument;
import interfaceFeature.xbean.averageCase.purchaseOrder.PurchaseOrderType;
import interfaceFeature.xbean.averageCase.purchaseOrder.Items;

import junit.framework.*;

import java.math.BigDecimal;


public class AverageCaseTest extends TestCase
{
    public AverageCaseTest(String s)
    {
        super(s);
    }

    public static Test suite()
    {
        return new TestSuite(AverageCaseTest.class);
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
            it[i].setUSPrice(new BigDecimal("" + 2));
        }
        Items items = Items.Factory.newInstance();
        items.setItemArray(it);
        po.setItems(items);
        // System.out.println("poDoc: " + poDoc);

        int i = poDoc.getTotal();
//20 items @ $2
        assertEquals(40, i);
    }
}
