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
import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;
import org.apache.xmlbeans.impl.marshal.util.collections.Accumulator;
import org.apache.xmlbeans.impl.marshal.util.collections.AccumulatorFactory;
import org.apache.xmlbeans.impl.values.XmlListImpl;

import java.util.Iterator;

//TODO: optimize list processing to use CharSequence and avoid extra String objects


final class ListArrayConverter
    extends BaseSimpleTypeConverter
{
    private final ListArrayRuntimeBindingType listType;

    private static final char SPACE = ' ';


    ListArrayConverter(ListArrayRuntimeBindingType rtt)
    {
        listType = rtt;
    }


    protected Object getObject(UnmarshalResult context)
        throws XmlException
    {
        final String str = context.getStringValue();

        return getObjectFromListContent(str, context);
    }


    public Object unmarshalAttribute(UnmarshalResult result)
        throws XmlException
    {
        final String str = result.getAttributeStringValue();
        return getObjectFromListContent(str, result);
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        return getObjectFromListContent(lexical_value.toString(), result
        );
    }

    public CharSequence print(Object value, MarshalResult result)
        throws XmlException
    {
        final Iterator itr = ArrayUtils.getCollectionIterator(value);
        if (!itr.hasNext()) return "";


        final RuntimeBindingProperty item_prop = listType.getItemProperty();
        StringBuffer buf = new StringBuffer();

        final Object first = itr.next();
        if (first != null) {
            final CharSequence lex = item_prop.getLexical(first, result);
            buf.append(lex);
        }

        while (itr.hasNext()) {
            final Object item = itr.next();
            if (item == null) continue;
            final CharSequence lex = item_prop.getLexical(item, result);
            buf.append(SPACE);
            buf.append(lex);
        }
        return buf;
    }

    protected Object getObjectFromListContent(final String str,
                                              UnmarshalResult context)
        throws XmlException
    {
        final RuntimeBindingProperty item_prop = listType.getItemProperty();

        final Class list_java_type = listType.getJavaType();
        final Class item_java_type = item_prop.getRuntimeBindingType().getJavaType();

        final TypeUnmarshaller item_um = item_prop.getTypeUnmarshaller(context);

        return unmarshalListString(str, list_java_type, item_java_type,
                                   item_um, context);

    }

    protected static Object unmarshalListString(final CharSequence str,
                                                final Class list_java_type,
                                                final Class item_java_type,
                                                final TypeUnmarshaller item_um,
                                                final UnmarshalResult context)
        throws XmlException
    {
        final Accumulator accum =
            AccumulatorFactory.createAccumulator(list_java_type,
                                                 item_java_type);

        return unmarshalListString(accum, str, item_um, context);
    }

    protected static Object unmarshalListString(final Accumulator accum,
                                                final CharSequence str,
                                                final TypeUnmarshaller item_um,
                                                final UnmarshalResult context)
        throws XmlException
    {
        final String lex = str.toString();//TODO: avoid this call
        final String[] strings = XmlListImpl.split_list(lex);
        for (int i = 0, alen = strings.length; i < alen; i++) {
            final String s = strings[i];
            final Object val = item_um.unmarshalAttribute(s, context);
            accum.append(val);
        }
        return accum.getFinalArray();
    }
}
