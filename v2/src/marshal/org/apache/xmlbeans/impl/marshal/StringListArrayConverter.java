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
import org.apache.xmlbeans.impl.marshal.util.collections.StringList;

final class StringListArrayConverter
    extends BaseSimpleTypeConverter
{
    private static final Class STR_ARRAY_TYPE = (new String[0]).getClass();

    private static final char SPACE = ' ';

    //could get more clever here but probably not worth the trouble.
    private static final TypeUnmarshaller STR_UNMARSHALLER =
        new StringTypeConverter();


    protected Object getObject(UnmarshalResult context) throws XmlException
    {
        //TODO: process based on CharSequence...
        final String str = context.getStringValue();

        return ListArrayConverter.unmarshalListString(new StringList(),
                                                      str,
                                                      STR_UNMARSHALLER,
                                                      context);
    }

    public Object unmarshalAttribute(UnmarshalResult context) throws XmlException
    {
        final String str = context.getAttributeStringValue();

        return ListArrayConverter.unmarshalListString(new StringList(),
                                                      str,
                                                      STR_UNMARSHALLER,
                                                      context);

    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {

        return ListArrayConverter.unmarshalListString(new StringList(),
                                                      lexical_value.toString(),
                                                      STR_UNMARSHALLER,
                                                      result);
    }

    public CharSequence print(Object value, MarshalResult result)
    {
        String[] val = (String[])value;
        final int alen = val.length;

        if (alen == 0) return "";

        StringBuffer buf = new StringBuffer();
        buf.append(val[0]);

        for (int i = 1; i < alen; i++) {
            buf.append(SPACE);
            buf.append(val[i]);
        }

        return buf;
    }
}
