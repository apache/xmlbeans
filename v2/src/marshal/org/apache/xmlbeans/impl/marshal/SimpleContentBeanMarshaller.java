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
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;

final class SimpleContentBeanMarshaller implements TypeMarshaller
{
    private final SimpleContentRuntimeBindingType simpleContentType;
    private final TypeMarshaller contentMarshaller;

    public SimpleContentBeanMarshaller(SimpleContentRuntimeBindingType rtt,
                                       RuntimeBindingTypeTable table,
                                       BindingLoader loader)
        throws XmlException
    {
        simpleContentType = rtt;
        final BindingType content_binding_type =
            rtt.getSimpleContentProperty().getRuntimeBindingType().getBindingType();
        final TypeMarshaller marshaller =
            table.lookupMarshaller(content_binding_type, loader);
        if (marshaller == null) {
            String e = "failed to find marshaller for " + content_binding_type;
            throw new AssertionError(e);
        }
        contentMarshaller = marshaller;
    }

    //non simple types can throw a runtime exception
    public CharSequence print(Object value, MarshalResult result)
        throws XmlException
    {
        final Object simple_content_val =
            simpleContentType.getSimpleContentProperty().getValue(value, result);

        final RuntimeBindingType prop_rtt =
            simpleContentType.getSimpleContentProperty().getRuntimeBindingType();
        final RuntimeBindingType actualRuntimeType =
            MarshalResult.findActualRuntimeType(simple_content_val,
                                                prop_rtt, result);

        TypeMarshaller content_marshaller;
        if (actualRuntimeType == prop_rtt) {
            content_marshaller = contentMarshaller;
        } else {
            RuntimeBindingTypeTable table = result.getTypeTable();
            BindingLoader loader = result.getBindingLoader();
            content_marshaller =
                table.lookupMarshaller(prop_rtt.getBindingType(), loader);
            if (content_marshaller == null) {
                String msg = "unable to find marshaller for " +
                    prop_rtt.getBindingType() + ".  Using default marshaller";
                result.addWarning(msg);
                content_marshaller = contentMarshaller;
            }
        }
        return content_marshaller.print(simple_content_val, result);
    }
}
