package drtcases;

import interfaceFeature.xbean.fixedAttrBean.purchaseOrder.PurchaseOrderDocument;
import interfaceFeature.xbean.fixedAttrBean.purchaseOrder.PurchaseOrderType;
import interfaceFeature.xbean.fixedAttrBean.purchaseOrder.Items;


import java.math.BigDecimal;

import junit.framework.*;

public class FixedAttrTest extends TestCase
{
    public FixedAttrTest(String s)
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

        StringBuffer sb = new StringBuffer();
        sb.append("<pur:purchaseOrder xmlns:pur=\"http://xbean.interface_feature/fixedAttrBean/PurchaseOrder\">");

        sb.append("<pur:items>");

        StringBuffer sbContent = new StringBuffer();
        for (int i = 0; i < LEN; i++)
            sbContent.append("<pur:item><pur:USPrice>4</pur:USPrice></pur:item>");

        int pos = sb.length();
        sb.append("</pur:items></pur:purchaseOrder>");

        String sExpected = sb.subSequence(0, pos) + sbContent.toString() + sb.subSequence(pos, sb.length());

        assertEquals(sExpected, poDoc.xmlText());

        try
        {
            poDoc.setPrice(10);

        }
        catch (Exception t)
        {
            t.printStackTrace(System.err);
            System.exit(-1);
        }


        assertTrue(!poDoc.validate());
    }

}
