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

/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Nov 13, 2003
 */
package drtcases;

import org.w3c.dom.Document;
import org.apache.xmlbeans.impl.common.LoadSaveUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;

public class SaveLoadTests extends TestCase
{
    public SaveLoadTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(SaveLoadTests.class); }

    public void testLoadSave()
            throws IOException, SAXException, ParserConfigurationException, XmlException, XMLStreamException
    {
        File file = TestEnv.xbeanCase("xpath/testXPath.xml");

        Document doc = LoadSaveUtils.xmlText2GenericDom(new FileInputStream(file),
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        XmlObject xo = XmlObject.Factory.parse(doc);

        XMLStreamReader xsr = xo.newXMLStreamReader();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LoadSaveUtils.xmlStreamReader2XmlText(xsr, bos);

        check( XmlObject.Factory.parse(TestEnv.xbeanCase("xpath/testXPath.xml")).toString(), bos.toString() );
    }

    private static void check(String expected, String actual) throws XmlException
    {
        XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
        boolean match = XmlComparator.lenientlyCompareTwoXmlStrings(actual, expected, diag);
        Assert.assertTrue("------------  Found difference:" +
                " actual=\n'" + actual + "'\nexpected=\n'" + expected + "'\ndiagnostic=" + diag , match);
    }
}
