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

import org.apache.xmlbeans.unmarshal.MessagePlan;
import org.apache.xmlbeans.unmarshal.UnmarshalPlan;
import org.apache.xmlbeans.unmarshal.SimpleTypePlan;
import org.apache.xmlbeans.unmarshal.StructurePlan;
import org.apache.xmlbeans.unmarshal.UnmarshalContext;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;

import java.io.File;

import org.apache.xmlbeans.xml.stream.XMLInputStream;

public class Unmarshal
{
    public static void main(String args[]) throws Exception
    {
        MessagePlan messagePlan = new MessagePlan(2);

        SimpleTypePlan param1Plan = new SimpleTypePlan(SimpleTypePlan.JAVA_FLOAT, true, SchemaType.BTC_DOUBLE, null, 0);

        StructurePlan param2Plan = new StructurePlan();

        param2Plan.setTargetClass(com.bea.test.TestStructure.class);
        param2Plan.setInfoForElement(new QName("varInt"), new StructurePlan.FieldInfo("varInt", false, "com.bea.test.TestStructure", null, StructurePlan.FieldInfo.SINGLETON, new SimpleTypePlan(SimpleTypePlan.JAVA_INT, true, SchemaType.BTC_INTEGER, null, 0)));
        param2Plan.setInfoForElement(new QName("varFloat"), new StructurePlan.FieldInfo("varFloat", false, "com.bea.test.TestStructure", null, StructurePlan.FieldInfo.SINGLETON, new SimpleTypePlan(SimpleTypePlan.JAVA_FLOAT, true, SchemaType.BTC_FLOAT, null, 0)));
        param2Plan.setInfoForElement(new QName("varString"), new StructurePlan.FieldInfo("varString", false, "com.bea.test.TestStructure", null, StructurePlan.FieldInfo.SINGLETON, new SimpleTypePlan(SimpleTypePlan.JAVA_STRING, false, SchemaType.BTC_STRING, null, 0)));

        messagePlan.setInfoForElement(new QName("LastTradePrice"), new MessagePlan.ParamInfo(0, MessagePlan.ParamInfo.SINGLETON, param1Plan));
        messagePlan.setInfoForElement(new QName("inputStruct"), new MessagePlan.ParamInfo(1, MessagePlan.ParamInfo.SINGLETON, param2Plan));

        XmlObject xobj = XmlObject.Factory.parse(new File(args[0]));
        XMLInputStream xinput = xobj.newXMLInputStream();
        while (xinput.hasNext())
        {
            if (xinput.peek().isStartElement())
                break;
            xinput.skip();
        }

        UnmarshalContext context = new UnmarshalContext();
        Object[] result = (Object[])messagePlan.unmarshal(xinput, context);
        context.complete();

        for (int i = 0; i < result.length; i++)
        {
            System.out.println("Result " + i + " = " + result[i]);
            if (result[i] instanceof TestStructure)
            {
                TestStructure s = (TestStructure)result[i];
                System.out.println("  varInt = " + s.varInt);
                System.out.println("  varFloat = " + s.varFloat);
                System.out.println("  varString = " + s.varString);
            }
        }
    }
}
