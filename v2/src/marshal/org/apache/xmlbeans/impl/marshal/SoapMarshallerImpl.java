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
import org.apache.xmlbeans.SoapMarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

class SoapMarshallerImpl
    implements SoapMarshaller
{
    //per binding context constants
    private final BindingLoader loader;
    private final RuntimeBindingTypeTable typeTable;
    private final EncodingStyle encodingStyle;


    SoapMarshallerImpl(BindingLoader loader,
                       RuntimeBindingTypeTable typeTable,
                       EncodingStyle encodingStyle)
    {
        this.loader = loader;
        this.typeTable = typeTable;
        this.encodingStyle = encodingStyle;
    }

    public XMLStreamReader marshalType(Object obj,
                                       QName elementName,
                                       QName schemaType,
                                       String javaType,
                                       XmlOptions options)
        throws XmlException
    {
        NamespaceContext nscontext =
            MarshallerImpl.getNamespaceContextFromOptions(options);

        throw new AssertionError("UNIMP");


    }

    public XMLStreamReader marshalReferenced(XmlOptions options)
        throws XmlException
    {
        throw new AssertionError("UNIMP: this=" + this);
    }
}
