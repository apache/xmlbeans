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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

class UnmarshallerImpl implements Unmarshaller
{
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;

    private static final XMLInputFactory XML_INPUT_FACTORY =
        XMLInputFactory.newInstance();

    public UnmarshallerImpl(BindingLoader loader,
                            RuntimeBindingTypeTable typeTable)
    {
        assert loader != null;
        assert typeTable != null;

        this.bindingLoader = loader;
        this.typeTable = typeTable;
    }

    public Object unmarshal(XMLStreamReader reader)
        throws XmlException
    {
        return unmarshal(reader, null);
    }

    public Object unmarshal(XMLStreamReader reader, XmlOptions options)
        throws XmlException
    {
        final UnmarshalResult result =
            new UnmarshalResult(bindingLoader, typeTable, options);

        return result.unmarshal(reader);
    }

    public Object unmarshal(InputStream doc)
        throws XmlException
    {
        return unmarshal(doc, null);
    }

    public Object unmarshal(InputStream doc, XmlOptions options)
        throws XmlException
    {
        if (doc == null) throw new IllegalArgumentException("null inputStream");

        try {
            final XMLStreamReader reader =
                XML_INPUT_FACTORY.createXMLStreamReader(doc);
            return unmarshal(reader, options);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    public Object unmarshalType(XMLStreamReader reader,
                                QName schemaType,
                                String javaType)
        throws XmlException
    {
        return unmarshalType(reader, schemaType, javaType, null);
    }

    public Object unmarshalType(XMLStreamReader reader,
                                QName schemaType,
                                String javaType,
                                XmlOptions options)
        throws XmlException
    {
        if (reader == null) throw new IllegalArgumentException("null reader");
        if (schemaType == null) throw new IllegalArgumentException("null schemaType");
        if (javaType == null) throw new IllegalArgumentException("null javaType");

        final UnmarshalResult result =
            new UnmarshalResult(bindingLoader, typeTable, options);

        return result.unmarshalType(reader, schemaType, javaType);
    }

    XMLInputFactory getXmlInputFactory()
    {
        return XML_INPUT_FACTORY;
    }
}
