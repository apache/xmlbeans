/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package drtcases;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import com.easypo.XmlLineItemBean;
import com.easypo.XmlShipperBean;

import java.math.BigDecimal;
import java.math.BigInteger;

public class CreationTests extends TestCase
{
    public CreationTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(CreationTests.class); }

    public void testCreatePo() throws Exception
    {
        XmlPurchaseOrderDocumentBean doc =
            XmlPurchaseOrderDocumentBean.Factory.newInstance();
        
        PurchaseOrder order = doc.addNewPurchaseOrder();
        order.addNewCustomer().setName("David Bau");
        order.getCustomer().setAddress("Gladwyne, PA");
        XmlLineItemBean li;
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 1");
        li.setPrice(new BigDecimal("21.79"));
        li.setQuantity(BigInteger.valueOf(2));
        li.setPerUnitOunces(new BigDecimal("5"));
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 2");
        li.setPrice(new BigDecimal("19.89"));
        li.setQuantity(BigInteger.valueOf(2));
        li.setPerUnitOunces(new BigDecimal("5"));
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 3");
        li.setPrice(new BigDecimal("19.89"));
        li.setQuantity(BigInteger.valueOf(1));
        li.setPerUnitOunces(new BigDecimal("5"));
        XmlShipperBean sh = order.addNewShipper();
        sh.setName("UPS");
        sh.setPerOunceRate(new BigDecimal("0.74"));

//        System.out.println(doc.xmlText());

        Assert.assertEquals("David Bau", order.getCustomer().getName());
        Assert.assertEquals("Gladwyne, PA", order.getCustomer().getAddress());
        Assert.assertEquals(3, order.sizeOfLineItemArray());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 1", order.getLineItemArray(0).getDescription());
        Assert.assertEquals(new BigDecimal("21.79"), order.getLineItemArray(0).getPrice());
        Assert.assertEquals(new BigInteger("2"), order.getLineItemArray(0).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), order.getLineItemArray(0).getPerUnitOunces());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 2", order.getLineItemArray(1).getDescription());
        Assert.assertEquals(new BigDecimal("19.89"), order.getLineItemArray(1).getPrice());
        Assert.assertEquals(new BigInteger("2"), order.getLineItemArray(1).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), order.getLineItemArray(1).getPerUnitOunces());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 3", order.getLineItemArray(2).getDescription());
        Assert.assertEquals(new BigDecimal("19.89"), order.getLineItemArray(2).getPrice());
        Assert.assertEquals(new BigInteger("1"), order.getLineItemArray(2).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), order.getLineItemArray(2).getPerUnitOunces());

        Assert.assertEquals(true, order.isSetShipper());
        Assert.assertEquals("UPS", order.getShipper().getName());
        Assert.assertEquals(new BigDecimal("0.74"), order.getShipper().getPerOunceRate());
    }
}
