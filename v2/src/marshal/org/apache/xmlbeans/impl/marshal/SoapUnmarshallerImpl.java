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

import org.apache.xmlbeans.EncodingStyle;
import org.apache.xmlbeans.SoapUnmarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

final class SoapUnmarshallerImpl
    implements SoapUnmarshaller
{
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;
    private final EncodingStyle encodingStyle;
    private final RefObjectTable refObjectTable = new RefObjectTable();


    SoapUnmarshallerImpl(BindingLoader loader,
                         RuntimeBindingTypeTable typeTable,
                         EncodingStyle encodingStyle)
    {
        assert loader != null;
        assert typeTable != null;
        assert encodingStyle != null;

        this.bindingLoader = loader;
        this.typeTable = typeTable;
        this.encodingStyle = encodingStyle;
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
        if (!reader.isStartElement()) {
            throw new IllegalStateException("reader must be positioned on a start element");
        }

        final SoapUnmarshalResult result;
        if (EncodingStyle.SOAP11 == encodingStyle) {
            result = new Soap11UnmarshalResult(bindingLoader, typeTable, refObjectTable, options);
        } else if (EncodingStyle.SOAP12 == encodingStyle) {
            throw new AssertionError("soap 12 UNIMP: " + encodingStyle);
        } else {
            throw new AssertionError("unknown encoding style: " + encodingStyle);
        }


        return result.unmarshalType(reader, schemaType, javaType);
    }
}
