package xmlobject.detailed;

import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.detailed.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlCustomerBean;
import org.apache.xmlbeans.XmlCursor;

/**
 * Date: Sep 10, 2004
 * Time: 10:31:36 AM
 *
 * @owner ykadiysk
 */
public class TypedObjectCursor extends TestCase{
    public TypedObjectCursor(String name){
        super(name);
    }
     public static Test suite() {
        return new TestSuite(TypedObjectCursor.class);
    }

    public void testObjectCursor(){
    XmlPurchaseOrderDocumentBean.PurchaseOrder po= XmlPurchaseOrderDocumentBean.PurchaseOrder.Factory.newInstance();
    XmlCursor cur=po.newCursor();
    XmlCustomerBean cust=po.addNewCustomer();
    cust.setAddress("123 Fake Street");
    cust.setAge(23);
    cust.setName("Lisa Simpson");
    cur.toFirstContentToken();
    assertEquals(XmlCursor.TokenType.START, cur.currentTokenType());
      assertEquals(XmlCursor.TokenType.ATTR,cur.toNextToken());
        assertEquals("<xml-fragment age=\"23\"/>", cur.xmlText());
            assertEquals(XmlCursor.TokenType.START,cur.toNextToken());
         assertEquals(XmlCursor.TokenType.TEXT,cur.toNextToken());
        assertEquals("<xml-fragment>Lisa Simpson</xml-fragment>", cur.xmlText());
           assertEquals(XmlCursor.TokenType.END,cur.toNextToken());
            assertEquals(XmlCursor.TokenType.START,cur.toNextToken());
         assertEquals(XmlCursor.TokenType.TEXT,cur.toNextToken());
        assertEquals("<xml-fragment>123 Fake Street</xml-fragment>", cur.xmlText());
        cur.toPrevToken();
        cur.setTextValue("456".toCharArray(),0,3);
}
}
