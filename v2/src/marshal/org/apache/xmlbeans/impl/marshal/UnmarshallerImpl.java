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
