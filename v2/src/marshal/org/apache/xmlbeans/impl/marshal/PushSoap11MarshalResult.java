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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

final class PushSoap11MarshalResult
    extends PushSoapMarshalResult
{
    PushSoap11MarshalResult(BindingLoader bindingLoader,
                            RuntimeBindingTypeTable typeTable,
                            XMLStreamWriter writer,
                            XmlOptions options,
                            ObjectRefTable objectRefTable)
        throws XmlException
    {
        super(bindingLoader, typeTable, writer, options, objectRefTable);
    }

    protected QName getRefQName()
    {
        return Soap11Constants.REF_NAME;
    }

    protected String getRefValue(int id)
    {
        return Soap11Constants.constructRefValueFromId(id);
    }

    protected QName getIdQName()
    {
        return Soap11Constants.ID_NAME;
    }

    protected String getIdValue(int id)
    {
        return Soap11Constants.constructIdValueFromId(id);
    }
}
