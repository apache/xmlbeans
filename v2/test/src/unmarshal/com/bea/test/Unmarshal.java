/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
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

import weblogic.xml.stream.XMLInputStream;

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
