/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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

    //non simple types can throw a runtime exception
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
