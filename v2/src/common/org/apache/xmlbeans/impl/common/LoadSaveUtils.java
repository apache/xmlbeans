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
 * Date: Nov 11, 2003
 */
package org.apache.xmlbeans.impl.common;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;


public class LoadSaveUtils
{
    public static Document xmlText2GenericDom(InputStream is, Document emptyDoc)
            throws SAXException, ParserConfigurationException, IOException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();

        Sax2Dom handler = new Sax2Dom(emptyDoc);

        parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        parser.parse(is, handler);

        return (Document) handler.getDOM();
    }

    public static void xmlStreamReader2XmlText(XMLStreamReader xsr, OutputStream os)
            throws XMLStreamException
    {
        XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(os);

        while (xsr.hasNext())
        {
            switch( xsr.getEventType() )
            {
                case XMLStreamReader.ATTRIBUTE:
                    xsw.writeAttribute(xsr.getPrefix(), xsr.getNamespaceURI(), xsr.getLocalName(), xsr.getText());
                    break;

                case XMLStreamReader.CDATA:
                    xsw.writeCData(xsr.getText());
                    break;

                case XMLStreamReader.CHARACTERS:
                    xsw.writeCharacters(xsr.getText());
                    break;

                case XMLStreamReader.COMMENT:
                    xsw.writeComment(xsr.getText());
                    break;

                case XMLStreamReader.DTD:
                    xsw.writeDTD(xsr.getText());
                    break;

                case XMLStreamReader.END_DOCUMENT:
                    xsw.writeEndDocument();
                    break;

                case XMLStreamReader.END_ELEMENT:
                    xsw.writeEndElement();
                    break;

                case XMLStreamReader.ENTITY_DECLARATION:
                    break;

                case XMLStreamReader.ENTITY_REFERENCE:
                    xsw.writeEntityRef(xsr.getText());
                    break;

                case XMLStreamReader.NAMESPACE:
                    xsw.writeNamespace(xsr.getPrefix(), xsr.getNamespaceURI());
                    break;

                case XMLStreamReader.NOTATION_DECLARATION:
                    break;

                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    xsw.writeProcessingInstruction(xsr.getPITarget(), xsr.getPIData());
                    break;

                case XMLStreamReader.SPACE:
                    xsw.writeCharacters(xsr.getText());
                    break;

                case XMLStreamReader.START_DOCUMENT:
                    xsw.writeStartDocument();
                    break;

                case XMLStreamReader.START_ELEMENT:
                    xsw.writeStartElement(xsr.getPrefix()==null ? "" : xsr.getPrefix(), xsr.getLocalName(), xsr.getNamespaceURI());

                    int attrs = xsr.getAttributeCount();
                    for ( int i = attrs-1; i>=0; i--)
                    {
                        xsw.writeAttribute(xsr.getAttributePrefix(i)==null ? "" : xsr.getAttributePrefix(i), xsr.getAttributeNamespace(i), xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                    }

                    int nses = xsr.getNamespaceCount();
                    for ( int i = 0; i<nses; i++)
                    {
                        xsw.writeNamespace(xsr.getNamespacePrefix(i), xsr.getNamespaceURI(i));
                    }
                    break;
            }
            xsr.next();
        }
        xsw.flush();
    }
}
