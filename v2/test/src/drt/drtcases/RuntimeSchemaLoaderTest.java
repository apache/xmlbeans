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

package drtcases;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.SchemaTypeLoader;

import javax.xml.namespace.QName;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3.x2001.xmlSchema.SchemaDocument.Schema;

public class RuntimeSchemaLoaderTest extends TestCase
{
    public RuntimeSchemaLoaderTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(RuntimeSchemaLoaderTest.class); }

    public void testDynamicLoad() throws Throwable
    {
        File inputfile1 = TestEnv.xbeanCase("schema/dynamic/dyntest.xsd");
        SchemaTypeLoader loader = XmlBeans.loadXsd(new XmlObject[] { XmlObject.Factory.parse(inputfile1) });
        XmlObject result = loader.parse(TestEnv.xbeanCase("schema/dynamic/dyntest.xml"), null, null);
        Assert.assertEquals("D=wrappedinstance@http://openuri.org/test/dyntest", result.schemaType().toString());
        Assert.assertEquals(loader.findDocumentType(new QName("http://openuri.org/test/dyntest", "wrappedinstance" )),
                            result.schemaType());
    }

    public void testDynamicLoad2() throws Throwable
    {
        File inputfile1 = TestEnv.xbeanCase("schema/dynamic/dyntest2.xsd");
        SchemaTypeLoader loader = XmlBeans.loadXsd(new XmlObject[] { XmlObject.Factory.parse(inputfile1) });
        XmlObject result = loader.parse(TestEnv.xbeanCase("schema/dynamic/dyntest2.xml"), null, null);
        Assert.assertEquals("D=wrappedwildcard@http://openuri.org/test/dyntest", result.schemaType().toString());
        Assert.assertEquals(loader.findDocumentType(new QName("http://openuri.org/test/dyntest", "wrappedwildcard")),
                            result.schemaType());
        XmlCursor cur = result.newCursor();
        Assert.assertTrue("Should have a root element", cur.toFirstChild());
        result = cur.getObject();
        Assert.assertEquals("E=wrappedwildcard|D=wrappedwildcard@http://openuri.org/test/dyntest", result.schemaType().toString());
        Assert.assertEquals(loader.findElement(new QName("http://openuri.org/test/dyntest", "wrappedwildcard")).getType(),
                            result.schemaType());
        Assert.assertTrue("Should have a first child", cur.toFirstChild());
        Assert.assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "schema"), cur.getName());
        XmlObject obj = cur.getObject();
        Assert.assertEquals(Schema.type, obj.schemaType());
    }
}
