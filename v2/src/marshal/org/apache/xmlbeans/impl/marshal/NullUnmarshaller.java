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

/**
 * Used to unmarshall objects with xsi:nil="true"
 * Just skips the contents and returns null
 */
final class NullUnmarshaller
    implements TypeUnmarshaller
{
    private static final TypeUnmarshaller INSTANCE = new NullUnmarshaller();

    private NullUnmarshaller()
    {
    }

    public Object unmarshal(UnmarshalResult context)
        throws XmlException
    {
        context.skipElement();
        return null;
    }

    public void unmarshal(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }

    public Object unmarshalAttribute(UnmarshalResult context)
    {
        throw new UnsupportedOperationException();
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

    public static TypeUnmarshaller getInstance()
    {
        return INSTANCE;
    }
}
