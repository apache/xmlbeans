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

import java.io.IOException;
import java.io.InputStream;

/**
 * converter for: byte[] <-> hexBinary
 */
final class HexBinaryTypeConverter
    extends BaseSimpleTypeConverter
{
    protected Object getObject(UnmarshalResult context) throws XmlException
    {
        final InputStream val = context.getHexBinaryValue();
        try {
            return MarshalStreamUtils.inputStreamToBytes(val);
        }
        catch (IOException e) {
            throw new XmlException(e);
        }
    }

    public Object unmarshalAttribute(UnmarshalResult context) throws XmlException
    {
        final InputStream val = context.getAttributeHexBinaryValue();
        try {
            return MarshalStreamUtils.inputStreamToBytes(val);
        }
        catch (IOException e) {
            throw new XmlException(e);
        }
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        try {
            return XsTypeConverter.lexHexBinary(lexical_value);
        }
        catch (InvalidLexicalValueException e) {
            throw new InvalidLexicalValueException(e, result.getLocation());
        }
    }

    //non simple types can throw a runtime exception
    public CharSequence print(Object value, MarshalResult result)
    {
        byte[] val = (byte[])value;
        return XsTypeConverter.printHexBinary(val);
    }
}
