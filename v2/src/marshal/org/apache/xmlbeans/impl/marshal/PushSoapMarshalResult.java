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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

abstract class PushSoapMarshalResult
    extends PushMarshalResult
{
    private final ObjectRefTable objectRefTable;

    PushSoapMarshalResult(BindingLoader bindingLoader,
                          RuntimeBindingTypeTable typeTable,
                          XMLStreamWriter writer,
                          XmlOptions options,
                          ObjectRefTable objectRefTable)
        throws XmlException
    {
        super(bindingLoader, typeTable, writer, options);
        this.objectRefTable = objectRefTable;
    }

    final void writeIdParts()
        throws XmlException
    {
        final Iterator itr = objectRefTable.getMultipleRefTableEntries();

        while (itr.hasNext()) {
            final ObjectRefTable.Value cur_val =
                (ObjectRefTable.Value)itr.next();
            marshalTypeWithId(cur_val);
        }
    }

    private void marshalTypeWithId(ObjectRefTable.Value val)
        throws XmlException
    {
        final RuntimeBindingProperty prop = val.getProp();
        final Object obj = val.object;

        final RuntimeBindingType actual_rtt =
            prop.getActualRuntimeType(obj, this);

        try {
            writeStartElement(actual_rtt.getSchemaTypeName());
            fillAndAddAttribute(getIdQName(), getIdValue(val.getId()));
            writeXsiAttributes(obj, actual_rtt, prop);
            updateState(obj, prop);
            writeContents(actual_rtt);
            writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }


    protected final void writeContents(final RuntimeBindingType actual_rtt)
        throws XmlException
    {
        final int id =
            objectRefTable == null ? -1 : objectRefTable.getId(getCurrObject());

        if (id < 0) {
            super.writeContents(actual_rtt);
        } else {
            fillAndAddAttribute(getRefQName(), getRefValue(id));
        }
    }


    protected abstract QName getRefQName();

    protected abstract String getRefValue(int id);

    protected abstract QName getIdQName();

    private static String getIdValue(int id)
    {
        return PullSoapMarshalResult.ID_PREFIX + id;
    }
}
