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
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

final class FloatTypeConverter
    extends BaseSimpleTypeConverter
{

    public Object unmarshalAttribute(UnmarshalResult context) throws XmlException
    {
        float val = context.getAttributeFloatValue();
        return new Float(val);
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        try {
            final float f = XsTypeConverter.lexFloat(lexical_value);
            return new Float(f);
        }
        catch (NumberFormatException ne) {
            throw new InvalidLexicalValueException(ne, result.getLocation());
        }
    }

    //non simple types can throw a runtime exception
    public CharSequence print(Object value, MarshalResult result)
    {
        Float val = (Float)value;
        return XsTypeConverter.printFloat(val.floatValue());
    }

    protected Object getObject(UnmarshalResult context) throws XmlException
    {
        float val = context.getFloatValue();
        return new Float(val);
    }
}
