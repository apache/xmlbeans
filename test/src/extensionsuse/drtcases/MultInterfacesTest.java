package drtcases;

import interfaceFeature.xbean.multInterfaces.purchaseOrder.PurchaseOrderDocument;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.PurchaseOrderType;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.Items;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.Items.Item;

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;

import junit.framework.*;

public class MultInterfacesTest extends TestCase
{
    public MultInterfacesTest(String s)
    {
        super(s);
    }

    public static Test suite()
    {
        return new TestSuite(MultInterfacesTest.class);
    }

    public void test()
    {


        PurchaseOrderDocument poDoc = null;

        poDoc = PurchaseOrderDocument.Factory.newInstance();
        PurchaseOrderType po = poDoc.addNewPurchaseOrder();
        int LEN = 20;

        Items.Item[] it = new Items.Item[LEN];
        for (int i = 0; i < LEN; i++)
        {
            it[i] = Items.Item.Factory.newInstance();
            it[i].setUSPrice(new BigDecimal("" + i));
        }
        Items items = Items.Factory.newInstance();
        items.setItemArray(it);
        po.setItems(items);

        StringBuffer sb = new StringBuffer();
        sb.append("<pur:purchaseOrder xmlns:pur=\"http://xbean.interface_feature/multInterfaces/PurchaseOrder\">");

        sb.append("<pur:items>");

        StringBuffer sbContent = new StringBuffer();
        for (int i = 0; i < LEN; i++)
            sbContent.append("<pur:item><pur:USPrice>" + i + "</pur:USPrice></pur:item>");

        int pos = sb.length();
        sb.append("</pur:items></pur:purchaseOrder>");

        String sExpected = sb.subSequence(0, pos) + sbContent.toString() + sb.subSequence(pos, sb.length());
        assertEquals(sExpected, poDoc.xmlText());


        assertEquals(0, poDoc.getMinPrice());
        int price = 10;

        poDoc.setMinPrice((double) price);

        sbContent = new StringBuffer();
        for (int i = 0; i < LEN; i++)
            if (i < price)
                sbContent.append("<pur:item><pur:USPrice>" + price + "</pur:USPrice></pur:item>");
            else
                sbContent.append("<pur:item><pur:USPrice>" + i + "</pur:USPrice></pur:item>");
        sExpected = sb.subSequence(0, pos) + sbContent.toString() + sb.subSequence(pos, sb.length());
        assertEquals(sExpected, poDoc.xmlText());

        assertEquals(price, poDoc.getMinPrice());

        int expTotal = (price - 1) * price + (price + 1 + LEN) * (LEN - price) / 2;
        assertEquals(expTotal, poDoc.getTotal());

        XmlObject item = poDoc.getCheapestItem();

        Item expected = it[0];
        expected.setUSPrice(new BigDecimal(30d));
        //       assertEquals(expected, item );
    }

}
