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

import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import java.util.Collection;

/**
 * Main entry point into marshalling framework.
 * Use the BindingContextFactory to create one
 */
final class BindingContextImpl implements BindingContext
{
    private final BindingLoader bindingLoader;
    private final RuntimeTypeFactory runtimeTypeFactory;
    private final RuntimeBindingTypeTable typeTable;


    /* package protected -- use the factory */
    BindingContextImpl(BindingLoader bindingLoader)
    {
        this.bindingLoader = bindingLoader;
        runtimeTypeFactory = new RuntimeTypeFactory();
        this.typeTable =
            RuntimeBindingTypeTable.createTable(runtimeTypeFactory);
    }


    public Unmarshaller createUnmarshaller()
        throws XmlException
    {
        return new UnmarshallerImpl(bindingLoader, typeTable);
    }

    public Unmarshaller createUnmarshaller(XmlOptions options)
        throws XmlException
    {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }

        return createUnmarshaller();
    }

    public Marshaller createMarshaller()
        throws XmlException
    {
        return new MarshallerImpl(bindingLoader, typeTable, runtimeTypeFactory);
    }


    public Marshaller createMarshaller(XmlOptions options)
        throws XmlException
    {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }

        return createMarshaller();
    }

    static Collection extractErrorHandler(XmlOptions options)
    {
        if (options != null) {
            Collection underlying = (Collection)options.get(XmlOptions.ERROR_LISTENER);
            if (underlying != null)
                return underlying;
        }

        return FailFastErrorHandler.getInstance();
    }


}
