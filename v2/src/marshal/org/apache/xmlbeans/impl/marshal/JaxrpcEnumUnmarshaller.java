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
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

final class JaxrpcEnumUnmarshaller
    implements TypeUnmarshaller
{
    private final JaxrpcEnumRuntimeBindingType runtimeType;

    public JaxrpcEnumUnmarshaller(JaxrpcEnumRuntimeBindingType rtt)
    {
        runtimeType = rtt;
    }

    public Object unmarshal(UnmarshalResult result)
        throws XmlException
    {
        final TypeUnmarshaller item_um = runtimeType.getItemUnmarshaller();
        final Object itemValue = item_um.unmarshal(result);
        try {
            return runtimeType.fromValue(itemValue, result);
        }
        catch (InvalidLexicalValueException e) {
            result.addError(e.getMessage(), e.getLocation());
            throw e;
        }
    }

    public void unmarshal(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }

    public void unmarshalIntoIntermediary(Object intermediary,
                                          UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }

    public Object unmarshalAttribute(UnmarshalResult result)
        throws XmlException
    {
        final TypeUnmarshaller item_um = runtimeType.getItemUnmarshaller();
        final Object itemValue = item_um.unmarshalAttribute(result);
        return runtimeType.fromValue(itemValue, result);
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        final TypeUnmarshaller item_um = runtimeType.getItemUnmarshaller();
        final Object itemValue = item_um.unmarshalAttribute(lexical_value, result);
        return runtimeType.fromValue(itemValue, result);
    }

    public void unmarshalAttribute(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
    }
}
