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
import org.apache.xmlbeans.impl.marshal.util.AttrCache;
import org.apache.xmlbeans.impl.newstore2.Public2;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

final class SoapUnmarshallerImpl
    implements SoapUnmarshaller, StreamRefNavigator

{
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;
    private final EncodingStyle encodingStyle;
    private final RefObjectTable refObjectTable = new RefObjectTable();
    private final Node referenceRoot;
    private AttrCache attrCache;


    SoapUnmarshallerImpl(BindingLoader loader,
                         RuntimeBindingTypeTable typeTable,
                         EncodingStyle encodingStyle,
                         Node reference_root)
    {
        assert loader != null;
        assert typeTable != null;
        assert encodingStyle != null;
        assert reference_root != null;

        this.bindingLoader = loader;
        this.typeTable = typeTable;
        this.encodingStyle = encodingStyle;
        this.referenceRoot = reference_root;
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

        final SoapUnmarshalResult result = createMarshalResult(options);
        return result.unmarshalType(reader, schemaType, javaType);
    }

    private SoapUnmarshalResult createMarshalResult(XmlOptions options)
    {
        SoapUnmarshalResult result;
        if (EncodingStyle.SOAP11 == encodingStyle) {
            result = new Soap11UnmarshalResult(bindingLoader, typeTable,
                                               refObjectTable, this,
                                               options);
        } else if (EncodingStyle.SOAP12 == encodingStyle) {
            throw new AssertionError("soap 12 UNIMP: " + encodingStyle);
        } else {
            throw new AssertionError("unknown encoding style: " + encodingStyle);
        }
        return result;
    }

    private QName getIdQName()
    {

        if (EncodingStyle.SOAP11 == encodingStyle) {
            return Soap11Constants.ID_NAME;
        } else if (EncodingStyle.SOAP12 == encodingStyle) {
            throw new AssertionError("soap 12 is unimplemented");
        } else {
            throw new AssertionError("unknown encoding style: " + encodingStyle);
        }

    }

    public XMLStreamReader lookupRef(String ref)
        throws XmlException
    {
        if (attrCache == null) {
            attrCache = new AttrCache(referenceRoot, getIdQName());
        }
        final Node target_node = attrCache.lookup(ref);
        if (target_node == null) {
            return null;
        }

        assert target_node.getNodeType() == Node.ELEMENT_NODE;

        final XMLStreamReader target_stream = Public2.getStream(target_node);
        assert target_stream.isStartElement();

        return target_stream;
    }


}
