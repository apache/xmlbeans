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

public final class ObjectAnyTypeConverter
    implements TypeConverter
{
    public ObjectAnyTypeConverter()
    {
    }

    public Object unmarshal(UnmarshalResult result)
        throws XmlException
    {
        //TODO: return SOAPElement!!
        throw new AssertionError("GENERIC XML UNIMPLEMENTED");
    }

    public void unmarshalIntoIntermediary(Object intermediary,
                                          UnmarshalResult result)
    {
        throw new UnsupportedOperationException("not used: " + this);
    }

    public Object unmarshalAttribute(UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("unused");
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("unused");
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

    //non simple types can throw a runtime exception
    public CharSequence print(Object value, MarshalResult result)
    {
        //TODO: REVIEW: is this the right thing here?
        return value.toString();
    }
}
