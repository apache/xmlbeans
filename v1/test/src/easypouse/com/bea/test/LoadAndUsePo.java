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

package com.bea.test;

import xml.util.*;
import org.openuri.easypo.*;

import java.io.File;

public class LoadAndUsePo
{
    public static void main(String[] args) throws Exception
    {
        for (int i = 0; i < args.length; i++)
        {
            System.out.println("======================");
            System.out.println("Loading file " + args[i]);
            summarizePoDocument(PurchaseOrderDocument.Factory.parse(new File(args[i])));
        }
    }

    public static void summarizePoDocument(PurchaseOrderDocument doc)
    {
        PurchaseOrder order = doc.getPurchaseOrder();
        System.out.println("Customer: " + order.getCustomer().getName());
        System.out.println("Address:  " + order.getCustomer().getAddress());
        LineItem[] item = order.getLineItem();
        for (int i = 0; i < item.length; i++)
        {
            System.out.println("----------------------");
            System.out.println("Item description: " + item[i].getDescription());
            System.out.println("  Price: " + item[i].getPrice());
            System.out.println("  Quantity: " + item[i].getQuantity());
            System.out.println("  Ounces: " + item[i].getPerUnitOunces());
            System.out.println("  ----------------");
            System.out.println("  Amount: " + item[i].getAmount());
            System.out.println("  Weight: " + item[i].getLineItemOunces());
        }
        System.out.println("----------------------");
        if (order.isSetShipper())
        {
            System.out.println("Shipper: " + order.getShipper().getName());
            System.out.println("  Per-ounce rate: " + order.getShipper().getPerOunceRate());
        }
        System.out.println("----------------------");
        System.out.println("Total weight in ounces: " + order.getTotalOunces());
        System.out.println("Shipping amount: " + order.getShippingAmount());
        System.out.println("Total amount: " + order.getTotalAmount());
    }
}